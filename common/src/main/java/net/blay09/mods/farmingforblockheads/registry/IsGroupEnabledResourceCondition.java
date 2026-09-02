package net.blay09.mods.farmingforblockheads.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.balm.server.packs.resources.BalmResourceCondition;
import net.blay09.mods.balm.server.packs.resources.ResourceConditionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record IsGroupEnabledResourceCondition(String group) implements BalmResourceCondition {

    private static final Logger logger = LoggerFactory.getLogger(IsGroupEnabledResourceCondition.class);

    public static final MapCodec<IsGroupEnabledResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("group").forGetter(IsGroupEnabledResourceCondition::group)
    ).apply(instance, IsGroupEnabledResourceCondition::new));

    @Override
    public boolean test(ResourceConditionContext context) {
        logger.error("Resource load condition `farmingforblockheads:is_group_enabled` is no longer supported and will always pass. Tested group: {}", group);
        return true;
    }
}
