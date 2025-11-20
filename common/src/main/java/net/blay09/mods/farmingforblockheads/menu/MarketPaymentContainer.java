package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.balm.world.DefaultContainer;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class MarketPaymentContainer extends DefaultContainer implements StackedContentsCompatible {
    private final MarketMenu menu;

    public MarketPaymentContainer(MarketMenu menu, int size) {
        super(size);
        this.menu = menu;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        final var itemStack = super.removeItem(slot, count);
        if (!itemStack.isEmpty()) {
            menu.slotsChanged(this);
        }
        return itemStack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        menu.slotsChanged(this);
    }

    @Override
    public void fillStackedContents(StackedItemContents stackedItemContents) {
        for (final var itemStack : getItems()) {
            stackedItemContents.accountSimpleStack(itemStack);
        }
    }

    public RecipeInput asRecipeInput() {
        return new SingleRecipeInput(getItem(0));
    }

    public void removePayment(int slot) {
        final var expectedPayment = menu.getExpectedPayment();
        removeItem(slot, expectedPayment.map(Payment::count).orElse(1));
    }
}
