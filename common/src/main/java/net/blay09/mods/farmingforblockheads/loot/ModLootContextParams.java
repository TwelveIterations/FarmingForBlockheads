package net.blay09.mods.farmingforblockheads.loot;

import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModLootContextParams {
    public static final ContextKey<Integer> SHIPMENT_VALUE = new ContextKey<>(id("shipment_value"));
    public static final ContextKey<List<ItemStack>> SHIPMENT_ITEMS = new ContextKey<>(id("shipment_items"));

    public static final ContextKeySet SHIPPING_BIN_CONTEXT = new ContextKeySet.Builder()
            .required(LootContextParams.ORIGIN)
            .required(ModLootContextParams.SHIPMENT_VALUE)
            .required(ModLootContextParams.SHIPMENT_ITEMS)
            .build();
}
