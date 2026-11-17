package com.cinecraft.menu;

import com.cinecraft.registry.ModBlocks;
import com.cinecraft.registry.ModMenus;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;

/**
 * Developer station menu: three input slots and one result slot. The result comes from the crafting recipes
 * (3x1 input), which includes the recipes of type {@code exposure:film_developing}. No own developing logic.
 */
public class DeveloperStationMenu extends AbstractContainerMenu {
    public static final int INPUTS = 3;
    private final Container inputs;
    private final SimpleContainer result = new SimpleContainer(1);
    private final ContainerLevelAccess access;
    private final Player player;

    /** Client side constructor. */
    public DeveloperStationMenu(int id, Inventory playerInv) {
        this(id, playerInv, new SimpleContainer(INPUTS), ContainerLevelAccess.NULL);
    }

    public DeveloperStationMenu(int id, Inventory playerInv, Container inputs, ContainerLevelAccess access) {
        super(ModMenus.DEVELOPER_STATION.get(), id);
        this.inputs = inputs;
        this.access = access;
        this.player = playerInv.player;
        checkContainerSize(inputs, INPUTS);

        for (int i = 0; i < INPUTS; i++) {
            addSlot(new Slot(inputs, i, 62 + i * 18, 35) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    slotsChanged(inputs);
                }
            });
        }
        addSlot(new Slot(result, 0, 134, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player taker, ItemStack stack) {
                takeResult(taker);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
        updateResult();
    }

    private CraftingInput input() {
        List<ItemStack> items = List.of(inputs.getItem(0), inputs.getItem(1), inputs.getItem(2));
        return CraftingInput.of(INPUTS, 1, items);
    }

    private Optional<RecipeHolder<CraftingRecipe>> recipe() {
        if (player.level().isClientSide) return Optional.empty();
        return player.level().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input(), player.level());
    }

    private void updateResult() {
        if (player.level().isClientSide) return;
        ItemStack out = recipe().map(r -> r.value().assemble(input(), player.level().registryAccess()))
                .orElse(ItemStack.EMPTY);
        result.setItem(0, out);
        broadcastChanges();
    }

    private void takeResult(Player taker) {
        Optional<RecipeHolder<CraftingRecipe>> recipe = recipe();
        if (recipe.isEmpty()) return;
        CraftingInput in = input();
        NonNullList<ItemStack> remaining = recipe.get().value().getRemainingItems(in);
        for (int i = 0; i < INPUTS; i++) {
            ItemStack slot = inputs.getItem(i);
            ItemStack rest = remaining.get(i);
            if (!slot.isEmpty()) {
                inputs.removeItem(i, 1);
                slot = inputs.getItem(i);
            }
            if (!rest.isEmpty()) {
                if (slot.isEmpty()) {
                    inputs.setItem(i, rest);
                } else if (ItemStack.isSameItemSameComponents(slot, rest)) {
                    rest.grow(slot.getCount());
                    inputs.setItem(i, rest);
                } else if (!taker.getInventory().add(rest)) {
                    taker.drop(rest, false);
                }
            }
        }
        updateResult();
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == inputs) updateResult();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            moved = stack.copy();
            int playerStart = INPUTS + 1;
            int end = slots.size();
            if (index == INPUTS) {
                ItemStack copy = stack.copy();
                if (!moveItemStackTo(stack, playerStart, end, true)) return ItemStack.EMPTY;
                takeResult(player);
                slot.onQuickCraft(stack, moved);
                result.setItem(0, ItemStack.EMPTY);
                updateResult();
                return copy;
            } else if (index < playerStart) {
                if (!moveItemStackTo(stack, playerStart, end, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, INPUTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.DEVELOPER_STATION.get());
    }
}
