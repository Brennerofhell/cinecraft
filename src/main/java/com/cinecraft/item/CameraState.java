package com.cinecraft.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/** Small per-stack camera state (lens cap, battery charge) stored in the stack's custom data. */
public final class CameraState {
    private static final String CAP = "cinecraft_lens_cap";
    private static final String CHARGE = "cinecraft_battery";
    private static final String CAGE = "cinecraft_cage";
    public static final int MAX_CHARGE = 100;
    /** Charge limit with a battery grip in the inventory. */
    public static final int GRIP_MAX_CHARGE = 200;

    private CameraState() {}

    public static boolean hasLensCap(ItemStack camera) {
        return data(camera).getBoolean(CAP);
    }

    public static void setLensCap(ItemStack camera, boolean on) {
        update(camera, tag -> tag.putBoolean(CAP, on));
    }

    public static boolean hasCage(ItemStack camera) {
        return data(camera).getBoolean(CAGE);
    }

    public static void setCage(ItemStack camera, boolean on) {
        update(camera, tag -> tag.putBoolean(CAGE, on));
    }

    public static int charge(ItemStack camera) {
        return data(camera).getInt(CHARGE);
    }

    public static void setCharge(ItemStack camera, int charge) {
        int c = Math.max(0, Math.min(GRIP_MAX_CHARGE, charge));
        update(camera, tag -> tag.putInt(CHARGE, c));
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void update(ItemStack stack, Consumer<CompoundTag> change) {
        CompoundTag tag = data(stack);
        change.accept(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
