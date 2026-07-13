package net.blay09.mods.farmingforblockheads.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModEntityTypeTags {
    public static final TagKey<EntityType<?>> RABBIT_TRAP_CAPTURABLE = TagKey.create(Registries.ENTITY_TYPE, id("rabbit_trap_capturable"));
    public static final TagKey<EntityType<?>> RABBIT_TRAP_SPAWNABLE = TagKey.create(Registries.ENTITY_TYPE, id("rabbit_trap_spawnable"));
}
