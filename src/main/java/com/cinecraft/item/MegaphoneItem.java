package com.cinecraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Director's megaphone: right-click shouts "Action!" to everyone nearby, sneak + right-click shouts "Cut!". */
public class MegaphoneItem extends Item {
    private static final double RANGE = 48.0;
    private static final int COOLDOWN_TICKS = 30;

    public MegaphoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean cut = player.isShiftKeyDown();
            Component message = Component.translatable(cut ? "item.cinecraft.megaphone.cut" : "item.cinecraft.megaphone.action",
                    player.getDisplayName());
            for (Player other : level.players()) {
                if (other.distanceToSqr(player) <= RANGE * RANGE && other instanceof ServerPlayer target) {
                    target.displayClientMessage(message, true);
                }
            }
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(),
                    SoundSource.PLAYERS, 1.5f, cut ? 0.7f : 1.3f);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
