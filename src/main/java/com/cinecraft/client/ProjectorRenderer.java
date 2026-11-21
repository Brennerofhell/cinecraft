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
 * tape, the picture is drawn on the projection screen in the direction it faces. Screen blocks that sit next to each
 * other in one plane form a single big screen (the biggest full rectangle around the block that was hit).
 */
public class ProjectorRenderer extends GeoBlockRenderer<ProjectorBlockEntity> {
    private static final int MAX_DISTANCE = 12;
    private static final int MAX_SCREEN_BLOCKS = 15;
    /** Depth of the screen block's front face from the block centre (screen is 3 pixels thick at the back). */
    private static final float FRONT_OFFSET = 0.5f - 3f / 16f + 0.003f;
    private static final float MARGIN = 0.08f;

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
            drawPicture(be, partialTick, poseStack, buffers);
        } catch (RuntimeException e) {
            // Never let a picture problem crash the render loop: log once and stop drawing pictures.
            pictureFailed = true;
            CineCraft.LOGGER.error("Projector picture rendering failed and was switched off", e);
        }
    }

    private void drawPicture(ProjectorBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        BlockState state = be.getBlockState();
        UUID film = be.filmId();
        Level level = be.getLevel();
        if (film == null || level == null || !state.getValue(BlockStateProperties.LIT)) return;

        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        Direction front = facing.getOpposite(); // the screen's front faces the projector
        BlockPos hit = findScreen(level, be.getBlockPos(), facing, front);
        if (hit == null) return;

        // Same moment for every player: derive the film position from the world time.
        long millis = (long) ((level.getGameTime() + partialTick) * 50f);
        FilmPlayback.Frame frame = FilmPlayback.current(film, millis);
        if (frame == null) return;

        // "right" as seen by a viewer in front of the screen
        int rx = front.getStepZ();
        int rz = -front.getStepX();

        // Grow to the full rectangle of screen blocks around the hit block.
        int left = 0;
        int right = 0;
        while (left < MAX_SCREEN_BLOCKS && isScreen(level, hit.offset(-rx * (left + 1), 0, -rz * (left + 1)), front)) left++;
        while (right < MAX_SCREEN_BLOCKS && isScreen(level, hit.offset(rx * (right + 1), 0, rz * (right + 1)), front)) right++;
        int down = 0;
        int up = 0;
        while (down < MAX_SCREEN_BLOCKS && rowIsScreen(level, hit, -(down + 1), left, right, rx, rz, front)) down++;
        while (up < MAX_SCREEN_BLOCKS && rowIsScreen(level, hit, up + 1, left, right, rx, rz, front)) up++;

        float screenW = left + right + 1;
        float screenH = down + up + 1;
        // Fit the picture into the screen, keep the aspect ratio, leave a small margin.
        float halfW = (screenW - MARGIN) / 2f;
        float halfH = halfW / frame.aspect();
        if (halfH > (screenH - MARGIN) / 2f) {
            halfH = (screenH - MARGIN) / 2f;
            halfW = halfH * frame.aspect();
        }

        // Origin of the poseStack is the projector's block corner; move to the centre of the screen rectangle.
        float cx = hit.getX() - be.getBlockPos().getX() + 0.5f + rx * (right - left) / 2f + front.getStepX() * FRONT_OFFSET;
        float cy = hit.getY() - be.getBlockPos().getY() + 0.5f + (up - down) / 2f;
        float cz = hit.getZ() - be.getBlockPos().getZ() + 0.5f + rz * (right - left) / 2f + front.getStepZ() * FRONT_OFFSET;

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

    private static boolean isScreen(Level level, BlockPos pos, Direction front) {
        BlockState s = level.getBlockState(pos);
        return s.is(ModBlocks.SCREEN.get()) && s.getValue(HorizontalDirectionalBlock.FACING) == front;
    }

    /** True if the whole row {@code dy} blocks above (or below) the hit block, between left and right, is screen. */
    private static boolean rowIsScreen(Level level, BlockPos hit, int dy, int left, int right, int rx, int rz, Direction front) {
        for (int i = -left; i <= right; i++) {
            if (!isScreen(level, hit.offset(rx * i, dy, rz * i), front)) return false;
        }
        return true;
    }

    /** First screen block in the projector's direction whose front faces the projector; solid blocks in between stop the beam. */
    private static BlockPos findScreen(Level level, BlockPos origin, Direction facing, Direction front) {
        for (int d = 1; d <= MAX_DISTANCE; d++) {
            BlockPos pos = origin.relative(facing, d);
            BlockState s = level.getBlockState(pos);
            if (s.is(ModBlocks.SCREEN.get())) {
                return s.getValue(HorizontalDirectionalBlock.FACING) == front ? pos : null;
            }
            if (s.isSolidRender(level, pos)) return null;
        }
        return null;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(ProjectorBlockEntity be) {
        // The picture is drawn up to MAX_DISTANCE blocks away from the projector, and screens can be wide.
        return new net.minecraft.world.phys.AABB(be.getBlockPos()).inflate(MAX_DISTANCE + MAX_SCREEN_BLOCKS);
    }
}
