package com.cinecraft.compat;

import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

/** Camera strap: while a strap is in the inventory, cameras cannot be thrown away by accident. */
public final class StrapEvents {
    private StrapEvents() {}

    @SubscribeEvent
    public static void onToss(ItemTossEvent event) {
        if (!(event.getEntity().getItem().getItem() instanceof CameraItem)) return;
        Player player = event.getPlayer();
        if (!hasStrap(player)) return;
        event.setCanceled(true);
        if (!player.level().isClientSide) {
            player.displayClientMessage(Component.translatable("item.cinecraft.camera_strap.held"), true);
        }
    }

    private static boolean hasStrap(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(com.cinecraft.registry.ModGearItems.STRAP_TAG)) return true;
        }
        return false;
    }
}
