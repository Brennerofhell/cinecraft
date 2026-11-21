package com.cinecraft.item;


import java.util.List;

/** Digital camera: stores photos on memory cards instead of film rolls. */
public class DigitalCameraItem extends CineCameraItem {
    public DigitalCameraItem(Properties properties) {
        super(properties, "digital_camera", "shoot");
    }

    /** The digital camera needs charge in its battery. */
    @Override
    public boolean canTakePhoto(io.github.mortuusars.exposure.world.entity.CameraHolder holder, net.minecraft.world.item.ItemStack stack) {
        return CameraState.charge(stack) > 0 && super.canTakePhoto(holder, stack);
    }

    @Override
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, TooltipContext context,
                                List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.cinecraft.camera_battery.tooltip",
                CameraState.charge(stack), CameraState.MAX_CHARGE).withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    /** The digital camera takes memory cards only. */
    @Override
    protected boolean acceptsMedium(boolean isMemoryCard) {
        return isMemoryCard;
    }
}
