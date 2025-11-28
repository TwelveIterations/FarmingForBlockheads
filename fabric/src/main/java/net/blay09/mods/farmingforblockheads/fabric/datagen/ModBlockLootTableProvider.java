package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.MarketBlock;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootTableProvider {
    protected ModBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(dataOutput, provider);
    }

    @Override
    public void generate() {
        add(ModBlocks.market.asBlock(), createSinglePropConditionTable(ModBlocks.market.asBlock(), MarketBlock.HALF, DoubleBlockHalf.LOWER));
        dropSelf(ModBlocks.chickenNest.asBlock());
        dropSelf(ModBlocks.feedingTrough.asBlock());
        dropOther(ModBlocks.fertilizedFarmlandHealthy.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandRich.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandStable.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandRichStable.asBlock(), Blocks.DIRT);
    }
}
