package net.blay09.mods.farmingforblockheads.recipe;

import net.blay09.mods.balm.world.item.crafting.BalmRecipeTypeRegistrar;
import net.blay09.mods.balm.world.item.crafting.DeferredRecipeType;
import net.minecraft.world.item.crafting.RecipeInput;

public class ModRecipes {

    public static DeferredRecipeType<RecipeInput, MarketRecipeImpl> marketRecipe;

    public static void initialize(BalmRecipeTypeRegistrar registry) {
        marketRecipe = registry.register("market", MarketRecipeImpl.class)
                .withSerializer(MarketRecipeImpl::serializer)
                .withRecipeBookCategory()
                .asDeferredRecipeType();
        registry.registerDisplayType("market", (id) -> MarketRecipeDisplay.TYPE);
    }
}
