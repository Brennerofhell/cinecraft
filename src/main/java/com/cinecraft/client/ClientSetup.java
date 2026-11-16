package com.cinecraft.client;

import com.cinecraft.CineCraft;
import com.cinecraft.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CineCraft.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PROJECTOR.get(), ctx -> new ProjectorRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.DEVELOPER_STATION.get(), ctx -> new DeveloperStationRenderer());
    }
}
