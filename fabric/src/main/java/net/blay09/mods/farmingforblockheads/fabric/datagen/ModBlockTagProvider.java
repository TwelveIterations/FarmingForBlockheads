package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        final var relocationNotSupported = builder(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "relocation_not_supported")));
        relocationNotSupported.add(ModBlocks.market.asResourceKey());

        final var dirtTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("dirt"));
        builder(dirtTag).add(ModBlocks.fertilizedFarmlandHealthy.asResourceKey(),
                ModBlocks.fertilizedFarmlandRich.asResourceKey(),
                ModBlocks.fertilizedFarmlandStable.asResourceKey(),
                ModBlocks.fertilizedFarmlandHealthyStable.asResourceKey(),
                ModBlocks.fertilizedFarmlandRichStable.asResourceKey());

        final var mineableAxeTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("mineable/axe"));
        builder(mineableAxeTag).add(ModBlocks.market.asResourceKey(), ModBlocks.chickenNest.asResourceKey(), ModBlocks.feedingTrough.asResourceKey(), ModBlocks.shippingBin.asResourceKey());

        final var mineablePickaxeTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("mineable/pickaxe"));
        builder(mineablePickaxeTag).add(ModBlocks.sprinkler.asResourceKey());

        final var mineableShovelTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("mineable/shovel"));
        builder(mineableShovelTag).add(ModBlocks.fertilizedFarmlandHealthyStable.asResourceKey(),
                ModBlocks.fertilizedFarmlandRichStable.asResourceKey(),
                ModBlocks.fertilizedFarmlandStable.asResourceKey(),
                ModBlocks.fertilizedFarmlandHealthy.asResourceKey(),
                ModBlocks.fertilizedFarmlandRich.asResourceKey());

        builder(ModBlockTags.RICH_FARMLAND).add(ModBlocks.fertilizedFarmlandRichStable.asResourceKey(), ModBlocks.fertilizedFarmlandRich.asResourceKey());
        builder(ModBlockTags.HEALTHY_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asResourceKey(), ModBlocks.fertilizedFarmlandHealthy.asResourceKey());
        builder(ModBlockTags.STABLE_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asResourceKey(),
                ModBlocks.fertilizedFarmlandRichStable.asResourceKey(),
                ModBlocks.fertilizedFarmlandStable.asResourceKey());

        builder(ModBlockTags.FERTILIZED_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asResourceKey(), ModBlocks.fertilizedFarmlandRichStable.asResourceKey(),
                ModBlocks.fertilizedFarmlandStable.asResourceKey(), ModBlocks.fertilizedFarmlandHealthy.asResourceKey(), ModBlocks.fertilizedFarmlandRich.asResourceKey());

        builder(ModBlockTags.LAVA_SPRINKLER_BASE).add(BlockItemIds.MAGMA_BLOCK);
    }

}
