package net.blay09.mods.farmingforblockheads.client;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.client.gui.screen.MarketScreen;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeImpl;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.registry.MarketDefaultsRegistry;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class FarmingForBlockheadsClient {

    public static void initialize(BalmClientRegistrars registrars) {
        registrars.menuScreens(ModScreens::initialize);
        registrars.entityRenderers(ModRenderers::initialize);
        registrars.blockEntityRenderers(ModRenderers::initialize);
        registrars.modelLayers(ModRenderers::initialize);
        registrars.blockStateModels(ModRenderers::initialize);

        Balm.modSupport().recipeViewers().register(id("recipes"), registrar -> {
            registrar.registerRecipeType(id("market"), MarketRecipeImpl.class)
                    .withSyncedRecipes(ModRecipes.marketRecipe)
                    .withCraftingStation(ModBlocks.market)
                    .buildDisplay(display -> display
                            .title(Component.translatable("jei.farmingforblockheads.market"))
                            .icon(ModBlocks.market)
                            .size(86, 48)
                            .background(id("textures/gui/jei_market.png"))
                            .slots((recipe, slots) -> {
                                final var payment = MarketDefaultsRegistry.resolvePayment(recipe);
                                slots.inputSlot(16, 13).add(payment.ingredient());
                                slots.outputSlot(54, 13).add(recipe.result());
                            }));

            registrar.registerScreenOcclusion(MarketScreen.class, marketScreen -> marketScreen.getFilterButtons().stream()
                    .map(button -> new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight()))
                    .toList());
        });
    }
}
