package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        builder(ModItemTags.RICH_FARMLAND).add(ModBlocks.fertilizedFarmlandRichStable.asBlockItemId(), ModBlocks.fertilizedFarmlandRich.asBlockItemId());
        builder(ModItemTags.HEALTHY_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlockItemId(),
                ModBlocks.fertilizedFarmlandHealthy.asBlockItemId());
        builder(ModItemTags.STABLE_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlockItemId(),
                ModBlocks.fertilizedFarmlandRichStable.asBlockItemId(),
                ModBlocks.fertilizedFarmlandStable.asBlockItemId());

        builder(ModItemTags.FERTILIZED_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlockItemId(),
                ModBlocks.fertilizedFarmlandRichStable.asBlockItemId(),
                ModBlocks.fertilizedFarmlandStable.asBlockItemId(),
                ModBlocks.fertilizedFarmlandHealthy.asBlockItemId(),
                ModBlocks.fertilizedFarmlandRich.asBlockItemId());
    }
}
