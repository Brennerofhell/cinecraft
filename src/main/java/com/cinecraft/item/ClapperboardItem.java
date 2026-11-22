package com.cinecraft.item;

import com.cinecraft.client.ClapperboardRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/** Clapperboard: right-click claps and counts the take; sneak+right-click starts a new scene. */
public class ClapperboardItem extends Item implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.clapperboard.idle");
    private static final RawAnimation CLAP = RawAnimation.begin().thenPlay("animation.clapperboard.clap");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ClapperboardItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (player.isShiftKeyDown()) {
                tag.putInt("take", 0);
                tag.putInt("scene", tag.getInt("scene") + 1);
                player.displayClientMessage(Component.translatable("item.cinecraft.clapperboard.new_scene",
                        tag.getInt("scene")), true);
            } else {
                int take = tag.getInt("take") + 1;
                int scene = Math.max(1, tag.getInt("scene"));
                tag.putInt("take", take);
                tag.putInt("scene", scene);
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "main", "clap");
                level.playSound(null, player.blockPosition(), SoundEvents.WOODEN_DOOR_CLOSE,
                        SoundSource.PLAYERS, 0.8f, 1.6f);
                player.displayClientMessage(Component.translatable("item.cinecraft.clapperboard.take", scene, take), true);
            }
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        if (level.isClientSide() && !player.isShiftKeyDown()) {
            var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            SlateMarks.clap(Math.max(1, tag.getInt("scene")), tag.getInt("take") + 1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ClapperboardRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new ClapperboardRenderer();
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> state.setAndContinue(IDLE))
                .triggerableAnim("clap", CLAP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
