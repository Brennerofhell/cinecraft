package com.cinecraft.block;

import com.cinecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ProjectorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.projector.run");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROJECTOR.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            var be = state.getAnimatable();
            if (be.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                return state.setAndContinue(RUN);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
