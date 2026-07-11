package net.blay09.mods.farmingforblockheads.mixin;

import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.ItemModelPropertyTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConditionalItemModel.class)
public interface ConditionalItemModelAccessor {
    @Accessor
    ItemModelPropertyTest getProperty();

    @Accessor
    ItemModel getOnTrue();
}
