package net.blay09.mods.farmingforblockheads.item;

import net.blay09.mods.balm.api.item.BalmItems;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItems {

    public static Item greenFertilizer;
    public static Item redFertilizer;
    public static Item yellowFertilizer;

    public static void initialize(BalmItems items) {
        items.registerItem((identifier) -> greenFertilizer = new FertilizerItem(itemProperties(identifier), FertilizerItem.FertilizerType.RICH),
                id("green_fertilizer"));
        items.registerItem((identifier) -> redFertilizer = new FertilizerItem(itemProperties(identifier), FertilizerItem.FertilizerType.HEALTHY),
                id("red_fertilizer"));
        items.registerItem((identifier) -> yellowFertilizer = new FertilizerItem(itemProperties(identifier), FertilizerItem.FertilizerType.STABLE),
                id("yellow_fertilizer"));

        items.registerCreativeModeTab(() -> new ItemStack(ModBlocks.market), id("farmingforblockheads"));
    }

    private static Item.Properties itemProperties(ResourceLocation identifier) {
        return new Item.Properties().setId(itemId(identifier));
    }

    private static ResourceKey<Item> itemId(ResourceLocation identifier) {
        return ResourceKey.create(Registries.ITEM, identifier);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, path);
    }

}
