package com.cinecraft.compat;

import io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * CineCraft is an addon for Exposure. When Exposure finishes a photo (server side), the CineCraft cameras held by the
 * photographer play their shutter animation.
 */
public final class ExposureCompat {
    private ExposureCompat() {}

    @SubscribeEvent
    public static void onFrameAdded(FrameAddedEvent event) {
        Entity holder = event.getCameraHolderEntity();
        if (!(holder instanceof LivingEntity living)) return;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = living.getItemInHand(hand);
            if (stack.getItem() instanceof com.cinecraft.item.CineCameraItem cam) {
                cam.playAction(living, stack);
            }
        }
    }
}
