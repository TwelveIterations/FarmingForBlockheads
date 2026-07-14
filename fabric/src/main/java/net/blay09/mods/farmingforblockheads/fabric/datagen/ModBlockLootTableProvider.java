package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.MarketBlock;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootSubProvider {
    protected ModBlockLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(dataOutput, provider);
    }

    @Override
    public void generate() {
        add(ModBlocks.market.asBlock(), createSinglePropConditionTable(ModBlocks.market.asBlock(), MarketBlock.HALF, DoubleBlockHalf.LOWER));
        dropSelf(ModBlocks.chickenNest.asBlock());
        add(ModBlocks.rabbitTrap.asBlock(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .when(ExplosionCondition.survivesExplosion())
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModBlocks.rabbitTrap.asBlock())
                                .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContext.BlockEntityTarget.BLOCK_ENTITY.contextParam())
                                        .include(DataComponents.MAX_DAMAGE)
                                        .include(DataComponents.DAMAGE)))));
        dropSelf(ModBlocks.feedingTrough.asBlock());
        dropSelf(ModBlocks.sprinkler.asBlock());
        dropSelf(ModBlocks.fishingBarrel.asBlock());
        dropSelf(ModBlocks.shippingBin.asBlock());
        dropOther(ModBlocks.fertilizedFarmlandHealthy.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandRich.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandStable.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandHealthyStable.asBlock(), Blocks.DIRT);
        dropOther(ModBlocks.fertilizedFarmlandRichStable.asBlock(), Blocks.DIRT);
    }
}
