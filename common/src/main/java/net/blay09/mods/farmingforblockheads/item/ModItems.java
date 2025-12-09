package net.blay09.mods.farmingforblockheads.item;

import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.minecraft.network.chat.Component;

public class ModItems {

    public static DeferredItem greenFertilizer;
    public static DeferredItem redFertilizer;
    public static DeferredItem yellowFertilizer;

    public static void initialize(BalmItemRegistrar items) {
        greenFertilizer = items.register("green_fertilizer", (properties) -> new FertilizerItem(properties, FertilizerItem.FertilizerType.RICH)).asDeferredItem();
        redFertilizer = items.register("red_fertilizer", (properties) -> new FertilizerItem(properties, FertilizerItem.FertilizerType.HEALTHY)).asDeferredItem();
        yellowFertilizer = items.register("yellow_fertilizer", (properties) -> new FertilizerItem(properties, FertilizerItem.FertilizerType.STABLE)).asDeferredItem();
    }

    public static void initialize(BalmCreativeModeTabRegistrar creativeModeTabs) {
        creativeModeTabs.register(FarmingForBlockheads.MOD_ID, (id, builder) ->
                builder.title(Component.translatable(id.toLanguageKey("itemGroup")))
                        .icon(() -> ModBlocks.market.createStack())
                        .displayItems((parameters, output) -> {
                            output.accept(greenFertilizer);
                            output.accept(redFertilizer);
                            output.accept(yellowFertilizer);
                        })
        );
    }

}
