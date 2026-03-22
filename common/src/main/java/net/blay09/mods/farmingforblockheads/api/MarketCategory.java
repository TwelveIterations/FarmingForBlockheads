package net.blay09.mods.farmingforblockheads.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public interface MarketCategory extends Comparable<MarketCategory> {
	/**
	 * @return the language key for the tooltip of the category button
	 */
	Component tooltip();

	/**
	 * @return the item to use for the category icon
	 */
	ItemStackTemplate iconStack();

	int sortIndex();

	@Override
	default int compareTo(MarketCategory o) {
		return Integer.compare(sortIndex(), o.sortIndex());
	}
}
