package com.cinecraft.client.playback;

import com.cinecraft.CineCraft;
import com.cinecraft.media.CcfFile;
import com.cinecraft.network.FilmPayloads;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client film cache in {@code <game dir>/cinecraft-cache}. A film is fetched chunk by chunk (the next chunk is
 * requested when the previous one arrived, so the server never has to queue anything) and checked as a CCF file.
 * Films never change once stored (they are named by UUID), so cached files are reused across sessions.
 */
public final class FilmCache {
    private FilmCache() {}

    private static final long MAX_BYTES = 512L * 1024 * 1024;

    private enum State { DOWNLOADING, READY, FAILED }

    private static final class Entry {
        State state = State.DOWNLOADING;
        Path file;
        OutputStream out;
        long total = -1;
        long received;
        int next;
    }

    private static final Map<UUID, Entry> ENTRIES = new ConcurrentHashMap<>();

    /** Returns the local file once the film is complete; starts the download on first call. */
    public static Path fileIfReady(UUID film) {
        Entry e = ENTRIES.get(film);
        if (e == null) {
            e = start(film);
        }
        return e.state == State.READY ? e.file : null;
    }

    private static synchronized Entry start(UUID film) {
        Entry existing = ENTRIES.get(film);
        if (existing != null) return existing;
        Entry e = new Entry();
        ENTRIES.put(film, e);
        try {
            Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("cinecraft-cache");
            Files.createDirectories(dir);
            e.file = dir.resolve(film + ".ccf");
            if (Files.isRegularFile(e.file) && isValid(e.file)) {
                e.state = State.READY;
                return e;
            }
            e.out = Files.newOutputStream(e.file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            PacketDistributor.sendToServer(new FilmPayloads.RequestChunk(film, 0));
        } catch (IOException ex) {
            fail(film, e, ex);
        }
        return e;
    }

    /** Runs on the client main thread for every chunk the server sends. */
    public static void onChunk(FilmPayloads.ChunkData c) {
        Entry e = ENTRIES.get(c.film());
        if (e == null || e.state != State.DOWNLOADING) return;
        try {
            if (c.total() <= 0 || c.total() > MAX_BYTES || c.index() != e.next || c.data().length == 0) {
                throw new IOException("unexpected chunk (film missing on server or bad data)");
            }
            e.total = c.total();
            e.out.write(c.data());
            e.received += c.data().length;
            e.next++;
            if (e.received > e.total) {
                throw new IOException("too much data");
            } else if (e.received == e.total) {
                e.out.close();
                if (isValid(e.file)) {
                    e.state = State.READY;
                } else {
                    throw new IOException("downloaded film is not valid");
                }
            } else {
                PacketDistributor.sendToServer(new FilmPayloads.RequestChunk(c.film(), e.next));
            }
        } catch (IOException ex) {
            fail(c.film(), e, ex);
        }
    }

    private static boolean isValid(Path file) {
        try (CcfFile.Reader ignored = new CcfFile.Reader(file)) {
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static void fail(UUID film, Entry e, Exception ex) {
        CineCraft.LOGGER.warn("Film {} could not be loaded: {}", film, ex.getMessage());
        e.state = State.FAILED;
        try {
            if (e.out != null) e.out.close();
            if (e.file != null) Files.deleteIfExists(e.file);
        } catch (IOException ignored) {
            // nothing more to do
        }
    }

    /** Forget all progress (on disconnect); finished files stay on disk. */
    public static void clear() {
        ENTRIES.values().forEach(e -> {
            try {
                if (e.out != null && e.state == State.DOWNLOADING) {
                    e.out.close();
                    Files.deleteIfExists(e.file);
                }
            } catch (IOException ignored) {
                // best effort
            }
        });
        ENTRIES.clear();
    }
}
