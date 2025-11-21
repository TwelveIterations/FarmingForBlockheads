package net.blay09.mods.farmingforblockheads;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.blay09.mods.balm.platform.event.callback.CropCallback;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FarmingForBlockheads {

    public static final String MOD_ID = "farmingforblockheads";

    public static Logger logger = LogManager.getLogger();

    public static void initialize(BalmRegistrars registrars) {
        FarmingForBlockheadsConfig.initialize();
        registrars.dataComponentTypes(ModComponents::initialize);
        ModNetworking.initialize(Balm.networking());
        registrars.blocks(ModBlocks::initialize);
        registrars.blockEntityTypes(ModBlockEntities::initialize);
        registrars.entityTypes(ModEntities::initialize);
        registrars.items(ModItems::initialize);
        registrars.registrar(Registries.SOUND_EVENT, ModSounds::initialize);
        registrars.menuTypes(ModMenus::initialize);
        registrars.recipeTypes(ModRecipes::initialize);
        ModLootModifiers.initialize(Balm.lootModifiers());

        registrars.resourceReloadListeners(registrar -> {
            registrar.register("market_category_loader", new MarketCategoryLoader());
            registrar.register("market_defaults_loader", MarketDefaultsLoader::new);
        });

        CropCallback.Grow.After.EVENT.register(FarmlandHandler::onGrowEvent);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
