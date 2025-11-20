package net.blay09.mods.farmingforblockheads;

import net.blay09.mods.farmingforblockheads.api.MarketCategory;
import net.blay09.mods.farmingforblockheads.api.InternalMethods;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.registry.MarketCategoryRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Map;
import java.util.Optional;

public class InternalMethodsImpl implements InternalMethods {
    @Override
    public Optional<MarketCategory> getMarketCategory(Identifier registryName) {
        return MarketCategoryRegistry.INSTANCE.get(registryName);
    }

    @Override
    public RecipeType<?> getMarketRecipeType() {
        return ModRecipes.marketRecipe.type();
    }

    @Override
    public Map<Identifier, MarketCategory> getMarketCategories() {
        return MarketCategoryRegistry.INSTANCE.getAll();
    }
}
