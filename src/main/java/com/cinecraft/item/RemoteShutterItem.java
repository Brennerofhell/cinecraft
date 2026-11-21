package com.cinecraft.item;

import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Remote shutter: triggers the first ready camera in the inventory without opening its viewfinder. */
public class RemoteShutterItem extends Item {
    public static final int COOLDOWN_TICKS = 40;

    public RemoteShutterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack remote = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(remote);
        }
        // Exposure's mixins make players CameraHolders at runtime.
        CameraHolder holder = (CameraHolder) (Object) player;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof CameraItem camera && camera.canTakePhoto(holder, stack)) {
                camera.release(holder, stack);
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                return InteractionResultHolder.success(remote);
            }
        }
        player.displayClientMessage(Component.translatable("item.cinecraft.remote_shutter.no_camera"), true);
        return InteractionResultHolder.fail(remote);
    }
}
