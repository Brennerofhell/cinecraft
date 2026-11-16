package com.cinecraft.registry;

import com.cinecraft.CineCraft;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CineCraft.MOD_ID);

    // Placeholder-free: simple items without behaviour yet. Camera logic arrives in Phase 3.
    public static final DeferredItem<Item> DIGITAL_CAMERA = ITEMS.registerSimpleItem("digital_camera",
            new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MEMORY_CARD_SMALL = ITEMS.registerSimpleItem("memory_card_small",
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> ANALOG_CAMERA = ITEMS.registerSimpleItem("analog_camera",
            new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> FILM_ROLL = ITEMS.registerSimpleItem("film_roll",
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> FILM_CARTRIDGE = ITEMS.registerSimpleItem("film_cartridge",
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> LENS = ITEMS.registerSimpleItem("lens",
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> CLAPPERBOARD = ITEMS.registerItem("clapperboard",
            com.cinecraft.item.ClapperboardItem::new, new Item.Properties().stacksTo(1));

    static {
        ModBlocks.registerBlockItems(ITEMS);
    }
}
