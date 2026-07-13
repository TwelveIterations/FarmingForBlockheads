package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        valueLookupBuilder(ModItemTags.FISHING_RODS)
                .add(Items.FISHING_ROD)
                .addOptionalTag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/fishing_rods")));
        valueLookupBuilder(ModItemTags.RICH_FARMLAND).add(ModBlocks.fertilizedFarmlandRichStable.asItem(), ModBlocks.fertilizedFarmlandRich.asItem());
        valueLookupBuilder(ModItemTags.HEALTHY_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asItem(),
                ModBlocks.fertilizedFarmlandHealthy.asItem());
        valueLookupBuilder(ModItemTags.STABLE_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asItem(),
                ModBlocks.fertilizedFarmlandRichStable.asItem(),
                ModBlocks.fertilizedFarmlandStable.asItem());

        valueLookupBuilder(ModItemTags.FERTILIZED_FARMLAND).add(ModBlocks.fertilizedFarmlandHealthyStable.asItem(),
                ModBlocks.fertilizedFarmlandRichStable.asItem(),
                ModBlocks.fertilizedFarmlandStable.asItem(),
                ModBlocks.fertilizedFarmlandHealthy.asItem(),
                ModBlocks.fertilizedFarmlandRich.asItem());


        valueLookupBuilder(ModItemTags.SPRINKLER_TOPS).add(
                Items.CARVED_PUMPKIN,
                Items.JACK_O_LANTERN,
                Items.PLAYER_HEAD,
                Items.CREEPER_HEAD,
                Items.DRAGON_HEAD,
                Items.PIGLIN_HEAD,
                Items.ZOMBIE_HEAD
        );
    }
}
