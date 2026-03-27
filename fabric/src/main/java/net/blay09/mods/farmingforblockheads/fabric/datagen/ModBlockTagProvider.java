package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        final var dirtTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("dirt"));
        valueLookupBuilder(dirtTag).add(ModBlocks.fertilizedFarmlandHealthy.asBlock(),
                ModBlocks.fertilizedFarmlandRich.asBlock(),
                ModBlocks.fertilizedFarmlandStable.asBlock(),
                ModBlocks.fertilizedFarmlandHealthyStable.asBlock(),
                ModBlocks.fertilizedFarmlandRichStable.asBlock());

        final var mineableAxeTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("mineable/axe"));
        valueLookupBuilder(mineableAxeTag).add(ModBlocks.market.asBlock(), ModBlocks.chickenNest.asBlock(), ModBlocks.feedingTrough.asBlock());

        final var mineableShovelTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("mineable/shovel"));
        valueLookupBuilder(mineableShovelTag).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(),
                ModBlocks.fertilizedFarmlandRichStable.asBlock(),
                ModBlocks.fertilizedFarmlandStable.asBlock(),
                ModBlocks.fertilizedFarmlandHealthy.asBlock(),
                ModBlocks.fertilizedFarmlandRich.asBlock());

        valueLookupBuilder(ModBlockTags.RICH_FARMLAND).add(ModBlocks.fertilizedFarmlandRichStable.asBlock(), ModBlocks.fertilizedFarmlandRich.asBlock());
        valueLookupBuilder(ModBlockTags.HEALTHY_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(), ModBlocks.fertilizedFarmlandHealthy.asBlock());
        valueLookupBuilder(ModBlockTags.STABLE_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(),
                ModBlocks.fertilizedFarmlandRichStable.asBlock(),
                ModBlocks.fertilizedFarmlandStable.asBlock());

        valueLookupBuilder(ModBlockTags.FERTILIZED_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(), ModBlocks.fertilizedFarmlandRichStable.asBlock(),
                ModBlocks.fertilizedFarmlandStable.asBlock(), ModBlocks.fertilizedFarmlandHealthy.asBlock(), ModBlocks.fertilizedFarmlandRich.asBlock());
    }

}
