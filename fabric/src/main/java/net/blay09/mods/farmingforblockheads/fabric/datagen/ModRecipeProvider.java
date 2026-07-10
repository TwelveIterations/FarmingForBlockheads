package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.balm.tags.BalmItemTags;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.MISC, ModItems.yellowFertilizer, 4)
                        .pattern("GGG")
                        .pattern("NSN")
                        .pattern("DDD")
                        .define('G', BalmItemTags.YELLOW_DYES)
                        .define('N', BalmItemTags.GOLD_NUGGETS)
                        .define('S', Items.WHEAT_SEEDS)
                        .define('D', Items.DIRT)
                        .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                        .save(exporter);

                shaped(RecipeCategory.MISC, ModItems.redFertilizer, 4)
                        .pattern("RRR")
                        .pattern("NSN")
                        .pattern("BBB")
                        .define('R', BalmItemTags.RED_DYES)
                        .define('N', Items.GOLD_NUGGET)
                        .define('S', Items.WHEAT_SEEDS)
                        .define('B', Items.BONE_MEAL)
                        .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                        .save(exporter);

                shaped(RecipeCategory.MISC, ModItems.greenFertilizer, 4)
                        .pattern("GGG")
                        .pattern("NSN")
                        .pattern("WWW")
                        .define('G', BalmItemTags.GREEN_DYES)
                        .define('N', Items.GOLD_NUGGET)
                        .define('S', Items.WHEAT_SEEDS)
                        .define('W', Items.WHEAT)
                        .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                        .save(exporter);

                shaped(RecipeCategory.TOOLS, ModItems.scythe)
                        .pattern("III")
                        .pattern(" SI")
                        .pattern(" S ")
                        .define('I', Items.IRON_INGOT)
                        .define('S', Items.STICK)
                        .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                        .save(exporter);

                shaped(RecipeCategory.TOOLS, ModItems.wateringCan)
                        .pattern(" I ")
                        .pattern("I I")
                        .pattern(" B ")
                        .define('I', Items.IRON_INGOT)
                        .define('B', Items.WATER_BUCKET)
                        .unlockedBy("has_water_bucket", has(Items.WATER_BUCKET))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.chickenNest)
                        .pattern("PHP")
                        .define('H', Items.HAY_BLOCK)
                        .define('P', ItemTags.PLANKS)
                        .unlockedBy("has_wheat", has(Items.WHEAT))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.market)
                        .pattern("PCP")
                        .pattern("W W")
                        .pattern("WWW")
                        .define('C', Items.RED_WOOL)
                        .define('P', ItemTags.PLANKS)
                        .define('W', ItemTags.LOGS)
                        .unlockedBy("has_wool", has(ItemTags.WOOL))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.feedingTrough)
                        .pattern("PCP")
                        .pattern("PHP")
                        .define('H', Items.HAY_BLOCK)
                        .define('P', ItemTags.PLANKS)
                        .define('C', Items.GOLDEN_CARROT)
                        .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.sprinkler)
                        .pattern("IBI")
                        .pattern(" W ")
                        .pattern("CRC")
                        .define('I', Items.IRON_INGOT)
                        .define('W', ItemTags.LOGS)
                        .define('B', Items.WATER_BUCKET)
                        .define('C', Items.COBBLESTONE)
                        .define('R', Items.REDSTONE)
                        .unlockedBy("has_redstone", has(Items.REDSTONE))
                        .save(exporter);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.shippingBin)
                        .pattern("IWI")
                        .pattern("WEW")
                        .pattern("IGI")
                        .define('I', Items.IRON_INGOT)
                        .define('W', ItemTags.LOGS)
                        .define('E', Items.EMERALD)
                        .define('G', Items.GREEN_WOOL)
                        .unlockedBy("has_wool", has(ItemTags.WOOL))
                        .save(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return FarmingForBlockheads.MOD_ID;
    }
}
