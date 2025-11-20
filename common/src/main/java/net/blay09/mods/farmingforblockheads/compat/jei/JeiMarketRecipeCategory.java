package net.blay09.mods.farmingforblockheads.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipe;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.registry.MarketDefaultsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class JeiMarketRecipeCategory implements IRecipeCategory<RecipeHolder<MarketRecipe>> {

    public static final RecipeType<RecipeHolder<MarketRecipe>> TYPE = RecipeType.createFromVanilla(ModRecipes.marketRecipeType);

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "textures/gui/jei_market.png");

    private final IDrawable icon;
    private final IDrawableStatic background;

    public JeiMarketRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.market));
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 86, 48);
    }

    @Override
    public RecipeType<RecipeHolder<MarketRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.farmingforblockheads.market");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MarketRecipe> recipeHolder, IFocusGroup focuses) {
        final var recipe = recipeHolder.value();
        final var payment = MarketDefaultsRegistry.resolvePayment(recipe);
        builder.addSlot(RecipeIngredientRole.INPUT, 16, 13)
                .addIngredients(payment.ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 54, 13)
                .addIngredient(VanillaTypes.ITEM_STACK, recipe.result());
    }

    @Override
    public void draw(RecipeHolder<MarketRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // TODO Component costText = getFormattedCostString(recipeHolder.value());
        // TODO Font font = Minecraft.getInstance().font;
        // TODO int stringWidth = font.width(costText);
        // TODO guiGraphics.drawString(font, costText.getVisualOrderText(), 42 - stringWidth / 2, 35, 0xFFFFFF, true);
    }
}
