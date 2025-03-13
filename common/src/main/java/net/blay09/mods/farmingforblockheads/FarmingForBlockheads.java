package net.blay09.mods.farmingforblockheads;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.CropGrowEvent;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.block.entity.ModBlockEntities;
import net.blay09.mods.farmingforblockheads.component.ModComponents;
import net.blay09.mods.farmingforblockheads.entity.ModEntities;
import net.blay09.mods.farmingforblockheads.menu.ModMenus;
import net.blay09.mods.farmingforblockheads.item.ModItems;
import net.blay09.mods.farmingforblockheads.loot.ModLootModifiers;
import net.blay09.mods.farmingforblockheads.network.ModNetworking;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.registry.MarketCategoryLoader;
import net.blay09.mods.farmingforblockheads.registry.MarketDefaultsLoader;
import net.blay09.mods.farmingforblockheads.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FarmingForBlockheads {

    public static final String MOD_ID = "farmingforblockheads";

    public static Logger logger = LogManager.getLogger();

    public static void initialize() {
        FarmingForBlockheadsConfig.initialize();
        ModNetworking.initialize(Balm.getNetworking());
        ModBlocks.initialize(Balm.getBlocks());
        ModBlockEntities.initialize(Balm.getBlockEntities());
        ModEntities.initialize(Balm.getEntities());
        ModItems.initialize(Balm.getItems());
        ModSounds.initialize(Balm.getSounds());
        ModMenus.initialize(Balm.getMenus());
        ModLootModifiers.initialize(Balm.getLootTables());
        ModRecipes.initialize(Balm.getRecipes());
        ModComponents.initialize(Balm.getComponents());

        Balm.addServerReloadListener(ResourceLocation.fromNamespaceAndPath(MOD_ID, "market_category_loader"), new MarketCategoryLoader());
        Balm.addServerReloadListener(ResourceLocation.fromNamespaceAndPath(MOD_ID, "market_defaults_loader"), MarketDefaultsLoader::new);

        Balm.getEvents().onEvent(CropGrowEvent.Post.class, FarmlandHandler::onGrowEvent);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
