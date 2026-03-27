package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeImpl;
import net.blay09.mods.farmingforblockheads.registry.MarketDefaultsRegistry;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

public class ServerPlaceMarketRecipe {
    private final ServerPlaceRecipe.CraftingMenuAccess<MarketRecipeImpl> menuAccess;
    private final Slot paymentSlot;
    private final Inventory inventory;
    private final RecipeHolder<MarketRecipeImpl> recipeHolder;
    private final boolean useMaxItems;
    private final boolean creative;

    public ServerPlaceMarketRecipe(ServerPlaceRecipe.CraftingMenuAccess<MarketRecipeImpl> menuAccess, Slot paymentSlot, Inventory inventory, RecipeHolder<MarketRecipeImpl> recipeHolder, boolean useMaxItems, boolean creative) {
        this.menuAccess = menuAccess;
        this.paymentSlot = paymentSlot;
        this.recipeHolder = recipeHolder;
        this.inventory = inventory;
        this.useMaxItems = useMaxItems;
        this.creative = creative;
    }

    public static RecipeBookMenu.PostPlaceAction placeRecipe(
            ServerPlaceRecipe.CraftingMenuAccess<MarketRecipeImpl> access, Slot paymentSlot, Inventory inventory, RecipeHolder<MarketRecipeImpl> recipeHolder, boolean useMaxItems, boolean creative) {
        final var serverPlaceMarketRecipe = new ServerPlaceMarketRecipe(access, paymentSlot, inventory, recipeHolder, useMaxItems, creative);
        if (!creative && !serverPlaceMarketRecipe.testClearPayment()) {
            return RecipeBookMenu.PostPlaceAction.NOTHING;
        } else {
            final var stackedItemContents = new StackedItemContents();
            inventory.fillStackedContents(stackedItemContents);
            access.fillCraftSlotsStackedContents(stackedItemContents);
            return serverPlaceMarketRecipe.tryPlaceRecipe(recipeHolder, stackedItemContents);
        }
    }

    private RecipeBookMenu.PostPlaceAction tryPlaceRecipe(RecipeHolder<MarketRecipeImpl> recipeHolder, StackedItemContents stackedItemContents) {
        if (stackedItemContents.canCraft(recipeHolder.value(), null)) {
            placeRecipe(recipeHolder, stackedItemContents);
            inventory.setChanged();
            return RecipeBookMenu.PostPlaceAction.NOTHING;
        } else {
            clearPayment();
            inventory.setChanged();
            return RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE;
        }
    }

    private void placeRecipe(RecipeHolder<MarketRecipeImpl> recipeHolder, StackedItemContents stackedItemContents) {
        if (!menuAccess.recipeMatches(recipeHolder)) {
            clearPayment();
        }

        final var itemsToUse = new ArrayList<Holder<Item>>();
        if (stackedItemContents.canCraft(recipeHolder.value(), itemsToUse::add)) {
            final var effectivePayment = MarketDefaultsRegistry.resolvePayment(recipeHolder.value());
            var paymentSlotItem = paymentSlot.getItem();
            final var desiredCount = Math.min(paymentSlot.getMaxStackSize(), useMaxItems
                    ? stackedItemContents.getBiggestCraftableStack(recipeHolder.value(), null) * effectivePayment.count()
                    : paymentSlotItem.getCount() + effectivePayment.count());
            final var itemToUse = itemsToUse.getFirst();
            while (paymentSlotItem.getCount() < desiredCount) {
                final var desiredAddition = desiredCount - paymentSlotItem.getCount();
                final var foundSlot = inventory.findSlotMatchingCraftingIngredient(itemToUse, paymentSlotItem);
                if (foundSlot == -1) {
                    break;
                }

                final var slotItem = inventory.getItem(foundSlot);
                var toTake = Math.min(slotItem.getCount(), desiredAddition);
                if (paymentSlotItem.isEmpty()) {
                    paymentSlot.set(slotItem.split(toTake));
                    paymentSlotItem = paymentSlot.getItem();
                } else if (ItemStack.isSameItemSameComponents(paymentSlotItem, slotItem)) {
                    toTake = Math.min(toTake, paymentSlotItem.getMaxStackSize() - paymentSlotItem.getCount());
                    if (toTake <= 0) {
                        break;
                    }

                    slotItem.shrink(toTake);
                    paymentSlotItem.grow(toTake);
                } else {
                    break;
                }
            }
        }
    }

    private boolean testClearPayment() {
        final var restItem = paymentSlot.getItem().copy();
        if (restItem.isEmpty()) {
            return true;
        }

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            final var slotItem = inventory.getItem(i);
            if (slotItem.isEmpty()) {
                return true;
            } else if (ItemStack.isSameItemSameComponents(restItem, slotItem)) {
                final var availableSpace = slotItem.getMaxStackSize() - slotItem.getCount();
                if (availableSpace > 0) {
                    restItem.shrink(availableSpace);
                }
                if (restItem.isEmpty()) {
                    return true;
                }
            }
        }

        return restItem.isEmpty();
    }

    private void clearPayment() {
        final var itemstack = paymentSlot.getItem().copy();
        inventory.placeItemBackInInventory(itemstack, false);
        paymentSlot.set(itemstack);

        menuAccess.clearCraftingContent();
    }
}
