package net.blay09.mods.farmingforblockheads.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.balm.server.packs.resources.BalmResourceCondition;
import net.blay09.mods.balm.server.packs.resources.ResourceConditionContext;

public record IsGroupEnabledResourceCondition(String group) implements BalmResourceCondition {

    public static final MapCodec<IsGroupEnabledResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("group").forGetter(IsGroupEnabledResourceCondition::group)
    ).apply(instance, IsGroupEnabledResourceCondition::new));

    @Override
    public boolean test(ResourceConditionContext context) {
        return MarketDefaultsRegistry.isEnabled(group);
    }
}
