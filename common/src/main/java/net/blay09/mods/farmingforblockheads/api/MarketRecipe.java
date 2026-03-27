package net.blay09.mods.farmingforblockheads.api;

import net.minecraft.world.item.ItemStackTemplate;

public interface MarketRecipe {
    Payment payment();
    ItemStackTemplate result();
}
