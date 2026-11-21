package com.cinecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModConfig {
    private ModConfig() {}

    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_RECORDING;
    public static final ModConfigSpec.IntValue MAX_FILM_SECONDS;
    public static final ModConfigSpec.LongValue MAX_FILM_BYTES;
    public static final ModConfigSpec.LongValue TOTAL_STORAGE_BYTES;

    public static final ModConfigSpec.IntValue CAPTURE_HEIGHT;
    public static final ModConfigSpec.IntValue CAPTURE_FPS;

    static {
        ModConfigSpec.Builder s = new ModConfigSpec.Builder();
        s.push("recording");
        ENABLE_RECORDING = s.comment("Allow players to record films onto video tapes and upload them to the server (experimental)")
                .define("enableRecording", false);
        s.pop();
        s.push("limits");
        MAX_FILM_SECONDS = s.comment("Maximum length of a single film in seconds")
                .defineInRange("maxFilmSeconds", 60, 1, 600);
        MAX_FILM_BYTES = s.comment("Maximum size of a single film file in bytes")
                .defineInRange("maxFilmBytes", 32L * 1024 * 1024, 1024L, 512L * 1024 * 1024);
        TOTAL_STORAGE_BYTES = s.comment("Total server storage for films in bytes")
                .defineInRange("totalStorageBytes", 2L * 1024 * 1024 * 1024, 1024L * 1024, Long.MAX_VALUE);
        s.pop();
        SERVER_SPEC = s.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.push("capture");
        CAPTURE_HEIGHT = c.comment("Capture height in pixels (480 = 480p)")
                .defineInRange("captureHeight", 480, 144, 720);
        CAPTURE_FPS = c.comment("Capture frame rate")
                .defineInRange("captureFps", 12, 5, 30);
        c.pop();
        CLIENT_SPEC = c.build();
    }
}
