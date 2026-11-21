package com.cinecraft.block;

import com.cinecraft.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Projector rendered by GeckoLib (animated reels and crank), with horizontal FACING. */
public class ProjectorBlock extends BaseEntityBlock {
    public static final MapCodec<ProjectorBlock> CODEC = simpleCodec(ProjectorBlock::new);
    private static final double[] BOX = ShapedBlock.pixelBox(1.5, 0, 0, 15, 16, 13);

    public ProjectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(BlockStateProperties.LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING, BlockStateProperties.LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.LIT, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.PROJECTOR.get().create(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return ShapedBlock.rotated(BOX, state.getValue(HorizontalDirectionalBlock.FACING));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    /** Right-click with a recorded video tape inserts it (if the projector holds none yet). */
    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state,
            Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof com.cinecraft.item.VideoTapeItem
                && stack.has(com.cinecraft.registry.ModDataComponents.FILM_ID.get())
                && level.getBlockEntity(pos) instanceof ProjectorBlockEntity projector
                && projector.tape().isEmpty()) {
            if (!level.isClientSide) {
                projector.setTape(stack.copyWithCount(1));
                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.6f, 1.4f);
            }
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Right-click toggles the projector on/off; sneaking with an empty hand takes the tape out. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof ProjectorBlockEntity projector
                    && !projector.tape().isEmpty()) {
                player.getInventory().placeItemBackInInventory(projector.tape());
                projector.setTape(net.minecraft.world.item.ItemStack.EMPTY);
            } else {
                setLit(level, pos, state, !state.getValue(BlockStateProperties.LIT));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Redstone power switches the projector on, losing power switches it off. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moved) {
        if (level.isClientSide) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(BlockStateProperties.LIT)) {
            setLit(level, pos, state, powered);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ProjectorBlockEntity projector) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), projector.tape());
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    private static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), Block.UPDATE_ALL);
        level.playSound(null, pos, lit ? SoundEvents.LEVER_CLICK : SoundEvents.COMPARATOR_CLICK,
                SoundSource.BLOCKS, 0.6f, lit ? 1.0f : 0.8f);
    }
}
