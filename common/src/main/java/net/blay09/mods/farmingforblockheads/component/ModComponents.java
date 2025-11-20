package net.blay09.mods.farmingforblockheads.component;

import net.blay09.mods.balm.core.component.BalmDataComponentTypeRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;

public class ModComponents {
    public static Holder<DataComponentType<DescriptionComponent>> description;

    public static void initialize(BalmDataComponentTypeRegistrar components) {
        description = components.register("description", DescriptionComponent.CODEC).asHolder();
    }
}

