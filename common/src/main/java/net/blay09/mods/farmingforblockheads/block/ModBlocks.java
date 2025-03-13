package net.blay09.mods.farmingforblockheads.block;

import net.blay09.mods.balm.api.block.BalmBlocks;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.component.DescriptionComponent;
import net.blay09.mods.farmingforblockheads.component.ModComponents;
import net.blay09.mods.farmingforblockheads.item.FertilizerItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class ModBlocks {

    public static Block market;
    public static Block chickenNest;
    public static Block feedingTrough;
    public static Block fertilizedFarmlandRich;
    public static Block fertilizedFarmlandRichStable;
    public static Block fertilizedFarmlandHealthy;
    public static Block fertilizedFarmlandHealthyStable;
    public static Block fertilizedFarmlandStable;

    public static void initialize(BalmBlocks blocks) {
        blocks.register((identifier) -> market = new MarketBlock(defaultProperties(identifier)),
                (block, identifier) -> new BlockItem(block, itemProperties(identifier)),
                id("market"));
        blocks.register((identifier) -> chickenNest = new ChickenNestBlock(defaultProperties(identifier)),
                (block, identifier) -> new BlockItem(block, itemProperties(identifier)
                        .component(ModComponents.description.get(),
                                new DescriptionComponent(Component.translatable("tooltip.farmingforblockheads.chicken_nest")))),
                id("chicken_nest"));
        blocks.register((identifier) -> feedingTrough = new FeedingTroughBlock(defaultProperties(identifier)),
                (block, identifier) -> new BlockItem(block, itemProperties(identifier)
                        .component(ModComponents.description.get(),
                                new DescriptionComponent(Component.translatable("tooltip.farmingforblockheads.feeding_trough")))),
                id("feeding_trough"));
        blocks.register((identifier) -> fertilizedFarmlandRich = new FertilizedFarmlandBlock(defaultProperties(identifier)),
                (block, identifier) -> new BlockItem(block, itemProperties(identifier)
                        .component(ModComponents.description.get(), new DescriptionComponent(FertilizerItem.FertilizerType.RICH.getTooltip()))),
                id("fertilized_farmland_rich"));
        blocks.register((identifier) -> fertilizedFarmlandRichStable = new FertilizedFarmlandBlock(defaultProperties(identifier)),
                (block, identifier) -> new BlockItem(block, itemProperties(identifier)
                        .component(ModComponents.description.get(),
                                new DescriptionComponent(List.of(
                                        FertilizerItem.FertilizerType.RICH.getTooltip(),
                                        FertilizerItem.FertilizerType.STABLE.getTooltip())))),
                id("fertilized_farmland_rich_stable"));
        blocks.register((identifier) -> fertilizedFarmlandHealthy = new FertilizedFarmlandBlock(defaultProperties(identifier)),
                (block, identifier) -> new BlockItem(block, itemProperties(identifier)
                        .component(ModComponents.description.get(),
                                new DescriptionComponent(FertilizerItem.FertilizerType.HEALTHY.getTooltip()))),
                id("fertilized_farmland_healthy"));
        blocks.register((identifier) -> fertilizedFarmlandHealthyStable = new FertilizedFarmlandBlock(defaultProperties(identifier)),
                (block, identifier) -> new BlockItem(block, itemProperties(identifier)
                        .component(ModComponents.description.get(),
                                new DescriptionComponent(List.of(
                                        FertilizerItem.FertilizerType.HEALTHY.getTooltip(),
                                        FertilizerItem.FertilizerType.STABLE.getTooltip())))),
                id("fertilized_farmland_healthy_stable"));
        blocks.register((identifier) -> fertilizedFarmlandStable = new FertilizedFarmlandBlock(defaultProperties(identifier)),
                (block, identifier) -> new BlockItem(block, itemProperties(identifier)
                        .component(ModComponents.description.get(),
                                new DescriptionComponent(FertilizerItem.FertilizerType.STABLE.getTooltip()))),
                id("fertilized_farmland_stable"));
    }

    private static BlockBehaviour.Properties defaultProperties(ResourceLocation identifier) {
        return BlockBehaviour.Properties.of().setId(blockId(identifier));
    }

    private static Item.Properties itemProperties(ResourceLocation identifier) {
        return new Item.Properties().setId(itemId(identifier));
    }

    private static ResourceKey<Block> blockId(ResourceLocation identifier) {
        return ResourceKey.create(Registries.BLOCK, identifier);
    }

    private static ResourceKey<Item> itemId(ResourceLocation identifier) {
        return ResourceKey.create(Registries.ITEM, identifier);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, path);
    }
}
