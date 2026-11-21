package com.cinecraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Lens cap: use it with a camera in the other hand to put the cap on or take it off. A capped camera cannot shoot. */
public class LensCapItem extends Item {
    public LensCapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack cap = player.getItemInHand(hand);
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack camera = player.getItemInHand(other);
        if (!(camera.getItem() instanceof CineCameraItem)) {
            return InteractionResultHolder.pass(cap);
        }
        if (!level.isClientSide) {
            boolean on = !CameraState.hasLensCap(camera);
            CameraState.setLensCap(camera, on);
            player.displayClientMessage(Component.translatable(on
                    ? "item.cinecraft.lens_cap.on" : "item.cinecraft.lens_cap.off"), true);
        }
        return InteractionResultHolder.sidedSuccess(cap, level.isClientSide());
    }
}
