package com.cinecraft.registry;

import com.cinecraft.CineCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CineCraft.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cinecraft.main"))
                    .icon(() -> new ItemStack(ModItems.DIGITAL_CAMERA.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.DIGITAL_CAMERA.get());
                        output.accept(ModItems.MEMORY_CARD_SMALL.get());
                        output.accept(ModBlocks.TRIPOD.get());
                        output.accept(ModBlocks.SCREEN.get());
                        output.accept(ModItems.ANALOG_CAMERA.get());
                        output.accept(ModItems.FILM_ROLL.get());
                        output.accept(ModItems.FILM_CARTRIDGE.get());
                        output.accept(ModItems.LENS.get());
                        output.accept(ModItems.CLAPPERBOARD.get());
                        output.accept(ModBlocks.PROJECTOR.get());
                        output.accept(ModBlocks.DARKROOM_ENLARGER.get());
                        output.accept(ModBlocks.DARKROOM_SAFELIGHT.get());
                        output.accept(ModBlocks.DEVELOPER_TRAY.get());
                        output.accept(ModBlocks.DEVELOPER_STATION.get());
                        output.accept(ModBlocks.FILM_SHELF.get());
                        for (ModGearItems.Category category : ModGearItems.Category.values()) {
                            ModGearItems.BY_CATEGORY.getOrDefault(category, java.util.List.of())
                                    .forEach(item -> output.accept(item.get()));
                        }
                    })
                    .build());
}
