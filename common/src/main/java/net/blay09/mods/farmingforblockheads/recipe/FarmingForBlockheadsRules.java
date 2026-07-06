package net.blay09.mods.farmingforblockheads.recipe;

import net.blay09.mods.farmingforblockheads.block.entity.ShippingBinBlockEntity;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.scope.ShogiScope;

import java.util.List;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public final class FarmingForBlockheadsRules {
    public static final ShogiScope SCOPE = Shogi.scope(id("rules"), scope -> {
        scope.setDefaultNamespaces(List.of("farmingforblockheads", "shogi"));
        ShippingBinSalesEffects.register(scope);
    });

    public static final ShogiValue<ShippingBinBlockEntity, Integer> SHIPPING_BIN_CAPACITY =
            SCOPE.intValue(id("shipping_bin/capacity"), _ -> 128);

    private FarmingForBlockheadsRules() {
    }

    public static int getShippingBinCapacity(ShippingBinBlockEntity blockEntity) {
        return Math.max(1, SHIPPING_BIN_CAPACITY.getOrDefault(blockEntity));
    }

}
