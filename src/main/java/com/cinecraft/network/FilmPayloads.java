package com.cinecraft.network;

import com.cinecraft.CineCraft;
import com.cinecraft.media.FilmStore;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Client to server packets of the film upload (see {@link FilmStore}). */
public final class FilmPayloads {
    private FilmPayloads() {}

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> typeOf(String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, path));
    }

    public record Begin(UUID film, long totalBytes, int chunkCount, byte[] sha256, int seconds) implements CustomPacketPayload {
        public static final Type<Begin> TYPE = typeOf("film_begin");
        public static final StreamCodec<RegistryFriendlyByteBuf, Begin> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Begin::film,
                ByteBufCodecs.VAR_LONG, Begin::totalBytes,
                ByteBufCodecs.VAR_INT, Begin::chunkCount,
                ByteBufCodecs.byteArray(32), Begin::sha256,
                ByteBufCodecs.VAR_INT, Begin::seconds,
                Begin::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Chunk(int index, byte[] data) implements CustomPacketPayload {
        public static final Type<Chunk> TYPE = typeOf("film_chunk");
        public static final StreamCodec<RegistryFriendlyByteBuf, Chunk> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Chunk::index,
                ByteBufCodecs.byteArray(FilmStore.CHUNK_SIZE), Chunk::data,
                Chunk::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Finish() implements CustomPacketPayload {
        public static final Type<Finish> TYPE = typeOf("film_finish");
        public static final StreamCodec<RegistryFriendlyByteBuf, Finish> CODEC = StreamCodec.unit(new Finish());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client asks the server for chunk {@code index} of a film; the answer is {@link ChunkData}. */
    public record RequestChunk(UUID film, int index) implements CustomPacketPayload {
        public static final Type<RequestChunk> TYPE = typeOf("film_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestChunk> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, RequestChunk::film,
                ByteBufCodecs.VAR_INT, RequestChunk::index,
                RequestChunk::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** One chunk of a film; {@code total} is the film size in bytes, or -1 if the server does not have the film. */
    public record ChunkData(UUID film, long total, int index, byte[] data) implements CustomPacketPayload {
        public static final Type<ChunkData> TYPE = typeOf("film_data");
        public static final StreamCodec<RegistryFriendlyByteBuf, ChunkData> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, ChunkData::film,
                ByteBufCodecs.VAR_LONG, ChunkData::total,
                ByteBufCodecs.VAR_INT, ChunkData::index,
                ByteBufCodecs.byteArray(FilmStore.CHUNK_SIZE), ChunkData::data,
                ChunkData::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
