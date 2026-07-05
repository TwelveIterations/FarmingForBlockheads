package net.blay09.mods.farmingforblockheads.entity;

import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static Holder<EntityType<MerchantEntity>> merchant;
    public static Holder<EntityType<ShippingBalloonEntity>> shippingBalloon;
    public static Holder<EntityType<FallingShippingCrateEntity>> fallingShippingCrate;

    public static void initialize(BalmEntityTypeRegistrar entities) {
        merchant = entities.register("merchant", () -> EntityType.Builder.of(MerchantEntity::new, MobCategory.MISC).sized(0.6f, 1.95f))
                .withDefaultAttributes(MerchantEntity::createAttributes)
                .asHolder();
        shippingBalloon = entities.register("shipping_balloon", () -> EntityType.Builder.of(ShippingBalloonEntity::new, MobCategory.MISC)
                .sized(1.4f, 2.7f)
                .clientTrackingRange(8)
                .updateInterval(20)
                .noSave()).asHolder();
        fallingShippingCrate = entities.register("falling_shipping_crate", () -> EntityType.Builder.of(FallingShippingCrateEntity::new, MobCategory.MISC)
                .sized(0.9f, 0.9f)
                .clientTrackingRange(8)
                .updateInterval(2)).asHolder();
    }

}
