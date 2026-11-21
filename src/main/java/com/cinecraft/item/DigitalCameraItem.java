package com.cinecraft.item;

import io.github.mortuusars.exposure.world.item.camera.Attachment;

import java.util.List;

/** Digital camera: stores photos on memory cards instead of film rolls. */
public class DigitalCameraItem extends CineCameraItem {
    public DigitalCameraItem(Properties properties) {
        super(properties, "digital_camera", "shoot");
    }

    @Override
    protected List<Attachment<?>> defineAttachments() {
        return CineCameraItem.attachmentsWithFilm(stack -> stack.getItem() instanceof MemoryCardItem);
    }
}
