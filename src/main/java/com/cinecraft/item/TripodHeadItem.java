package com.cinecraft.item;

import com.cinecraft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Tripod head: use it on a Tripod to turn it a quarter turn (sneak to turn the other way). */
public class TripodHeadItem extends Item {
    public TripodHeadItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.TRIPOD.get())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            var facing = state.getValue(HorizontalDirectionalBlock.FACING);
            var next = context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                    ? facing.getCounterClockWise() : facing.getClockWise();
            level.setBlock(pos, state.setValue(HorizontalDirectionalBlock.FACING, next), 3);
            level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.6f, 1.2f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
