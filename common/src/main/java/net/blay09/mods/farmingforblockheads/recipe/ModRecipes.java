package net.blay09.mods.farmingforblockheads.recipe;

import net.blay09.mods.balm.world.item.crafting.BalmRecipeTypeRegistrar;
import net.blay09.mods.balm.world.item.crafting.DeferredRecipeType;
import net.minecraft.world.item.crafting.RecipeInput;

public class ModRecipes {

    public static DeferredRecipeType<RecipeInput, MarketRecipe> marketRecipe;

    public static void initialize(BalmRecipeTypeRegistrar registry) {
        marketRecipe = registry.register("market", MarketRecipe.class)
                .withSerializer(MarketRecipe::serializer)
                .withRecipeBookCategory()
                .asDeferredRecipeType();
        registry.registerDisplayType("market", (id) -> MarketRecipeDisplay.TYPE);
    }
}
