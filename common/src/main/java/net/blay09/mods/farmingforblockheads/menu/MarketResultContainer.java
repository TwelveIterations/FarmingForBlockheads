package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MarketResultContainer extends ResultContainer {
    @Override
    public boolean setRecipeUsed(ServerPlayer player, RecipeHolder<?> recipeHolder) {
        if (recipeHolder.value() instanceof MarketRecipeImpl marketRecipe) {
            if (!marketRecipe.enabled()) {
                return false;
            }
        }

        return super.setRecipeUsed(player, recipeHolder);
    }
}
