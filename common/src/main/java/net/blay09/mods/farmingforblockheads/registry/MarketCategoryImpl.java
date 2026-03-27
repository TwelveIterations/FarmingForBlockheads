package net.blay09.mods.farmingforblockheads.registry;

import net.blay09.mods.farmingforblockheads.api.MarketCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;

public record MarketCategoryImpl(ItemStackTemplate iconStack, int sortIndex, Component tooltip) implements MarketCategory {
}
