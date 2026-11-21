package com.cinecraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Camera battery: use it with a digital camera in the other hand to charge it ({@link CameraState#MAX_CHARGE} shots). */
public class BatteryItem extends Item {
    public BatteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack battery = player.getItemInHand(hand);
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack camera = player.getItemInHand(other);
        if (!(camera.getItem() instanceof DigitalCameraItem)) {
            return InteractionResultHolder.pass(battery);
        }
        if (!level.isClientSide) {
            if (CameraState.charge(camera) > 0) {
                player.displayClientMessage(Component.translatable("item.cinecraft.camera_battery.not_empty"), true);
                return InteractionResultHolder.fail(battery);
            }
            CameraState.setCharge(camera, CameraState.MAX_CHARGE);
            if (!player.getAbilities().instabuild) battery.shrink(1);
            player.displayClientMessage(Component.translatable("item.cinecraft.camera_battery.charged",
                    CameraState.MAX_CHARGE), true);
        }
        return InteractionResultHolder.sidedSuccess(battery, level.isClientSide());
    }
}
