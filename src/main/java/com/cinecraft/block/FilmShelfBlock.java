package com.cinecraft.block;

import com.cinecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Film shelf: right-click opens an 18-slot storage; contents drop when the block is broken. */
public class FilmShelfBlock extends ShapedBlock implements EntityBlock {
    public FilmShelfBlock(Properties properties) {
        super(properties, pixelBox(0, 0, 0, 16, 16, 16));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.FILM_SHELF.get().create(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof FilmShelfBlockEntity shelf) {
            player.openMenu(shelf);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FilmShelfBlockEntity shelf) {
            Containers.dropContents(level, pos, shelf);
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
