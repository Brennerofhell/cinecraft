package com.cinecraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Camera cage: use it with a camera in the other hand to rig or unrig the cage. A caged camera recovers faster after each shot. */
public class CageItem extends Item {
    public CageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack cage = player.getItemInHand(hand);
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack camera = player.getItemInHand(other);
        if (!(camera.getItem() instanceof CineCameraItem)) {
            return InteractionResultHolder.pass(cage);
        }
        if (!level.isClientSide) {
            boolean on = !CameraState.hasCage(camera);
            CameraState.setCage(camera, on);
            player.displayClientMessage(Component.translatable(on
                    ? "item.cinecraft.camera_cage.on" : "item.cinecraft.camera_cage.off"), true);
        }
        return InteractionResultHolder.sidedSuccess(cage, level.isClientSide());
    }
}
