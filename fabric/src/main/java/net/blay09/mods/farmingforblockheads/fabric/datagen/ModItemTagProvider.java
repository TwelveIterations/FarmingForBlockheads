package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends IntrinsicHolderTagsProvider<Item> {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ITEM, registriesFuture, (item) -> item.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(ModItemTags.RICH_FARMLAND).add(ModBlocks.fertilizedFarmlandRichStable.asItem(), ModBlocks.fertilizedFarmlandRich.asItem());
        tag(ModItemTags.HEALTHY_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asItem(),
                ModBlocks.fertilizedFarmlandHealthy.asItem());
        tag(ModItemTags.STABLE_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asItem(),
                ModBlocks.fertilizedFarmlandRichStable.asItem(),
                ModBlocks.fertilizedFarmlandStable.asItem());

        tag(ModItemTags.FERTILIZED_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asItem(),
                ModBlocks.fertilizedFarmlandRichStable.asItem(),
                ModBlocks.fertilizedFarmlandStable.asItem(),
                ModBlocks.fertilizedFarmlandHealthy.asItem(),
                ModBlocks.fertilizedFarmlandRich.asItem());
    }
}
