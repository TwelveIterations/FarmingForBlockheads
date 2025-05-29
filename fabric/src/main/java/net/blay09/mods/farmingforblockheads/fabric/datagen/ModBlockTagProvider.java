package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends IntrinsicHolderTagsProvider<Block> {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BLOCK, registriesFuture, (block) -> block.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        final var dirtTag = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("dirt"));
        tag(dirtTag).add(ModBlocks.fertilizedFarmlandHealthy,
                ModBlocks.fertilizedFarmlandRich,
                ModBlocks.fertilizedFarmlandStable,
                ModBlocks.fertilizedFarmlandHealthyStable,
                ModBlocks.fertilizedFarmlandRichStable);

        final var mineableAxeTag = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/axe"));
        tag(mineableAxeTag).add(ModBlocks.market, ModBlocks.chickenNest, ModBlocks.feedingTrough);

        final var mineableShovelTag = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/shovel"));
        tag(mineableShovelTag).add(ModBlocks.fertilizedFarmlandHealthyStable,
                ModBlocks.fertilizedFarmlandRichStable,
                ModBlocks.fertilizedFarmlandStable,
                ModBlocks.fertilizedFarmlandHealthy,
                ModBlocks.fertilizedFarmlandRich);

        tag(ModBlockTags.RICH_FARMLAND).add(ModBlocks.fertilizedFarmlandRichStable, ModBlocks.fertilizedFarmlandRich);
        tag(ModBlockTags.HEALTHY_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable, ModBlocks.fertilizedFarmlandHealthy);
        tag(ModBlockTags.STABLE_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable,
                ModBlocks.fertilizedFarmlandRichStable,
                ModBlocks.fertilizedFarmlandStable);

        tag(ModBlockTags.FERTILIZED_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable, ModBlocks.fertilizedFarmlandRichStable,
                ModBlocks.fertilizedFarmlandStable, ModBlocks.fertilizedFarmlandHealthy, ModBlocks.fertilizedFarmlandRich);
    }

}
