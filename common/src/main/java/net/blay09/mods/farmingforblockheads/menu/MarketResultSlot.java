package net.blay09.mods.farmingforblockheads.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MarketResultSlot extends Slot {

    private final MarketPaymentContainer paymentSlots;
    private final Player player;
    private int removeCount;

    public MarketResultSlot(Player player, MarketPaymentContainer paymentSlots, Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
        this.player = player;
        this.paymentSlots = paymentSlots;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (hasItem()) {
            removeCount += Math.min(amount, getItem().getCount());
        }

        return super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack itemStack, int amount) {
        this.removeCount += amount;
        checkTakeAchievements(itemStack);
    }

    @Override
    protected void onSwapCraft(int itemsCrafted) {
        removeCount += itemsCrafted;
    }

    protected void checkTakeAchievements(ItemStack itemStack) {
        if (removeCount > 0) {
            itemStack.onCraftedBy(player, removeCount);
        }

        if (container instanceof RecipeCraftingHolder recipeCraftingHolder) {
            recipeCraftingHolder.awardUsedRecipes(player, paymentSlots.getItems());
        }

        removeCount = 0;
    }

    @Override
    public void onTake(Player player, ItemStack itemStack) {
        checkTakeAchievements(itemStack);

        final var remainingStack = ItemStack.EMPTY; // Not used atm. Keeping this in for now in case we want to use it later.
        final var paymentSlot = 0;
        var paymentStack = paymentSlots.getItem(paymentSlot);
        if (!paymentStack.isEmpty()) {
            paymentSlots.removePayment(paymentSlot);
            paymentStack = this.paymentSlots.getItem(paymentSlot);
        }

        if (!remainingStack.isEmpty()) {
            if (paymentStack.isEmpty()) {
                paymentSlots.setItem(paymentSlot, remainingStack);
            } else if (ItemStack.isSameItemSameComponents(paymentStack, remainingStack)) {
                remainingStack.grow(paymentStack.getCount());
                paymentSlots.setItem(paymentSlot, remainingStack);
            } else if (!player.getInventory().add(remainingStack)) {
                player.drop(remainingStack, false);
            }
        }
    }

    @Override
    public boolean isFake() {
        return true;
    }
}
