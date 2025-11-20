package net.blay09.mods.farmingforblockheads.entity;

import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static Holder<EntityType<MerchantEntity>> merchant;

    public static void initialize(BalmEntityTypeRegistrar entities) {
        merchant = entities.register("merchant", () -> EntityType.Builder.of(MerchantEntity::new, MobCategory.MISC).sized(0.6f, 1.95f))
                .withDefaultAttributes(MerchantEntity::createAttributes)
                .asHolder();
    }

}
