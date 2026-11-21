package com.cinecraft.media;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Server-side film storage under {@code <world>/cinecraft/films} plus the chunked upload protocol.
 * Pure Java (no Minecraft classes) so it can be tested on its own.
 *
 * <p>Rules: file names come from UUIDs only, uploads are written to a temp file, checked (size, SHA-256, valid
 * {@link CcfFile}) and then moved into place atomically; limits for single film size and total storage apply.
 */
public final class FilmStore {
    /** Chunk size used by clients; the server accepts anything up to this. */
    public static final int CHUNK_SIZE = 16 * 1024;

    private final Path filmsDir;
    private final Path tmpDir;
    private final long maxFilmBytes;
    private final long totalStorageBytes;
    private final Map<UUID, Upload> uploads = new HashMap<>();

    public FilmStore(Path root, long maxFilmBytes, long totalStorageBytes) throws IOException {
        this.filmsDir = root.resolve("films");
        this.tmpDir = root.resolve("tmp");
        this.maxFilmBytes = maxFilmBytes;
        this.totalStorageBytes = totalStorageBytes;
        Files.createDirectories(filmsDir);
        Files.createDirectories(tmpDir);
    }

    public Path pathFor(UUID film) {
        Path p = filmsDir.resolve(film + ".ccf").normalize();
        if (!p.startsWith(filmsDir)) throw new IllegalArgumentException("bad film id");
        return p;
    }

    public boolean has(UUID film) {
        return Files.isRegularFile(pathFor(film));
    }

    public long usedBytes() throws IOException {
        try (Stream<Path> files = Files.list(filmsDir)) {
            return files.filter(Files::isRegularFile).mapToLong(f -> {
                try {
                    return Files.size(f);
                } catch (IOException e) {
                    return 0;
                }
            }).sum();
        }
    }

    /** Size in bytes of a stored film, or -1 if it does not exist. */
    public long size(UUID film) {
        try {
            Path p = pathFor(film);
            return Files.isRegularFile(p) ? Files.size(p) : -1;
        } catch (IOException e) {
            return -1;
        }
    }

    /** Reads chunk {@code index} (of {@link #CHUNK_SIZE} bytes, the last one may be shorter) of a stored film. */
    public byte[] readChunk(UUID film, int index) throws IOException {
        long size = size(film);
        long from = (long) index * CHUNK_SIZE;
        if (size < 0 || index < 0 || from >= size) throw new IOException("no such chunk");
        byte[] data = new byte[(int) Math.min(CHUNK_SIZE, size - from)];
        try (RandomAccessFile f = new RandomAccessFile(pathFor(film).toFile(), "r")) {
            f.seek(from);
            f.readFully(data);
        }
        return data;
    }

    /** Starts an upload for {@code player}; only one upload per player at a time. */
    public synchronized void begin(UUID player, UUID film, long totalBytes, int chunkCount, byte[] sha256) throws IOException {
        if (uploads.containsKey(player)) throw new IOException("upload already running");
        if (totalBytes <= 0 || totalBytes > maxFilmBytes) throw new IOException("film too large");
        long expectedChunks = (totalBytes + CHUNK_SIZE - 1) / CHUNK_SIZE;
        if (chunkCount != expectedChunks) throw new IOException("bad chunk count");
        if (sha256.length != 32) throw new IOException("bad hash");
        if (has(film)) throw new IOException("film exists");
        if (usedBytes() + totalBytes > totalStorageBytes) throw new IOException("storage full");
        Path tmp = tmpDir.resolve(player + ".part");
        Files.deleteIfExists(tmp);
        uploads.put(player, new Upload(film, totalBytes, chunkCount, sha256.clone(), tmp));
    }

    /** Appends the next chunk (must arrive in order). */
    public synchronized void chunk(UUID player, int index, byte[] data) throws IOException {
        Upload up = uploads.get(player);
        if (up == null) throw new IOException("no upload");
        try {
            if (index != up.nextIndex) throw new IOException("chunk out of order");
            if (data.length == 0 || data.length > CHUNK_SIZE) throw new IOException("bad chunk size");
            if (up.received + data.length > up.totalBytes) throw new IOException("too much data");
            try (RandomAccessFile f = new RandomAccessFile(up.tmp.toFile(), "rw")) {
                f.seek(up.received);
                f.write(data);
            }
            up.received += data.length;
            up.nextIndex++;
        } catch (IOException | RuntimeException e) {
            abort(player);
            throw e;
        }
    }

    /** Completes the upload: verifies size, hash and file format, then stores the film. Returns its header. */
    public synchronized CcfFile.Header finish(UUID player) throws IOException {
        Upload up = uploads.get(player);
        if (up == null) throw new IOException("no upload");
        try {
            if (up.nextIndex != up.chunkCount || up.received != up.totalBytes) throw new IOException("incomplete upload");
            if (!Arrays.equals(sha256Of(up.tmp), up.sha256)) throw new IOException("hash mismatch");
            CcfFile.Header header;
            try (CcfFile.Reader reader = new CcfFile.Reader(up.tmp)) {
                header = reader.header();
            }
            Files.move(up.tmp, pathFor(up.film), StandardCopyOption.ATOMIC_MOVE);
            uploads.remove(player);
            return header;
        } catch (IOException | RuntimeException e) {
            abort(player);
            throw e;
        }
    }

    /** Drops a running upload and its temp file (also call when a player disconnects). */
    public synchronized void abort(UUID player) {
        Upload up = uploads.remove(player);
        if (up != null) {
            try {
                Files.deleteIfExists(up.tmp);
            } catch (IOException ignored) {
                // temp file is cleaned up on the next upload start
            }
        }
    }

    public static byte[] sha256Of(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (var in = Files.newInputStream(file)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class Upload {
        final UUID film;
        final long totalBytes;
        final int chunkCount;
        final byte[] sha256;
        final Path tmp;
        long received;
        int nextIndex;

        Upload(UUID film, long totalBytes, int chunkCount, byte[] sha256, Path tmp) {
            this.film = film;
            this.totalBytes = totalBytes;
            this.chunkCount = chunkCount;
            this.sha256 = sha256;
            this.tmp = tmp;
        }
    }
}
