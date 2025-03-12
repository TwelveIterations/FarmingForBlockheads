package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.MarketBlock;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import static net.minecraft.client.data.models.BlockModelGenerators.createEmptyOrFullDispatch;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.feedingTrough);

        createDoubleBlockMarket(blockStateModelGenerator, ModBlocks.market, ModBlocks.market);
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.chickenNest);

        createFertilizedFarmland(blockStateModelGenerator, ModBlocks.fertilizedFarmlandHealthy);
        createFertilizedFarmland(blockStateModelGenerator, ModBlocks.fertilizedFarmlandRich);
        createFertilizedFarmland(blockStateModelGenerator, ModBlocks.fertilizedFarmlandStable);
        createFertilizedFarmland(blockStateModelGenerator, ModBlocks.fertilizedFarmlandHealthyStable);
        createFertilizedFarmland(blockStateModelGenerator, ModBlocks.fertilizedFarmlandRichStable);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(ModItems.greenFertilizer, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.redFertilizer, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.yellowFertilizer, ModelTemplates.FLAT_ITEM);
    }

    private void createFertilizedFarmland(BlockModelGenerators blockStateModelGenerator, Block farmland) {
        final var textureMapping = (new TextureMapping()).put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(farmland));
        final var moistTextureMapping = (new TextureMapping()).put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(farmland, "_moist"));
        final var model = ModelTemplates.FARMLAND.create(farmland, textureMapping, blockStateModelGenerator.modelOutput);
        final var moistModel = ModelTemplates.FARMLAND.create(TextureMapping.getBlockTexture(farmland, "_moist"),
                moistTextureMapping,
                blockStateModelGenerator.modelOutput);
        blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(farmland)
                .with(createEmptyOrFullDispatch(BlockStateProperties.MOISTURE, 7, moistModel, model)));
    }

    private void createDoubleBlockMarket(BlockModelGenerators blockStateModelGenerator, Block block, Block modelBlock) {
        final var topModelLocation = ModelLocationUtils.getModelLocation(modelBlock, "_top");
        final var bottomModelLocation = ModelLocationUtils.getModelLocation(modelBlock, "_bottom");
        final var generator = MultiVariantGenerator.multiVariant(block)
                .with(createHorizontalFacingDispatch())
                .with(PropertyDispatch.property(MarketBlock.HALF)
                        .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                        .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, topModelLocation)));
        blockStateModelGenerator.blockStateOutput.accept(generator);
        Item item = block.asItem();
        blockStateModelGenerator.registerSimpleItemModel(block.asItem(), ModelLocationUtils.getModelLocation(item));
    }
}
