package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetLoreFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModGameplayLootTableProvider extends SimpleFabricLootTableSubProvider {

    private static final ResourceKey<LootTable> RABBIT_TRAP = ResourceKey.create(Registries.LOOT_TABLE, FarmingForBlockheads.id("gameplay/rabbit_trap"));

    protected ModGameplayLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture, LootContextParamSets.CHEST);
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter) {
        exporter.accept(RABBIT_TRAP, LootTable.lootTable()
                .setRandomSequence(FarmingForBlockheads.id("rabbit_trap"))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(1 / 2000f))
                        .add(LootItem.lootTableItem(Items.PAPER)
                                .apply(SetNameFunction.setName(Component.translatable("item.farmingforblockheads.fox_note").withStyle(style -> style.withItalic(false)), SetNameFunction.Target.CUSTOM_NAME))
                                .apply(SetLoreFunction.setLore()
                                        .addLine(Component.translatable("item.farmingforblockheads.fox_note.lore.1").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
                                        .addLine(Component.translatable("item.farmingforblockheads.fox_note.lore.2").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY).withItalic(true)))))));
    }
}
