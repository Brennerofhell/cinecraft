package com.cinecraft.network;

import com.cinecraft.CineCraft;
import com.cinecraft.config.ModConfig;
import com.cinecraft.item.VideoTapeItem;
import com.cinecraft.media.CcfFile;
import com.cinecraft.media.FilmStore;
import com.cinecraft.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server side of the film upload: validates requests, feeds the {@link FilmStore} and writes the film ID onto the tape. */
public final class FilmNetwork {
    private FilmNetwork() {}

    private record Pending(UUID film, int seconds) {}

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();
    private static FilmStore store;

    /** Mod bus: registers the packets. */
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(FilmPayloads.Begin.TYPE, FilmPayloads.Begin.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> onBegin(payload, ctx)));
        registrar.playToServer(FilmPayloads.Chunk.TYPE, FilmPayloads.Chunk.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> onChunk(payload, ctx)));
        registrar.playToServer(FilmPayloads.RequestChunk.TYPE, FilmPayloads.RequestChunk.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> onRequest(payload, ctx)));
        registrar.playToClient(FilmPayloads.ChunkData.TYPE, FilmPayloads.ChunkData.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> com.cinecraft.client.playback.FilmCache.onChunk(payload)));
        registrar.playToServer(FilmPayloads.Finish.TYPE, FilmPayloads.Finish.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> onFinish(ctx)));
    }

    private static synchronized FilmStore store(MinecraftServer server) throws IOException {
        if (store == null) {
            store = new FilmStore(server.getWorldPath(LevelResource.ROOT).resolve("cinecraft"),
                    ModConfig.MAX_FILM_BYTES.get(), ModConfig.TOTAL_STORAGE_BYTES.get());
        }
        return store;
    }

    private static void onBegin(FilmPayloads.Begin p, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        try {
            if (!ModConfig.ENABLE_RECORDING.get()) {
                fail(player, "item.cinecraft.video_tape.disabled");
                return;
            }
            if (p.seconds() < 1 || p.seconds() > ModConfig.MAX_FILM_SECONDS.get()) {
                fail(player, "item.cinecraft.video_tape.too_long");
                return;
            }
            if (emptyTapeSlot(player) == null) {
                fail(player, "item.cinecraft.video_tape.no_tape");
                return;
            }
            store(player.server).begin(player.getUUID(), p.film(), p.totalBytes(), p.chunkCount(), p.sha256());
            PENDING.put(player.getUUID(), new Pending(p.film(), p.seconds()));
        } catch (IOException e) {
            CineCraft.LOGGER.warn("Film upload from {} rejected: {}", player.getGameProfile().getName(), e.getMessage());
            fail(player, "item.cinecraft.video_tape.rejected");
        }
    }

    private static void onChunk(FilmPayloads.Chunk p, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (!PENDING.containsKey(player.getUUID())) return;
        try {
            store(player.server).chunk(player.getUUID(), p.index(), p.data());
        } catch (IOException e) {
            CineCraft.LOGGER.warn("Film chunk from {} rejected: {}", player.getGameProfile().getName(), e.getMessage());
            PENDING.remove(player.getUUID());
            fail(player, "item.cinecraft.video_tape.rejected");
        }
    }

    private static void onFinish(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        Pending pending = PENDING.remove(player.getUUID());
        if (pending == null) return;
        try {
            CcfFile.Header header = store(player.server).finish(player.getUUID());
            ItemStack tape = emptyTapeSlot(player);
            if (tape == null) {
                // The tape was moved away while uploading: hand out a fresh recorded one.
                tape = new ItemStack(com.cinecraft.registry.ModGearItems.byId("video_tape").get());
                player.getInventory().placeItemBackInInventory(tape);
            }
            tape.set(ModDataComponents.FILM_ID.get(), pending.film());
            tape.set(ModDataComponents.FILM_SECONDS.get(), Math.max(1, header.frameCount() / header.fps()));
            player.displayClientMessage(Component.translatable("item.cinecraft.video_tape.saved"), true);
        } catch (IOException e) {
            CineCraft.LOGGER.warn("Film upload from {} failed: {}", player.getGameProfile().getName(), e.getMessage());
            fail(player, "item.cinecraft.video_tape.rejected");
        }
    }

    private static void onRequest(FilmPayloads.RequestChunk p, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        try {
            FilmStore films = store(player.server);
            long total = films.size(p.film());
            byte[] data = total < 0 ? new byte[0] : films.readChunk(p.film(), p.index());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                    new FilmPayloads.ChunkData(p.film(), total, p.index(), data));
        } catch (IOException e) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                    new FilmPayloads.ChunkData(p.film(), -1, p.index(), new byte[0]));
        }
    }

    private static ItemStack emptyTapeSlot(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (VideoTapeItem.isEmpty(stack)) return stack;
        }
        return null;
    }

    private static void fail(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    /** Game bus: an upload that is still running when the player leaves is dropped. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        PENDING.remove(id);
        synchronized (FilmNetwork.class) {
            if (store != null) store.abort(id);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        synchronized (FilmNetwork.class) {
            store = null;
        }
        PENDING.clear();
    }
}
