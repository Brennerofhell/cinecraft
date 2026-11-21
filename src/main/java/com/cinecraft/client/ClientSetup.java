package com.cinecraft.client;

import com.cinecraft.CineCraft;
import com.cinecraft.registry.ModBlockEntities;
import com.cinecraft.registry.ModItems;
import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderRegistry;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = CineCraft.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PROJECTOR.get(), ctx -> new ProjectorRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.DEVELOPER_STATION.get(), ctx -> new DeveloperStationRenderer());
    }

    /** Exposure needs a viewfinder per camera item on the client, otherwise using the camera crashes. */
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        if (net.neoforged.fml.ModList.get().isLoaded("ponder")) {
            event.enqueueWork(com.cinecraft.compat.PonderCompat::register);
        }
        event.enqueueWork(() -> {
            for (var camera : java.util.List.of(ModItems.ANALOG_CAMERA, ModItems.DIGITAL_CAMERA)) {
                ViewfinderRegistry.register((CameraItem) camera.get(), Viewfinder::new);
            }
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(com.cinecraft.registry.ModMenus.DEVELOPER_STATION.get(), DeveloperStationScreen::new);
    }
}
