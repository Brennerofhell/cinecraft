package com.cinecraft.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Accessory, filter, lab, production and component items. Plain items without behaviour yet;
 * registered into {@link ModItems#ITEMS} and grouped by {@link Category} for the creative tab.
 */
public final class ModGearItems {
    private ModGearItems() {}

    public enum Category { ACCESSORY, FILTER, LAB, PRODUCTION, COMPONENT }

    public static final Map<Category, List<DeferredItem<Item>>> BY_CATEGORY = new EnumMap<>(Category.class);

    private static void add(DeferredRegister.Items items, Category category, String id, int stackSize) {
        DeferredItem<Item> item = items.registerSimpleItem(id, new Item.Properties().stacksTo(stackSize));
        BY_CATEGORY.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
    }

    static void register(DeferredRegister.Items items) {
        add(items, Category.ACCESSORY, "camera_flash", 1);
        add(items, Category.ACCESSORY, "camera_strap", 1);
        add(items, Category.ACCESSORY, "camera_battery", 16);
        add(items, Category.ACCESSORY, "battery_grip", 1);
        add(items, Category.ACCESSORY, "remote_shutter", 1);
        add(items, Category.ACCESSORY, "lens_cap", 1);
        add(items, Category.ACCESSORY, "lens_hood", 1);
        add(items, Category.ACCESSORY, "camera_cage", 1);
        add(items, Category.ACCESSORY, "camera_backpack", 1);
        add(items, Category.ACCESSORY, "tripod_head", 1);
        add(items, Category.FILTER, "uv_filter", 1);
        add(items, Category.FILTER, "polarizing_filter", 1);
        add(items, Category.FILTER, "nd_filter", 1);
        add(items, Category.FILTER, "variable_nd_filter", 1);
        add(items, Category.FILTER, "mist_filter", 1);
        add(items, Category.FILTER, "star_filter", 1);
        add(items, Category.LAB, "photo_paper", 64);
        add(items, Category.LAB, "developer_bottle", 16);
        add(items, Category.LAB, "fixer_bottle", 16);
        add(items, Category.LAB, "developing_tank", 1);
        add(items, Category.LAB, "negative_strip", 16);
        add(items, Category.LAB, "photo_print", 16);
        add(items, Category.LAB, "darkroom_tongs", 1);
        add(items, Category.PRODUCTION, "shotgun_microphone", 1);
        add(items, Category.PRODUCTION, "wireless_microphone", 1);
        add(items, Category.PRODUCTION, "audio_recorder", 1);
        add(items, Category.PRODUCTION, "directors_megaphone", 1);
        add(items, Category.PRODUCTION, "film_reel", 16);
        add(items, Category.PRODUCTION, "video_tape", 16);
        add(items, Category.PRODUCTION, "timecode_generator", 1);
        add(items, Category.PRODUCTION, "follow_focus", 1);
        add(items, Category.COMPONENT, "image_sensor", 64);
        add(items, Category.COMPONENT, "image_processor", 64);
        add(items, Category.COMPONENT, "autofocus_module", 64);
        add(items, Category.COMPONENT, "optical_glass", 64);
        add(items, Category.COMPONENT, "precision_lens", 64);
        add(items, Category.COMPONENT, "shutter_mechanism", 64);
        add(items, Category.COMPONENT, "camera_circuit", 64);
        add(items, Category.COMPONENT, "stabilization_module", 64);
    }
}
