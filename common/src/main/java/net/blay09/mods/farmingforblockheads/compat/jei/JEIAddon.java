package net.blay09.mods.farmingforblockheads.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.client.gui.screen.MarketScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIAddon implements IModPlugin {

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // TODO final var recipeManager = Minecraft.getInstance().level.getRecipeManager();
        // TODO final var marketRecipes = recipeManager.getAllRecipesFor(ModRecipes.marketRecipeType)
        // TODO         .stream()
        // TODO         .filter(recipe -> MarketPresetRegistry.isRecipeEnabled(recipe.value()))
        // TODO         .toList();
        // TODO registration.addRecipes(JeiMarketRecipeCategory.TYPE, marketRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.market), JeiMarketRecipeCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(MarketScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(MarketScreen screen) {
                List<Rect2i> list = new ArrayList<>();
                for (Button button : screen.getFilterButtons()) {
                    list.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
                }
                return list;
            }
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new JeiMarketRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "jei");
    }
}
