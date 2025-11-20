package net.blay09.mods.farmingforblockheads.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Map;
import java.util.Optional;

public interface InternalMethods {
    Optional<MarketCategory> getMarketCategory(Identifier id);

    RecipeType<?> getMarketRecipeType();

    Map<Identifier, MarketCategory> getMarketCategories();
}
