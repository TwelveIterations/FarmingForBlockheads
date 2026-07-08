package net.blay09.mods.farmingforblockheads.loot;

import com.mojang.serialization.MapCodec;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class ModLootNumberProviders {
    public static Holder<MapCodec<? extends NumberProvider>> shipmentValue;

    public static void initialize(BalmRegistrar.Scoped<MapCodec<? extends NumberProvider>> registry) {
        shipmentValue = registry.register("shipment_value", _ -> ShipmentValueNumberProvider.MAP_CODEC);
    }
}
