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

    /** A battery grip anywhere in the inventory doubles the charge. */
    private static boolean hasGrip(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(GRIP_TAG)) return true;
        }
        return false;
    }

    private static final net.minecraft.tags.TagKey<Item> GRIP_TAG = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cinecraft", "battery_grips"));

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
            int shots = hasGrip(player) ? CameraState.GRIP_MAX_CHARGE : CameraState.MAX_CHARGE;
            CameraState.setCharge(camera, shots);
            if (!player.getAbilities().instabuild) battery.shrink(1);
            player.displayClientMessage(Component.translatable("item.cinecraft.camera_battery.charged", shots), true);
        }
        return InteractionResultHolder.sidedSuccess(battery, level.isClientSide());
    }
}
