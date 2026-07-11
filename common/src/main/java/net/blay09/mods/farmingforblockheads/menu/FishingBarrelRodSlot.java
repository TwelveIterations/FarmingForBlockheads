package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.farmingforblockheads.block.entity.FishingBarrelBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FishingBarrelRodSlot extends Slot {
    public FishingBarrelRodSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return FishingBarrelBlockEntity.isFishingRod(itemStack);
    }
}
