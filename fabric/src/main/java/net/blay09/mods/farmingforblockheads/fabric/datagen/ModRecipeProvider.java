package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.advancements.Advancement;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        return new RecipeProvider(recipes, advancements) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.MISC, ModItems.yellowFertilizer, 4)
                        .pattern("GGG")
                        .pattern("NSN")
                        .pattern("DDD")
                        .define('G', Items.DYE.yellow())
                        .define('N', Items.GOLD_NUGGET)
                        .define('S', Items.WHEAT_SEEDS)
                        .define('D', Items.DIRT)
                        .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                        .save(output);

                shaped(RecipeCategory.MISC, ModItems.redFertilizer, 4)
                        .pattern("RRR")
                        .pattern("NSN")
                        .pattern("BBB")
                        .define('R', Items.DYE.red())
                        .define('N', Items.GOLD_NUGGET)
                        .define('S', Items.WHEAT_SEEDS)
                        .define('B', Items.BONE_MEAL)
                        .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                        .save(output);

                shaped(RecipeCategory.MISC, ModItems.greenFertilizer, 4)
                        .pattern("GGG")
                        .pattern("NSN")
                        .pattern("WWW")
                        .define('G', Items.DYE.green())
                        .define('N', Items.GOLD_NUGGET)
                        .define('S', Items.WHEAT_SEEDS)
                        .define('W', Items.WHEAT)
                        .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                        .save(output);

                shaped(RecipeCategory.TOOLS, ModItems.scythe)
                        .pattern("III")
                        .pattern(" SI")
                        .pattern(" S ")
                        .define('I', Items.IRON_INGOT)
                        .define('S', Items.STICK)
                        .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                        .save(output);

                shaped(RecipeCategory.TOOLS, ModItems.wateringCan)
                        .pattern(" I ")
                        .pattern("I I")
                        .pattern(" B ")
                        .define('I', Items.IRON_INGOT)
                        .define('B', Items.WATER_BUCKET)
                        .unlockedBy("has_water_bucket", has(Items.WATER_BUCKET))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.chickenNest)
                        .pattern("PHP")
                        .define('H', Items.HAY_BLOCK)
                        .define('P', ItemTags.PLANKS)
                        .unlockedBy("has_wheat", has(Items.WHEAT))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.rabbitTrap)
                        .pattern(" PP")
                        .pattern("SCP")
                        .define('S', Items.STICK)
                        .define('P', ItemTags.PLANKS)
                        .define('C', Items.CARROT)
                        .unlockedBy("has_carrot", has(Items.CARROT))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.market)
                        .pattern("PCP")
                        .pattern("W W")
                        .pattern("WWW")
                        .define('C', Items.WOOL.red())
                        .define('P', ItemTags.PLANKS)
                        .define('W', ItemTags.LOGS)
                        .unlockedBy("has_wool", has(ItemTags.WOOL))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.feedingTrough)
                        .pattern("PCP")
                        .pattern("PHP")
                        .define('H', Items.HAY_BLOCK)
                        .define('P', ItemTags.PLANKS)
                        .define('C', Items.GOLDEN_CARROT)
                        .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                        .save(output);

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
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.shippingBin)
                        .pattern("IWI")
                        .pattern("WEW")
                        .pattern("IGI")
                        .define('I', Items.IRON_INGOT)
                        .define('W', ItemTags.LOGS)
                        .define('E', Items.EMERALD)
                        .define('G', Items.WOOL.green())
                        .unlockedBy("has_wool", has(ItemTags.WOOL))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, ModBlocks.fishingBarrel)
                        .pattern("PSP")
                        .pattern("PNP")
                        .pattern("PRP")
                        .define('P', ItemTags.PLANKS)
                        .define('S', Items.STRING)
                        .define('N', Items.NAUTILUS_SHELL)
                        .define('R', Items.REDSTONE)
                        .unlockedBy("has_nautilus_shell", has(Items.NAUTILUS_SHELL))
                        .save(output);
            }
        };
    }

    @Override
    public String getName() {
        return FarmingForBlockheads.MOD_ID;
    }
}
