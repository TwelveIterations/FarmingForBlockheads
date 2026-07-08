package net.blay09.mods.farmingforblockheads.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ShipmentContainsCondition(ItemPredicate item) implements LootItemCondition {

    public static final MapCodec<ShipmentContainsCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPredicate.CODEC.fieldOf("item").forGetter(ShipmentContainsCondition::item)
    ).apply(instance, ShipmentContainsCondition::new));

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return ModLootConditions.shipmentContains.value();
    }

    @Override
    public boolean test(LootContext context) {
        final var soldItems = context.getOptionalParameter(ModLootContextParams.SHIPMENT_ITEMS);
        return soldItems != null && soldItems.stream().anyMatch(item);
    }
}
