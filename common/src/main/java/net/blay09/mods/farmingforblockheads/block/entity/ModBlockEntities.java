package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityTypeRegistrar;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static Holder<BlockEntityType<ChickenNestBlockEntity>> chickenNest;
    public static Holder<BlockEntityType<RabbitTrapBlockEntity>> rabbitTrap;
    public static Holder<BlockEntityType<FeedingTroughBlockEntity>> feedingTrough;
    public static Holder<BlockEntityType<FishingBarrelBlockEntity>> fishingBarrel;
    public static Holder<BlockEntityType<MarketBlockEntity>> market;
    public static Holder<BlockEntityType<ShippingBinBlockEntity>> shippingBin;
    public static Holder<BlockEntityType<SprinklerBlockEntity>> sprinkler;

    public static void initialize(BalmBlockEntityTypeRegistrar blockEntities) {
        chickenNest = blockEntities.register("chicken_nest", ChickenNestBlockEntity::new, ModBlocks.chickenNest).asHolder();
        rabbitTrap = blockEntities.register("rabbit_trap", RabbitTrapBlockEntity::new, ModBlocks.rabbitTrap).asHolder();
        feedingTrough = blockEntities.register("feeding_trough", FeedingTroughBlockEntity::new, ModBlocks.feedingTrough).asHolder();
        fishingBarrel = blockEntities.register("fishing_barrel", FishingBarrelBlockEntity::new, ModBlocks.fishingBarrel).asHolder();
        market = blockEntities.register("market", MarketBlockEntity::new, ModBlocks.market).asHolder();
        shippingBin = blockEntities.register("shipping_bin", ShippingBinBlockEntity::new, ModBlocks.shippingBin).asHolder();
        sprinkler = blockEntities.register("sprinkler", SprinklerBlockEntity::new, ModBlocks.sprinkler).asHolder();
    }

}
