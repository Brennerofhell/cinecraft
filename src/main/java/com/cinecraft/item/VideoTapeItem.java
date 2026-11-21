package com.cinecraft.item;

import com.cinecraft.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Video tape: an empty tape records a film (hold it and press the record key); a recorded tape carries the film ID. */
public class VideoTapeItem extends Item {
    public VideoTapeItem(Properties properties) {
        super(properties);
    }

    public static boolean isEmpty(ItemStack tape) {
        return tape.getItem() instanceof VideoTapeItem && !tape.has(ModDataComponents.FILM_ID.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (stack.has(ModDataComponents.FILM_ID.get())) {
            int seconds = stack.getOrDefault(ModDataComponents.FILM_SECONDS.get(), 0);
            tooltip.add(Component.translatable("item.cinecraft.video_tape.recorded", seconds).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.cinecraft.video_tape.empty").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.FILM_ID.get());
    }
}
