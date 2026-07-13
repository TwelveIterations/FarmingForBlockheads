package net.blay09.mods.farmingforblockheads.recipe;

import net.blay09.mods.farmingforblockheads.block.entity.FishingBarrelBlockEntity;
import net.blay09.mods.farmingforblockheads.block.entity.ShippingBinBlockEntity;
import net.blay09.mods.farmingforblockheads.block.entity.SprinklerBlockEntity;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

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
    public static final ShogiValue<ShogiContext, Integer> FISHING_BARREL_INTERVAL =
            SCOPE.intValue(id("fishing_barrel/interval"), FarmingForBlockheadsRules::getDefaultFishingBarrelInterval);

    private FarmingForBlockheadsRules() {
    }

    public static int getShippingBinCapacity(ShippingBinBlockEntity blockEntity) {
        return Math.max(1, SHIPPING_BIN_CAPACITY.getOrDefault(blockEntity));
    }

    public static int getFishingBarrelInterval(FishingBarrelBlockEntity blockEntity, ItemStack rod) {
        final var context = MutableShogiContext.of(blockEntity).withItemStack(rod);
        return Math.max(40, FISHING_BARREL_INTERVAL.getOrDefault(context));
    }

    private static int getDefaultFishingBarrelInterval(ShogiContext context) {
        final var level = context.requireLevel();
        final var lure = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.LURE)
                .map(it -> EnchantmentHelper.getItemEnchantmentLevel(it, context.itemStack()))
                .orElse(0);
        return 100 + level.getRandom().nextInt(501) - lure * 100;
    }

}
