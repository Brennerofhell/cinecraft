package com.cinecraft.block;

import com.cinecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Film shelf storage: 18 slots (2 rows) shown with the vanilla chest GUI. */
public class FilmShelfBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOTS = 18;
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public FilmShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILM_SHELF.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.cinecraft.film_shelf");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    /** Only film material fits: Exposure film rolls (raw and developed) and CineCraft film items. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(FILM_ROLLS) || stack.is(DEVELOPED_FILM_ROLLS) || stack.is(CINECRAFT_FILM);
    }

    private static final TagKey<Item> FILM_ROLLS = tag("exposure:film_rolls");
    private static final TagKey<Item> DEVELOPED_FILM_ROLLS = tag("exposure:developed_film_rolls");
    private static final TagKey<Item> CINECRAFT_FILM = tag("cinecraft:shelf_storable");

    private static TagKey<Item> tag(String id) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ChestMenu(net.minecraft.world.inventory.MenuType.GENERIC_9x2, id, inventory, this, 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
