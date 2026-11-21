package com.cinecraft.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmStyle;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import net.minecraft.world.item.Item;

/**
 * Memory card of the digital camera. It is an Exposure film roll in color (so the camera stores frames on it the
 * normal way) with a clean digital look: low sensitivity, almost no grain. Only the digital camera accepts it,
 * the analog camera only shoots with real film rolls (see {@link CineCameraItem#acceptsMedium(boolean)}).
 */
public class MemoryCardItem extends FilmRollItem {
    public static final int BAR_COLOR = 0x4FA3FF;

    public MemoryCardItem(Properties properties) {
        super(ExposureType.COLOR, BAR_COLOR, properties);
    }

    /** Item properties with the digital film style component. */
    public static Item.Properties properties() {
        return new Item.Properties().stacksTo(16)
                .component(Exposure.DataComponents.FILM_STYLE,
                        FilmStyle.create().withSensitivity(1.0f).withNoise(0.0f));
    }
}
