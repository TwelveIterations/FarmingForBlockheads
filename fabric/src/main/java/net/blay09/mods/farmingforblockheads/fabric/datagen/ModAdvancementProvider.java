package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.minecraft.data.advancements.AdvancementSubProvider.createPlaceholder;

public class ModAdvancementProvider extends FabricAdvancementProvider {

    public ModAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
        Advancement.Builder.advancement()
                .parent(createPlaceholder("husbandry/root"))
                .display(ModBlocks.shippingBin.asItem(),
                        Component.translatable("advancements.farmingforblockheads.lost_in_transit.title"),
                        Component.translatable("advancements.farmingforblockheads.lost_in_transit.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        true)
                .addCriterion("lost_in_transit", new Criterion<>(CriteriaTriggers.IMPOSSIBLE, new ImpossibleTrigger.TriggerInstance()))
                .save(consumer, FarmingForBlockheads.id("lost_in_transit").toString());
    }
}
