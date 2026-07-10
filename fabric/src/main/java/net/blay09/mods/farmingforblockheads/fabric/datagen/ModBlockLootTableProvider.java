package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.MarketBlock;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootSubProvider {
    protected ModBlockLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(dataOutput, provider);
    }

    @Override
    public void generate() {
        add(ModBlocks.market.asBlock(), createSinglePropConditionTable(ModBlocks.market.asBlock(), MarketBlock.HALF, DoubleBlockHalf.LOWER));
        dropSelf(ModBlocks.chickenNest.asBlock());
        dropSelf(ModBlocks.feedingTrough.asBlock());
        dropSelf(ModBlocks.sprinkler.asBlock());
        dropSelf(ModBlocks.shippingBin.asBlock());
        dropOther(ModBlocks.fertilizedFarmlandHealthy.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandRich.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandStable.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandRichStable.asBlock(), Blocks.DIRT);
    }
}
