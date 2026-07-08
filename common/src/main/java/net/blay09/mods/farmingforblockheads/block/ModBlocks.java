package net.blay09.mods.farmingforblockheads.block;

import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.blay09.mods.farmingforblockheads.component.DescriptionComponent;
import net.blay09.mods.farmingforblockheads.component.ModComponents;
import net.blay09.mods.farmingforblockheads.item.FertilizerItem;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModBlocks {

    public static DeferredBlock market;
    public static DeferredBlock shippingBin;
    public static DeferredBlock chickenNest;
    public static DeferredBlock feedingTrough;
    public static DeferredBlock fertilizedFarmlandRich;
    public static DeferredBlock fertilizedFarmlandRichStable;
    public static DeferredBlock fertilizedFarmlandHealthy;
    public static DeferredBlock fertilizedFarmlandHealthyStable;
    public static DeferredBlock fertilizedFarmlandStable;

    public static void initialize(BalmBlockRegistrar blocks) {
        market = blocks.register("market", MarketBlock::new, it -> it).withDefaultItem().asDeferredBlock();
        shippingBin = blocks.register("shipping_bin", ShippingBinBlock::new, it -> it)
                .withDefaultItem()
                .asDeferredBlock();
        chickenNest = blocks.register("chicken_nest", ChickenNestBlock::new, it -> it)
                .withDefaultItem(it -> it.component(ModComponents.description.value(),
                        new DescriptionComponent(Component.translatable("tooltip.farmingforblockheads.chicken_nest"))))
                .asDeferredBlock();

        feedingTrough = blocks.register("feeding_trough", FeedingTroughBlock::new, it -> it)
                .withDefaultItem(it -> it.component(ModComponents.description.value(),
                        new DescriptionComponent(Component.translatable("tooltip.farmingforblockheads.feeding_trough"))))
                .asDeferredBlock();
        fertilizedFarmlandRich = blocks.register("fertilized_farmland_rich", FertilizedFarmlandBlock::new, it -> it)
                .withDefaultItem(it -> it.component(ModComponents.description.value(),
                        new DescriptionComponent(FertilizerItem.FertilizerType.RICH.getTooltip())))
                .asDeferredBlock();
        fertilizedFarmlandRichStable = blocks.register("fertilized_farmland_rich_stable", FertilizedFarmlandBlock::new, it -> it)
                .withDefaultItem(it -> it.component(ModComponents.description.value(),
                        new DescriptionComponent(List.of(
                                FertilizerItem.FertilizerType.RICH.getTooltip(),
                                FertilizerItem.FertilizerType.STABLE.getTooltip()))))
                .asDeferredBlock();
        fertilizedFarmlandHealthy = blocks.register("fertilized_farmland_healthy", FertilizedFarmlandBlock::new, it -> it)
                .withDefaultItem(it -> it.component(ModComponents.description.value(),
                        new DescriptionComponent(FertilizerItem.FertilizerType.HEALTHY.getTooltip())))
                .asDeferredBlock();
        fertilizedFarmlandHealthyStable = blocks.register("fertilized_farmland_healthy_stable", FertilizedFarmlandBlock::new, it -> it)
                .withDefaultItem(it -> it.component(ModComponents.description.value(),
                        new DescriptionComponent(List.of(
                                FertilizerItem.FertilizerType.HEALTHY.getTooltip(),
                                FertilizerItem.FertilizerType.STABLE.getTooltip()))))
                .asDeferredBlock();
        fertilizedFarmlandStable = blocks.register("fertilized_farmland_stable", FertilizedFarmlandBlock::new, it -> it)
                .withDefaultItem(it -> it.component(ModComponents.description.value(),
                        new DescriptionComponent(FertilizerItem.FertilizerType.STABLE.getTooltip())))
                .asDeferredBlock();
    }

}
