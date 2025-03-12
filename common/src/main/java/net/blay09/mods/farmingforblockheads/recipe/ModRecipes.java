package net.blay09.mods.farmingforblockheads.recipe;

import net.blay09.mods.balm.api.recipe.BalmRecipes;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModRecipes {

    public static RecipeBookCategory marketRecipeBookCategory;
    public static RecipeType<MarketRecipe> marketRecipeType;
    public static RecipeSerializer<MarketRecipe> marketRecipeSerializer;

    public static void initialize(BalmRecipes registry) {
        registry.registerRecipeBookCategory(() -> marketRecipeBookCategory = new RecipeBookCategory(),
                ResourceLocation.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "market"));
        registry.registerRecipeDisplayType(() -> MarketRecipeDisplay.TYPE, ResourceLocation.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "market"));
        registry.registerRecipeType((identifier) -> marketRecipeType = new RecipeType<>() {
                    @Override
                    public String toString() {
                        return identifier.getPath();
                    }
                }, id("market"));
        registry.registerRecipeSerializer(() -> marketRecipeSerializer = new MarketRecipe.Serializer(), id("market"));
    }
}
