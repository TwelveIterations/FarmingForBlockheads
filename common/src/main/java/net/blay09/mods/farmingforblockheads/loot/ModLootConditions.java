package net.blay09.mods.farmingforblockheads.loot;

import com.mojang.serialization.MapCodec;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ModLootConditions {
    public static Holder<MapCodec<? extends LootItemCondition>> shipmentValue;
    public static Holder<MapCodec<? extends LootItemCondition>> shipmentContains;

    public static void initialize(BalmRegistrar.Scoped<MapCodec<? extends LootItemCondition>> registry) {
        shipmentValue = registry.register("shipment_value", _ -> ShipmentValueCondition.MAP_CODEC);
        shipmentContains = registry.register("shipment_contains", _ -> ShipmentContainsCondition.MAP_CODEC);
    }
}
