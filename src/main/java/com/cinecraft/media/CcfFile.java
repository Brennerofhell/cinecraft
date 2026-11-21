package com.cinecraft.media;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CineCraft film container (.ccf): MJPEG frames with an index.
 * <pre>
 * magic "CCF1" | width u16 | height u16 | fps u8 | frameCount u32 | frames: [len u32, jpeg bytes]... | index: frameCount * offset u64 | indexOffset u64
 * </pre>
 * Pure Java, no Minecraft classes, so it can be used on server and client and tested on its own.
 */
public final class CcfFile {
    public static final String MAGIC = "CCF1";
    public static final int MAX_FRAME_BYTES = 1 << 20;
    public static final int MAX_FRAMES = 12 * 60 * 10; // 10 minutes at 12 fps

    private CcfFile() {}

    public record Header(int width, int height, int fps, int frameCount) {}

    /** Writes frames to {@code target} through a temp file and an atomic move. */
    public static void write(Path target, int width, int height, int fps, List<byte[]> jpegFrames) throws IOException {
        if (jpegFrames.size() > MAX_FRAMES) throw new IOException("too many frames");
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        List<Long> offsets = new ArrayList<>();
        try (RandomAccessFile f = new RandomAccessFile(tmp.toFile(), "rw")) {
            f.setLength(0);
            f.write(MAGIC.getBytes(StandardCharsets.US_ASCII));
            f.writeShort(width);
            f.writeShort(height);
            f.writeByte(fps);
            f.writeInt(jpegFrames.size());
            for (byte[] frame : jpegFrames) {
                if (frame.length > MAX_FRAME_BYTES) throw new IOException("frame too large");
                offsets.add(f.getFilePointer());
                f.writeInt(frame.length);
                f.write(frame);
            }
            long indexOffset = f.getFilePointer();
            for (long o : offsets) f.writeLong(o);
            f.writeLong(indexOffset);
        }
        Files.move(tmp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    }

    /** Random-access reader; close when done. */
    public static final class Reader implements AutoCloseable {
        private final RandomAccessFile file;
        private final Header header;
        private final long[] offsets;

        public Reader(Path path) throws IOException {
            file = new RandomAccessFile(path.toFile(), "r");
            try {
                byte[] magic = new byte[4];
                file.readFully(magic);
                if (!MAGIC.equals(new String(magic, StandardCharsets.US_ASCII))) throw new IOException("not a ccf file");
                int w = file.readUnsignedShort();
                int h = file.readUnsignedShort();
                int fps = file.readUnsignedByte();
                int count = file.readInt();
                if (count < 0 || count > MAX_FRAMES || fps == 0) throw new IOException("invalid header");
                header = new Header(w, h, fps, count);
                long len = file.length();
                file.seek(len - 8);
                long indexOffset = file.readLong();
                if (indexOffset < 0 || indexOffset + (long) count * 8 + 8 != len) throw new IOException("corrupt index");
                file.seek(indexOffset);
                offsets = new long[count];
                for (int i = 0; i < count; i++) offsets[i] = file.readLong();
            } catch (IOException | RuntimeException e) {
                file.close();
                throw e;
            }
        }

        public Header header() {
            return header;
        }

        public byte[] readFrame(int index) throws IOException {
            if (index < 0 || index >= offsets.length) throw new IndexOutOfBoundsException("frame " + index);
            file.seek(offsets[index]);
            int len = file.readInt();
            if (len < 0 || len > MAX_FRAME_BYTES) throw new IOException("corrupt frame length");
            byte[] data = new byte[len];
            file.readFully(data);
            return data;
        }

        @Override
        public void close() throws IOException {
            file.close();
        }
    }
}
