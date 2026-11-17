package com.cinecraft.registry;

import com.cinecraft.CineCraft;
import com.cinecraft.menu.DeveloperStationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CineCraft.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DeveloperStationMenu>> DEVELOPER_STATION =
            MENUS.register("developer_station",
                    () -> new MenuType<>(DeveloperStationMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
