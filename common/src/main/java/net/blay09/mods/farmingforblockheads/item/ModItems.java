package net.blay09.mods.farmingforblockheads.item;

import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ToolMaterial;

public class ModItems {

    public static DeferredItem greenFertilizer;
    public static DeferredItem redFertilizer;
    public static DeferredItem yellowFertilizer;
    public static DeferredItem scythe;
    public static DeferredItem wateringCan;

    public static void initialize(BalmItemRegistrar items) {
        greenFertilizer = items.register("green_fertilizer", (properties) -> new FertilizerItem(properties, FertilizerItem.FertilizerType.RICH)).asDeferredItem();
        redFertilizer = items.register("red_fertilizer", (properties) -> new FertilizerItem(properties, FertilizerItem.FertilizerType.HEALTHY)).asDeferredItem();
        yellowFertilizer = items.register("yellow_fertilizer", (properties) -> new FertilizerItem(properties, FertilizerItem.FertilizerType.STABLE)).asDeferredItem();
        scythe = items.register("iron_scythe", ScytheItem::new, properties -> properties.hoe(ToolMaterial.IRON, 0f, -3f)).asDeferredItem();
        wateringCan = items.register("watering_can", WateringCanItem::new, properties -> properties.durability(256)).asDeferredItem();
    }

    public static void initialize(BalmCreativeModeTabRegistrar creativeModeTabs) {
        creativeModeTabs.register(FarmingForBlockheads.MOD_ID, (id, builder) ->
                builder.title(Component.translatable(id.toLanguageKey("itemGroup")))
                        .icon(() -> ModBlocks.market.createStack())
                        .displayItems((parameters, output) -> {
                            output.accept(ModBlocks.market);
                            output.accept(ModBlocks.shippingBin);
                            output.accept(ModBlocks.chickenNest);
                            output.accept(ModBlocks.feedingTrough);
                            output.accept(ModBlocks.sprinkler);
                            output.accept(ModBlocks.fishingBarrel);
                            output.accept(ModBlocks.rabbitTrap);
                            output.accept(greenFertilizer);
                            output.accept(redFertilizer);
                            output.accept(yellowFertilizer);
                            output.accept(scythe);
                            output.accept(wateringCan);
                        })
        );
    }

}
