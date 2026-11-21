package com.cinecraft.client.playback;

import com.cinecraft.CineCraft;
import com.cinecraft.media.CcfFile;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Plays films on the client: keeps one dynamic texture per film, decodes the JPEG frame that matches the current
 * time on a worker thread and uploads it on the render thread. Films loop.
 */
@EventBusSubscriber(modid = CineCraft.MOD_ID, value = Dist.CLIENT)
public final class FilmPlayback {
    private FilmPlayback() {}

    /** Texture of the current frame and the width / height ratio of the picture. */
    public record Frame(ResourceLocation texture, float aspect) {}

    private static final Map<UUID, Playback> PLAYBACKS = new HashMap<>();
    private static final Set<UUID> BROKEN = new HashSet<>();
    private static final long CLEANUP_INTERVAL_NANOS = 5_000_000_000L;
    private static final long UNUSED_NANOS = 30_000_000_000L;
    private static long lastCleanupNanos = System.nanoTime();
    private static final ExecutorService DECODER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cinecraft-decoder");
        t.setDaemon(true);
        return t;
    });

    private record Decoded(int frame, BufferedImage image) {}

    private static final class Playback {
        final CcfFile.Reader reader;
        final CcfFile.Header header;
        final DynamicTexture texture;
        final ResourceLocation location;
        final AtomicReference<Decoded> ready = new AtomicReference<>();
        volatile int decoding = -1;
        int shown = -1;
        long lastUsedNanos = System.nanoTime();

        Playback(UUID film, Path file) throws IOException {
            reader = new CcfFile.Reader(file);
            header = reader.header();
            texture = new DynamicTexture(new NativeImage(header.width(), header.height(), false));
            location = ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, "film/" + film);
            Minecraft.getInstance().getTextureManager().register(location, texture);
        }

        void close() {
            Minecraft.getInstance().getTextureManager().release(location);
            synchronized (reader) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // closing a read-only file
                }
            }
        }
    }

    /**
     * The frame to show at {@code timeMillis} (world time, so all players see the same moment), or {@code null}
     * while the film is still loading (or broken). Render thread only.
     */
    public static Frame current(UUID film, long timeMillis) {
        if (BROKEN.contains(film)) return null;
        Playback pb = PLAYBACKS.get(film);
        if (pb == null) {
            Path file = FilmCache.fileIfReady(film);
            if (file == null) return null;
            try {
                pb = new Playback(film, file);
                PLAYBACKS.put(film, pb);
            } catch (IOException e) {
                CineCraft.LOGGER.warn("Film {} cannot be played: {}", film, e.getMessage());
                BROKEN.add(film);
                return null;
            }
        }
        pb.lastUsedNanos = System.nanoTime();
        releaseUnused();
        advance(pb, timeMillis);
        if (pb.shown < 0) return null; // first frame not decoded yet
        return new Frame(pb.location, pb.header.width() / (float) pb.header.height());
    }

    /** Frees the textures of films nobody has looked at for a while (checked every few seconds). */
    private static void releaseUnused() {
        long now = System.nanoTime();
        if (now - lastCleanupNanos < CLEANUP_INTERVAL_NANOS) return;
        lastCleanupNanos = now;
        PLAYBACKS.values().removeIf(pb -> {
            if (now - pb.lastUsedNanos > UNUSED_NANOS) {
                pb.close();
                return true;
            }
            return false;
        });
    }

    private static void advance(Playback pb, long timeMillis) {
        Decoded d = pb.ready.getAndSet(null);
        if (d != null && d.image().getWidth() == pb.header.width() && d.image().getHeight() == pb.header.height()) {
            NativeImage pixels = pb.texture.getPixels();
            if (pixels != null) {
                for (int y = 0; y < d.image().getHeight(); y++) {
                    for (int x = 0; x < d.image().getWidth(); x++) {
                        int rgb = d.image().getRGB(x, y);
                        int r = rgb >> 16 & 0xFF;
                        int g = rgb >> 8 & 0xFF;
                        int b = rgb & 0xFF;
                        pixels.setPixelRGBA(x, y, 0xFF000000 | b << 16 | g << 8 | r);
                    }
                }
                pb.texture.upload();
                pb.shown = d.frame();
            }
        }
        int frame = (int) (timeMillis * pb.header.fps() / 1000 % pb.header.frameCount());
        if (frame != pb.shown && frame != pb.decoding) {
            pb.decoding = frame;
            DECODER.execute(() -> {
                try {
                    byte[] jpeg;
                    synchronized (pb.reader) {
                        jpeg = pb.reader.readFrame(frame);
                    }
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(jpeg));
                    if (image != null) pb.ready.set(new Decoded(frame, image));
                } catch (IOException | RuntimeException e) {
                    CineCraft.LOGGER.debug("Could not decode film frame {}: {}", frame, e.getMessage());
                } finally {
                    pb.decoding = -1;
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PLAYBACKS.values().forEach(Playback::close);
        PLAYBACKS.clear();
        BROKEN.clear();
        FilmCache.clear();
    }
}
