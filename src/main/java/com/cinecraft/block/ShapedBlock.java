package com.cinecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Block with a horizontal FACING (model front = north, faces the player on placement)
 * and a hitbox given in north-facing pixel coordinates (0..16).
 */
public class ShapedBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ShapedBlock> CODEC = simpleCodec(p -> new ShapedBlock(p, new double[] {0, 0, 0, 16, 16, 16}));

    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public ShapedBlock(Properties properties, double[] box) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        for (Direction d : Direction.Plane.HORIZONTAL) shapes.put(d, rotated(box, d));
    }

    /** Box {x1,y1,z1,x2,y2,z2} in north-facing pixels. */
    public static double[] pixelBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[] {x1, y1, z1, x2, y2, z2};
    }

    public static VoxelShape rotated(double[] b, Direction d) {
        double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
        return switch (d) {
            case EAST -> Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
            case SOUTH -> Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case WEST -> Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapes.getOrDefault(state.getValue(FACING), Shapes.block());
    }
}
