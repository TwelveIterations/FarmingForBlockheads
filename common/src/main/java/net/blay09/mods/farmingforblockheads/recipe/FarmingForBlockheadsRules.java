package net.blay09.mods.farmingforblockheads.recipe;

import net.blay09.mods.farmingforblockheads.block.entity.ShippingBinBlockEntity;
import net.blay09.mods.farmingforblockheads.block.entity.SprinklerBlockEntity;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.context.ShogiContext;
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
    public static final ShogiValue<SprinklerBlockEntity, ?> WATER_SPRINKLER_ENTITY_EFFECTS =
            SCOPE.maybe(id("sprinkler/water/entity_effects"), SprinklerBlockEntity::extinguishEntities);
    public static final ShogiValue<SprinklerBlockEntity, ?> LAVA_SPRINKLER_ENTITY_EFFECTS =
            SCOPE.maybe(id("sprinkler/lava/entity_effects"), SprinklerBlockEntity::igniteEntities);
    public static final ShogiValue<SprinklerBlockEntity, ?> HONEY_SPRINKLER_ENTITY_EFFECTS =
            SCOPE.maybe(id("sprinkler/honey/entity_effects"), SprinklerBlockEntity::removePoison);
    public static final ShogiValue<SprinklerBlockEntity, ?> SLIME_SPRINKLER_ENTITY_EFFECTS =
            SCOPE.maybe(id("sprinkler/slime/entity_effects"), SprinklerBlockEntity::slowEntities);
    public static final ShogiValue<SprinklerBlockEntity, ?> SNOW_SPRINKLER_ENTITY_EFFECTS =
            SCOPE.maybe(id("sprinkler/snow/entity_effects"), SprinklerBlockEntity::freezeEntities);
    public static final ShogiValue<SprinklerBlockEntity, ?> SULFUR_SPRINKLER_ENTITY_EFFECTS =
            SCOPE.maybe(id("sprinkler/sulfur/entity_effects"), SprinklerBlockEntity::nauseateEntities);
    public static final ShogiValue<SprinklerBlockEntity, Boolean> LAVA_SPRINKLER_CAN_MELT =
            SCOPE.booleanValue(id("sprinkler/lava/can_melt"), _ -> true);
    public static final ShogiValue<ShogiContext, Boolean> LAVA_SPRINKLER_CAN_MELT_AT =
            SCOPE.booleanValue(id("sprinkler/lava/can_melt_at"), _ -> true);
    public static final ShogiValue<SprinklerBlockEntity, Boolean> LAVA_SPRINKLER_CAN_IGNITE =
            SCOPE.booleanValue(id("sprinkler/lava/can_ignite"), _ -> true);
    public static final ShogiValue<ShogiContext, Boolean> LAVA_SPRINKLER_CAN_IGNITE_AT =
            SCOPE.booleanValue(id("sprinkler/lava/can_ignite_at"), _ -> true);
    public static final ShogiValue<SprinklerBlockEntity, Boolean> SNOW_SPRINKLER_CAN_FREEZE =
            SCOPE.booleanValue(id("sprinkler/snow/can_freeze"), _ -> true);
    public static final ShogiValue<ShogiContext, Boolean> SNOW_SPRINKLER_CAN_FREEZE_AT =
            SCOPE.booleanValue(id("sprinkler/snow/can_freeze_at"), _ -> true);
    public static final ShogiValue<SprinklerBlockEntity, Boolean> SNOW_SPRINKLER_CAN_CREATE_SNOW =
            SCOPE.booleanValue(id("sprinkler/snow/can_create_snow"), _ -> true);
    public static final ShogiValue<ShogiContext, Boolean> SNOW_SPRINKLER_CAN_CREATE_SNOW_AT =
            SCOPE.booleanValue(id("sprinkler/snow/can_create_snow_at"), _ -> true);

    private FarmingForBlockheadsRules() {
    }

    public static int getShippingBinCapacity(ShippingBinBlockEntity blockEntity) {
        return Math.max(1, SHIPPING_BIN_CAPACITY.getOrDefault(blockEntity));
    }

}
