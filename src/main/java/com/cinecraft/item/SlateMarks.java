package com.cinecraft.item;

/** Side-neutral hand-over from the clapperboard (client use) to the film recorder: the last clap. */
public final class SlateMarks {
    private SlateMarks() {}

    private static volatile int scene;
    private static volatile int take;
    private static volatile long serial;

    public static void clap(int sceneNo, int takeNo) {
        scene = sceneNo;
        take = takeNo;
        serial++;
    }

    public static int scene() { return scene; }
    public static int take() { return take; }
    public static long serial() { return serial; }
}
