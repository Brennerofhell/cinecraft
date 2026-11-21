package com.cinecraft.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

/**
 * Camera backpack: right-click opens 9 slots for photography gear (film, cards, filters, lenses, flashes, batteries and
 * other CineCraft or Exposure items). The contents live in the item's container component.
 */
public class BackpackItem extends Item {
    public static final int SLOTS = 9;

    public BackpackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack backpack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            SimpleContainer container = new SimpleContainer(SLOTS) {
                @Override
                public boolean canPlaceItem(int slot, ItemStack stack) {
                    return isGear(stack);
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    save(backpack, this);
                }
            };
            ItemContainerContents contents = backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
            contents.copyInto(items);
            for (int i = 0; i < SLOTS; i++) container.setItem(i, items.get(i));
            // setItem above triggered saves of a half-loaded container; write the full state back now.
            save(backpack, container);
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new ChestMenu(MenuType.GENERIC_9x1, id, inventory, container, 1) {
                        @Override
                        public boolean stillValid(Player user) {
                            return user.getInventory().contains(backpack) || user.getMainHandItem() == backpack
                                    || user.getOffhandItem() == backpack;
                        }
                    },
                    Component.translatable("item.cinecraft.camera_backpack")));
        }
        return InteractionResultHolder.sidedSuccess(backpack, level.isClientSide());
    }

    private static boolean isGear(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem) return false;
        String ns = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        return ns.equals("cinecraft") || ns.equals("exposure");
    }

    private static void save(ItemStack backpack, SimpleContainer container) {
        backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
    }
}
