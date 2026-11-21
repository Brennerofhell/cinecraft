package com.cinecraft.item;

import com.cinecraft.client.GearItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * Item with a GeckoLib model: loops {@code animation.<id>.idle} and plays {@code animation.<id>.<action>}
 * once when triggered (right-click, or by {@link com.cinecraft.compat.ExposureCompat}).
 */
public class GearGeoItem extends Item implements GeoItem {
    public static final String CONTROLLER = "main";
    public static final String TRIGGER = "action";

    private final String id;
    private final String action;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GearGeoItem(Properties properties, String id, String action) {
        super(properties);
        this.id = id;
        this.action = action;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public String id() {
        return id;
    }

    /** Server side: plays the action animation of this stack for everyone tracking {@code entity}. */
    public void playAction(Entity entity, ItemStack stack) {
        if (entity.level() instanceof ServerLevel level) {
            triggerAnim(entity, GeoItem.getOrAssignId(stack, level), CONTROLLER, TRIGGER);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        playAction(player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GearItemRenderer<GearGeoItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new GearItemRenderer<>(id);
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        RawAnimation idle = RawAnimation.begin().thenLoop("animation." + id + ".idle");
        RawAnimation act = RawAnimation.begin().thenPlay("animation." + id + "." + action);
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, state -> state.setAndContinue(idle))
                .triggerableAnim(TRIGGER, act));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
