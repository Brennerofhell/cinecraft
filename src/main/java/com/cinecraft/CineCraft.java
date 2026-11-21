package com.cinecraft;

import com.cinecraft.config.ModConfig;
import com.cinecraft.registry.ModBlocks;
import com.cinecraft.registry.ModCreativeTabs;
import com.cinecraft.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(CineCraft.MOD_ID)
public class CineCraft {
    public static final String MOD_ID = "cinecraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CineCraft(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        com.cinecraft.registry.ModBlockEntities.TYPES.register(modBus);
        com.cinecraft.registry.ModDataComponents.COMPONENTS.register(modBus);
        ModItems.ITEMS.register(modBus);
        modBus.addListener(com.cinecraft.network.FilmNetwork::register);
        com.cinecraft.registry.ModMenus.MENUS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(com.cinecraft.compat.ExposureCompat.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(com.cinecraft.compat.StrapEvents.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(com.cinecraft.network.FilmNetwork.class);

        container.registerConfig(Type.SERVER, ModConfig.SERVER_SPEC);
        container.registerConfig(Type.CLIENT, ModConfig.CLIENT_SPEC);
        LOGGER.info("CineCraft initialised");
    }
}
