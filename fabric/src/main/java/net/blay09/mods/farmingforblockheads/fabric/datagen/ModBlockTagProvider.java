package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends IntrinsicHolderTagsProvider<Block> {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BLOCK, registriesFuture, (block) -> block.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        final var dirtTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("dirt"));
        tag(dirtTag).add(ModBlocks.fertilizedFarmlandHealthy.asBlock(),
                ModBlocks.fertilizedFarmlandRich.asBlock(),
                ModBlocks.fertilizedFarmlandStable.asBlock(),
                ModBlocks.fertilizedFarmlandHealthyStable.asBlock(),
                ModBlocks.fertilizedFarmlandRichStable.asBlock());

        final var mineableAxeTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("mineable/axe"));
        tag(mineableAxeTag).add(ModBlocks.market.asBlock(), ModBlocks.chickenNest.asBlock(), ModBlocks.feedingTrough.asBlock());

        final var mineableShovelTag = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("mineable/shovel"));
        tag(mineableShovelTag).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(),
                ModBlocks.fertilizedFarmlandRichStable.asBlock(),
                ModBlocks.fertilizedFarmlandStable.asBlock(),
                ModBlocks.fertilizedFarmlandHealthy.asBlock(),
                ModBlocks.fertilizedFarmlandRich.asBlock());

        tag(ModBlockTags.RICH_FARMLAND).add(ModBlocks.fertilizedFarmlandRichStable.asBlock(), ModBlocks.fertilizedFarmlandRich.asBlock());
        tag(ModBlockTags.HEALTHY_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(), ModBlocks.fertilizedFarmlandHealthy.asBlock());
        tag(ModBlockTags.STABLE_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(),
                ModBlocks.fertilizedFarmlandRichStable.asBlock(),
                ModBlocks.fertilizedFarmlandStable.asBlock());

        tag(ModBlockTags.FERTILIZED_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(), ModBlocks.fertilizedFarmlandRichStable.asBlock(),
                ModBlocks.fertilizedFarmlandStable.asBlock(), ModBlocks.fertilizedFarmlandHealthy.asBlock(), ModBlocks.fertilizedFarmlandRich.asBlock());
    }

}
