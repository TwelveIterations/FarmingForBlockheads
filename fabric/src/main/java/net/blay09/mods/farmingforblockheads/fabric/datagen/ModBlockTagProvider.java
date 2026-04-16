package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider<Block> {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BLOCK, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        final var relocationNotSupported = getOrCreateTagBuilder(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "relocation_not_supported")));
        relocationNotSupported.add(ModBlocks.market);

        final var dirtTag = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("dirt"));
        getOrCreateTagBuilder(dirtTag).add(ModBlocks.fertilizedFarmlandHealthy,
                ModBlocks.fertilizedFarmlandRich,
                ModBlocks.fertilizedFarmlandStable,
                ModBlocks.fertilizedFarmlandHealthyStable,
                ModBlocks.fertilizedFarmlandRichStable);

        final var mineableAxeTag = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/axe"));
        getOrCreateTagBuilder(mineableAxeTag).add(ModBlocks.market, ModBlocks.chickenNest, ModBlocks.feedingTrough);

        final var mineableShovelTag = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/shovel"));
        getOrCreateTagBuilder(mineableShovelTag).add(ModBlocks.fertilizedFarmlandHealthyStable,
                ModBlocks.fertilizedFarmlandRichStable,
                ModBlocks.fertilizedFarmlandStable,
                ModBlocks.fertilizedFarmlandHealthy,
                ModBlocks.fertilizedFarmlandRich);

        getOrCreateTagBuilder(ModBlockTags.RICH_FARMLAND).add(ModBlocks.fertilizedFarmlandRichStable, ModBlocks.fertilizedFarmlandRich);
        getOrCreateTagBuilder(ModBlockTags.HEALTHY_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable, ModBlocks.fertilizedFarmlandHealthy);
        getOrCreateTagBuilder(ModBlockTags.STABLE_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable,
                ModBlocks.fertilizedFarmlandRichStable,
                ModBlocks.fertilizedFarmlandStable);

        getOrCreateTagBuilder(ModBlockTags.FERTILIZED_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable, ModBlocks.fertilizedFarmlandRichStable,
                ModBlocks.fertilizedFarmlandStable, ModBlocks.fertilizedFarmlandHealthy, ModBlocks.fertilizedFarmlandRich);
    }

}
