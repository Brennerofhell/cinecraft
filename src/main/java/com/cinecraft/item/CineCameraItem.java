package com.cinecraft.item;

import com.cinecraft.client.GearItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
 * Working Exposure camera (extends Exposure's CameraItem: viewfinder, film, shutter, attachments) with a GeckoLib model.
 * Plays {@code animation.<id>.<action>} when Exposure finishes a photo ({@link com.cinecraft.compat.ExposureCompat}).
 */
public class CineCameraItem extends io.github.mortuusars.exposure.world.item.camera.CameraItem implements GeoItem {
    public static final String CONTROLLER = "main";
    public static final String TRIGGER = "action";

    private final String id;
    private final String action;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CineCameraItem(Properties properties, String id, String action) {
        super(properties);
        this.id = id;
        this.action = action;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    /**
     * Whether the loaded film medium fits this camera: the analog camera takes film rolls, the digital camera
     * memory cards. (Exposure's attachment menu looks slots up by the original Attachment objects, so the slot
     * itself cannot be restricted without breaking the menu; the shutter is blocked instead.)
     */
    protected boolean acceptsMedium(boolean isMemoryCard) {
        return !isMemoryCard;
    }

    private boolean mediumFits(ItemStack camera) {
        ItemStack film = getFilmAttachment().get(camera).getForReading();
        if (film.isEmpty()) return true; // empty slot: Exposure reports "no film" itself
        return acceptsMedium(film.getItem() instanceof MemoryCardItem);
    }

    /** A caged camera recovers twice as fast after a shot. */
    @Override
    public int calculateCooldownAfterShot(ItemStack stack,
                                          io.github.mortuusars.exposure.world.camera.capture.CaptureParameters params) {
        int cooldown = super.calculateCooldownAfterShot(stack, params);
        return CameraState.hasCage(stack) ? cooldown / 2 : cooldown;
    }

    /** A camera with the lens cap on cannot shoot. */
    @Override
    public boolean canTakePhoto(io.github.mortuusars.exposure.world.entity.CameraHolder holder, ItemStack stack) {
        return !CameraState.hasLensCap(stack) && mediumFits(stack) && super.canTakePhoto(holder, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!mediumFits(stack)) {
            tooltip.add(net.minecraft.network.chat.Component.translatable(acceptsMedium(true)
                    ? "item.cinecraft.camera.needs_card" : "item.cinecraft.camera.needs_film")
                    .withStyle(net.minecraft.ChatFormatting.RED));
        }
        if (CameraState.hasCage(stack)) {
            tooltip.add(net.minecraft.network.chat.Component.translatable("item.cinecraft.camera_cage.tooltip")
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
        if (CameraState.hasLensCap(stack)) {
            tooltip.add(net.minecraft.network.chat.Component.translatable("item.cinecraft.lens_cap.tooltip")
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
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
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GearItemRenderer<CineCameraItem> renderer;

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
