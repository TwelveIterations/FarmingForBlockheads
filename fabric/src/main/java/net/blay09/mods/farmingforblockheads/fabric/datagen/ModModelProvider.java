package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.MarketBlock;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.block.SprinklerBlock;
import net.blay09.mods.farmingforblockheads.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import static net.minecraft.client.data.models.BlockModelGenerators.*;

public class ModModelProvider extends FabricModelProvider {

    private static final PropertyDispatch<VariantMutator> ROTATION_HORIZONTAL_FACING = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Y_ROT_90)
            .select(Direction.SOUTH, Y_ROT_180)
            .select(Direction.WEST, Y_ROT_270)
            .select(Direction.NORTH, NOP);

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        generators.createNonTemplateModelBlock(ModBlocks.feedingTrough.asBlock());
        createSprinkler(generators);
        generators.registerSimpleItemModel(ModBlocks.sprinkler.asBlock(), ModelLocationUtils.getModelLocation(ModBlocks.sprinkler.asItem()));
        generators.createNonTemplateHorizontalBlock(ModBlocks.shippingBin.asBlock());
        generators.registerSimpleItemModel(ModBlocks.shippingBin.asBlock(), ModelLocationUtils.getModelLocation(ModBlocks.shippingBin.asBlock()));
        generators.createNonTemplateHorizontalBlock(ModBlocks.fishingBarrel.asBlock());
        generators.registerSimpleItemModel(ModBlocks.fishingBarrel.asBlock(), ModelLocationUtils.getModelLocation(ModBlocks.fishingBarrel.asBlock()));

        createDoubleBlockMarket(generators, ModBlocks.market.asBlock(), ModBlocks.market.asBlock());
        generators.createNonTemplateHorizontalBlock(ModBlocks.chickenNest.asBlock());

        createFertilizedFarmland(generators, ModBlocks.fertilizedFarmlandHealthy.asBlock());
        createFertilizedFarmland(generators, ModBlocks.fertilizedFarmlandRich.asBlock());
        createFertilizedFarmland(generators, ModBlocks.fertilizedFarmlandStable.asBlock());
        createFertilizedFarmland(generators, ModBlocks.fertilizedFarmlandHealthyStable.asBlock());
        createFertilizedFarmland(generators, ModBlocks.fertilizedFarmlandRichStable.asBlock());
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        generators.generateFlatItem(ModItems.greenFertilizer.asItem(), ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.redFertilizer.asItem(), ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.yellowFertilizer.asItem(), ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.scythe.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generators.generateFlatItem(ModItems.wateringCan.asItem(), ModelTemplates.FLAT_ITEM);
    }

    private void createFertilizedFarmland(BlockModelGenerators generators, Block farmland) {
        final var textureMapping = (new TextureMapping()).put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(farmland));
        final var moistTextureMapping = (new TextureMapping()).put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(farmland, "_moist"));
        final var variant = plainVariant(ModelTemplates.FARMLAND.create(farmland, textureMapping, generators.modelOutput));
        final var moistVariant = plainVariant(ModelTemplates.FARMLAND.create(ModelLocationUtils.getModelLocation(farmland, "_moist"),
                moistTextureMapping,
                generators.modelOutput));
        generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(farmland)
                .with(createEmptyOrFullDispatch(BlockStateProperties.MOISTURE, 7, moistVariant, variant)));
    }

    private void createSprinkler(BlockModelGenerators generators) {
        final var block = ModBlocks.sprinkler.asBlock();
        final var model = plainVariant(ModelLocationUtils.getModelLocation(block));
        generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(SprinklerBlock.LIT).generate(_ -> model)));
    }

    private void createDoubleBlockMarket(BlockModelGenerators generators, Block block, Block modelBlock) {
        final var topModelLocation = ModelLocationUtils.getModelLocation(modelBlock, "_top");
        final var bottomModelLocation = ModelLocationUtils.getModelLocation(modelBlock, "_bottom");
        final var generator = MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(MarketBlock.HALF)
                        .select(DoubleBlockHalf.LOWER, plainVariant(bottomModelLocation))
                        .select(DoubleBlockHalf.UPPER, plainVariant(topModelLocation)))
                .with(ROTATION_HORIZONTAL_FACING);
        generators.blockStateOutput.accept(generator);
        final var item = block.asItem();
        generators.registerSimpleItemModel(block.asItem(), ModelLocationUtils.getModelLocation(item));
    }
}
