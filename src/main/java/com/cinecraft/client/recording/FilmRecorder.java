package com.cinecraft.client.recording;

import com.cinecraft.CineCraft;
import com.cinecraft.config.ModConfig;
import com.cinecraft.item.SlateMarks;
import com.cinecraft.item.VideoTapeItem;
import com.cinecraft.media.CcfFile;
import com.cinecraft.media.FilmStore;
import com.cinecraft.network.FilmPayloads;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Client side film recording (MVP): with an empty video tape in hand, the record key starts and stops recording.
 * Frames are grabbed from the main render target at the configured rate, shrunk, JPEG-encoded on a worker thread,
 * packed into a {@link CcfFile} and uploaded in chunks. The server decides whether recording is allowed at all
 * ({@code enableRecording} in the server config, off by default).
 */
@EventBusSubscriber(modid = CineCraft.MOD_ID, value = Dist.CLIENT)
public final class FilmRecorder {
    private FilmRecorder() {}

    public static final KeyMapping RECORD_KEY = new KeyMapping("key.cinecraft.record", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K, "key.categories.cinecraft");

    private static final int MAX_PENDING_FRAMES = 24;
    private static final int PACKETS_PER_TICK = 16;

    private static final ExecutorService ENCODER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cinecraft-encoder");
        t.setDaemon(true);
        return t;
    });
    private static final Queue<CustomPacketPayload> OUTGOING = new ConcurrentLinkedQueue<>();

    private static boolean recording;
    private static long startNanos;
    private static long lastFrameNanos;
    private static int width;
    private static int height;
    private static int fps;
    private static List<Future<byte[]>> frames = new ArrayList<>();
    private static long seenClap;
    /** Frames left that show the slate text after a clap while recording. */
    private static int slateFramesLeft;
    private static boolean flashNext;
    private static String slateText = "";

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(RECORD_KEY);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (RECORD_KEY.consumeClick()) {
            if (recording) {
                stop(mc);
            } else {
                start(mc);
            }
        }
        if (recording && mc.player == null) {
            recording = false;
            frames = new ArrayList<>();
        }
        if (recording && (System.nanoTime() - startNanos) / 1_000_000_000L >= maxSeconds()) {
            stop(mc);
        }
        for (int i = 0; i < PACKETS_PER_TICK; i++) {
            CustomPacketPayload next = OUTGOING.poll();
            if (next == null) break;
            PacketDistributor.sendToServer(next);
        }
    }

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Post event) {
        if (!recording) return;
        long now = System.nanoTime();
        if (now - lastFrameNanos < 1_000_000_000L / fps) return;
        if (SlateMarks.serial() != seenClap) {
            seenClap = SlateMarks.serial();
            slateText = "SCENE " + SlateMarks.scene() + "  TAKE " + SlateMarks.take();
            slateFramesLeft = Math.max(1, fps); // about one second
            flashNext = true;
        }
        long pending = frames.stream().filter(f -> !f.isDone()).count();
        if (pending >= MAX_PENDING_FRAMES) return; // encoder cannot keep up: drop this frame
        lastFrameNanos = now;
        capture(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        recording = false;
        frames = new ArrayList<>();
        OUTGOING.clear();
    }

    private static int maxSeconds() {
        try {
            return ModConfig.MAX_FILM_SECONDS.get();
        } catch (IllegalStateException e) {
            return 60; // server config not synced yet
        }
    }

    private static void start(Minecraft mc) {
        if (mc.player == null || !holdsEmptyTape(mc)) {
            message(mc, "item.cinecraft.video_tape.hold_tape");
            return;
        }
        fps = ModConfig.CAPTURE_FPS.get();
        height = ModConfig.CAPTURE_HEIGHT.get();
        frames = new ArrayList<>();
        startNanos = System.nanoTime();
        lastFrameNanos = 0;
        seenClap = SlateMarks.serial();
        slateFramesLeft = 0;
        flashNext = false;
        recording = true;
        message(mc, "item.cinecraft.video_tape.recording");
    }

    private static boolean holdsEmptyTape(Minecraft mc) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = mc.player.getItemInHand(hand);
            if (VideoTapeItem.isEmpty(stack)) return true;
        }
        return false;
    }

    /** Grabs the current frame, shrinks it and hands the JPEG encoding to the worker thread. */
    private static void capture(Minecraft mc) {
        RenderTarget target = mc.getMainRenderTarget();
        try (NativeImage image = Screenshot.takeScreenshot(target)) {
            int srcW = image.getWidth();
            int srcH = image.getHeight();
            int outH = Math.min(height, srcH);
            int outW = Math.max(1, Math.round(srcW * (float) outH / srcH));
            width = outW;
            int[] rgb = new int[outW * outH];
            for (int y = 0; y < outH; y++) {
                int sy = y * srcH / outH;
                for (int x = 0; x < outW; x++) {
                    int px = image.getPixelRGBA(x * srcW / outW, sy); // ABGR
                    rgb[y * outW + x] = (px & 0xFF) << 16 | (px >> 8 & 0xFF) << 8 | (px >> 16 & 0xFF);
                }
            }
            final int w = outW;
            final int h = outH;
            height = outH;
            final boolean timecode = carries(mc, "timecode_generator");
            final boolean bars = carries(mc, "follow_focus");
            final int index = frames.size();
            final int rate = fps;
            final boolean flash = flashNext;
            flashNext = false;
            final String slate = slateFramesLeft > 0 ? slateText : null;
            if (slateFramesLeft > 0) slateFramesLeft--;
            frames.add(ENCODER.submit(() -> jpeg(rgb, w, h, timecode ? index : -1, rate, bars, flash, slate)));
        } catch (RuntimeException e) {
            CineCraft.LOGGER.warn("Frame capture failed, stopping recording", e);
            recording = false;
            message(mc, "item.cinecraft.video_tape.failed");
        }
    }

    /** True if the player carries the given CineCraft item anywhere in the inventory (hotbar, main, offhand). */
    private static boolean carries(Minecraft mc, String path) {
        if (mc.player == null) return false;
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, path));
        return mc.player.getInventory().contains(new ItemStack(item));
    }

    private static byte[] jpeg(int[] rgb, int w, int h, int timecodeFrame, int rate, boolean bars,
                               boolean flash, String slate) throws java.io.IOException {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, rgb, 0, w);
        if (bars || timecodeFrame >= 0 || flash || slate != null) {
            Graphics2D g = img.createGraphics();
            try {
                if (flash) {
                    g.setColor(new Color(255, 255, 255, 170));
                    g.fillRect(0, 0, w, h);
                }
                if (slate != null) {
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, Math.max(10, h / 14)));
                    int sy = Math.max(12, h / 8);
                    g.setColor(Color.BLACK);
                    g.drawString(slate, w / 40 + 1, sy + 1);
                    g.setColor(Color.WHITE);
                    g.drawString(slate, w / 40, sy);
                }
                if (bars) {
                    // letterbox to 2.39:1
                    int barH = Math.max(0, (h - Math.round(w / 2.39f)) / 2);
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, w, barH);
                    g.fillRect(0, h - barH, w, barH);
                }
                if (timecodeFrame >= 0) {
                    int total = timecodeFrame / Math.max(1, rate);
                    String tc = String.format("%02d:%02d:%02d:%02d", total / 3600, total / 60 % 60, total % 60,
                            timecodeFrame % Math.max(1, rate));
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, Math.max(10, h / 18)));
                    int x = w / 40;
                    int y = h - Math.max(6, h / 14);
                    g.setColor(Color.BLACK);
                    g.drawString(tc, x + 1, y + 1);
                    g.setColor(Color.WHITE);
                    g.drawString(tc, x, y);
                }
            } finally {
                g.dispose();
            }
        }
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        try {
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.6f);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (MemoryCacheImageOutputStream stream = new MemoryCacheImageOutputStream(out)) {
                writer.setOutput(stream);
                writer.write(null, new IIOImage(img, null, null), param);
            }
            return out.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    private static void stop(Minecraft mc) {
        recording = false;
        List<Future<byte[]>> done = frames;
        frames = new ArrayList<>();
        int w = width;
        int h = height;
        int rate = fps;
        long millis = (System.nanoTime() - startNanos) / 1_000_000L;
        message(mc, "item.cinecraft.video_tape.saving");
        Thread t = new Thread(() -> finishAndUpload(mc, done, w, h, rate, millis), "cinecraft-film-upload");
        t.setDaemon(true);
        t.start();
    }

    private static void finishAndUpload(Minecraft mc, List<Future<byte[]>> pending, int w, int h, int rate, long millis) {
        try {
            if (pending.size() < 2) {
                mc.execute(() -> message(mc, "item.cinecraft.video_tape.too_short"));
                return;
            }
            List<byte[]> jpegs = new ArrayList<>();
            for (Future<byte[]> f : pending) jpegs.add(f.get());
            Path tmp = Files.createTempFile("cinecraft", ".ccf");
            try {
                CcfFile.write(tmp, w, h, rate, jpegs);
                byte[] data = Files.readAllBytes(tmp);
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
                int chunks = (data.length + FilmStore.CHUNK_SIZE - 1) / FilmStore.CHUNK_SIZE;
                int seconds = (int) Math.max(1, Math.round(millis / 1000.0));
                OUTGOING.add(new FilmPayloads.Begin(UUID.randomUUID(), data.length, chunks, hash, seconds));
                for (int i = 0; i < chunks; i++) {
                    int from = i * FilmStore.CHUNK_SIZE;
                    OUTGOING.add(new FilmPayloads.Chunk(i, Arrays.copyOfRange(data, from,
                            Math.min(data.length, from + FilmStore.CHUNK_SIZE))));
                }
                OUTGOING.add(new FilmPayloads.Finish());
            } finally {
                Files.deleteIfExists(tmp);
            }
        } catch (Exception e) {
            CineCraft.LOGGER.warn("Could not prepare film upload", e);
            mc.execute(() -> message(mc, "item.cinecraft.video_tape.failed"));
        }
    }

    private static void message(Minecraft mc, String key) {
        if (mc.player != null) mc.player.displayClientMessage(Component.translatable(key), true);
    }
}
