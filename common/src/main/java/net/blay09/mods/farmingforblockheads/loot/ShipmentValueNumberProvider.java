package net.blay09.mods.farmingforblockheads.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.Set;

public record ShipmentValueNumberProvider(LevelBasedValue amount) implements NumberProvider {
    public static final MapCodec<ShipmentValueNumberProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(ShipmentValueNumberProvider::amount)
    ).apply(instance, ShipmentValueNumberProvider::new));

    @Override
    public MapCodec<? extends NumberProvider> codec() {
        return ModLootNumberProviders.shipmentValue.value();
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(ModLootContextParams.SHIPMENT_VALUE);
    }

    @Override
    public float getFloat(LootContext context) {
        final var value = context.getOptionalParameter(ModLootContextParams.SHIPMENT_VALUE);
        return value != null ? amount.calculate(value) : 0f;
    }
}
