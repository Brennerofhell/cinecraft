package com.cinecraft.client;

import com.cinecraft.CineCraft;
import com.cinecraft.block.ProjectorBlockEntity;
import com.cinecraft.client.playback.FilmPlayback;
import com.cinecraft.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.UUID;

/**
 * Projector: GeckoLib model (spinning reels) plus the film picture. While the projector is on and holds a recorded
 * tape, the picture is drawn on the first projection screen in the direction it faces (the screen has to face back).
 */
public class ProjectorRenderer extends GeoBlockRenderer<ProjectorBlockEntity> {
    private static final int MAX_DISTANCE = 12;
    /** Depth of the screen block's front face from the block centre (screen is 3 pixels thick at the back). */
    private static final float FRONT_OFFSET = 0.5f - 3f / 16f + 0.003f;

    private boolean pictureFailed;

    public ProjectorRenderer() {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath(CineCraft.MOD_ID, "projector")));
    }

    @Override
    public void render(ProjectorBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        super.render(be, partialTick, poseStack, buffers, packedLight, packedOverlay);
        if (pictureFailed) return;
        try {
            drawPicture(be, poseStack, buffers);
        } catch (RuntimeException e) {
            // Never let a picture problem crash the render loop: log once and stop drawing pictures.
            pictureFailed = true;
            CineCraft.LOGGER.error("Projector picture rendering failed and was switched off", e);
        }
    }

    private void drawPicture(ProjectorBlockEntity be, PoseStack poseStack, MultiBufferSource buffers) {
        BlockState state = be.getBlockState();
        UUID film = be.filmId();
        Level level = be.getLevel();
        if (film == null || level == null || !state.getValue(BlockStateProperties.LIT)) return;

        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        BlockPos screen = findScreen(level, be.getBlockPos(), facing);
        if (screen == null) return;

        FilmPlayback.Frame frame = FilmPlayback.current(film);
        if (frame == null) return;

        // Picture size: fit into 0.92 x 0.92 blocks, keep the aspect ratio.
        float halfW = 0.46f;
        float halfH = 0.46f;
        if (frame.aspect() >= 1f) halfH = halfW / frame.aspect(); else halfW = halfH * frame.aspect();

        Direction front = facing.getOpposite(); // the screen's front faces the projector
        // Origin of the poseStack is the projector's block corner; move to the screen block's centre.
        float cx = screen.getX() - be.getBlockPos().getX() + 0.5f + front.getStepX() * FRONT_OFFSET;
        float cy = screen.getY() - be.getBlockPos().getY() + 0.5f;
        float cz = screen.getZ() - be.getBlockPos().getZ() + 0.5f + front.getStepZ() * FRONT_OFFSET;
        float rx = front.getStepZ(); // "right" as seen by a viewer in front of the screen
        float rz = -front.getStepX();

        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(frame.texture()));
        Matrix4f m = poseStack.last().pose();
        int light = LightTexture.FULL_BRIGHT;
        float nx = front.getStepX();
        float nz = front.getStepZ();
        quadVertex(vc, poseStack, m, cx - rx * halfW, cy + halfH, cz - rz * halfW, 0f, 0f, light, nx, nz);
        quadVertex(vc, poseStack, m, cx - rx * halfW, cy - halfH, cz - rz * halfW, 0f, 1f, light, nx, nz);
        quadVertex(vc, poseStack, m, cx + rx * halfW, cy - halfH, cz + rz * halfW, 1f, 1f, light, nx, nz);
        quadVertex(vc, poseStack, m, cx + rx * halfW, cy + halfH, cz + rz * halfW, 1f, 0f, light, nx, nz);
    }

    private static void quadVertex(VertexConsumer vc, PoseStack poseStack, Matrix4f m, float x, float y, float z,
                                   float u, float v, int light, float nx, float nz) {
        vc.addVertex(m, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(poseStack.last(), nx, 0f, nz);
    }

    /** First screen block in the projector's direction whose front faces the projector; solid blocks in between stop the beam. */
    private static BlockPos findScreen(Level level, BlockPos origin, Direction facing) {
        for (int d = 1; d <= MAX_DISTANCE; d++) {
            BlockPos pos = origin.relative(facing, d);
            BlockState s = level.getBlockState(pos);
            if (s.is(ModBlocks.SCREEN.get())) {
                return s.getValue(HorizontalDirectionalBlock.FACING) == facing.getOpposite() ? pos : null;
            }
            if (s.isSolidRender(level, pos)) return null;
        }
        return null;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(ProjectorBlockEntity be) {
        // The picture is drawn up to MAX_DISTANCE blocks away from the projector.
        return new net.minecraft.world.phys.AABB(be.getBlockPos()).inflate(MAX_DISTANCE);
    }
}
