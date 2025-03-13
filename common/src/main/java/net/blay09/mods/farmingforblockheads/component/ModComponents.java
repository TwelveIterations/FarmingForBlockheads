package net.blay09.mods.farmingforblockheads.component;

import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.component.BalmComponents;
import net.minecraft.core.component.DataComponentType;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModComponents {
    public static DeferredObject<DataComponentType<DescriptionComponent>> description;

    public static void initialize(BalmComponents components) {
        description = components.registerComponent(() -> DataComponentType.<DescriptionComponent>builder().persistent(DescriptionComponent.CODEC).build(),
                id("description"));
    }
}

