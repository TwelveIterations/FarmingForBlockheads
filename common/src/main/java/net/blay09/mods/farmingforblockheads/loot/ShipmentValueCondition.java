package net.blay09.mods.farmingforblockheads.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record ShipmentValueCondition(Optional<Integer> min, Optional<Integer> max) implements LootItemCondition {

    public static final MapCodec<ShipmentValueCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("min").forGetter(ShipmentValueCondition::min),
            Codec.INT.optionalFieldOf("max").forGetter(ShipmentValueCondition::max)
    ).apply(instance, ShipmentValueCondition::new));

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return ModLootConditions.shipmentValue.value();
    }

    @Override
    public boolean test(LootContext context) {
        final var value = context.getOptionalParameter(ModLootContextParams.SHIPMENT_VALUE);
        if (value == null) {
            return false;
        }

        return min.map(it -> value >= it).orElse(true) && max.map(it -> value <= it).orElse(true);
    }
}
