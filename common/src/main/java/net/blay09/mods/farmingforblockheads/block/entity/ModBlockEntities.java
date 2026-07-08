package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityTypeRegistrar;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static Holder<BlockEntityType<ChickenNestBlockEntity>> chickenNest;
    public static Holder<BlockEntityType<FeedingTroughBlockEntity>> feedingTrough;
    public static Holder<BlockEntityType<MarketBlockEntity>> market;
    public static Holder<BlockEntityType<ShippingBinBlockEntity>> shippingBin;

    public static void initialize(BalmBlockEntityTypeRegistrar blockEntities) {
        chickenNest = blockEntities.register("chicken_nest", ChickenNestBlockEntity::new, ModBlocks.chickenNest).asHolder();
        feedingTrough = blockEntities.register("feeding_trough", FeedingTroughBlockEntity::new, ModBlocks.feedingTrough).asHolder();
        market = blockEntities.register("market", MarketBlockEntity::new, ModBlocks.market).asHolder();
        shippingBin = blockEntities.register("shipping_bin", ShippingBinBlockEntity::new, ModBlocks.shippingBin).asHolder();
    }

}
