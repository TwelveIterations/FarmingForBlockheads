package net.blay09.mods.farmingforblockheads.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModBlockTags {
    public static final TagKey<Block> FERTILIZED_FARMLAND = TagKey.create(Registries.BLOCK, id("fertilized_farmland"));
    public static final TagKey<Block> RICH_FARMLAND = TagKey.create(Registries.BLOCK, id("rich_farmland"));
    public static final TagKey<Block> HEALTHY_FARMLAND = TagKey.create(Registries.BLOCK, id("healthy_farmland"));
    public static final TagKey<Block> STABLE_FARMLAND = TagKey.create(Registries.BLOCK, id("stable_farmland"));
    public static final TagKey<Block> LAVA_SPRINKLER_BASE = TagKey.create(Registries.BLOCK, id("lava_sprinkler_base"));
    public static final TagKey<Block> HONEY_SPRINKLER_BASE = TagKey.create(Registries.BLOCK, id("honey_sprinkler_base"));
    public static final TagKey<Block> SLIME_SPRINKLER_BASE = TagKey.create(Registries.BLOCK, id("slime_sprinkler_base"));
    public static final TagKey<Block> SNOW_SPRINKLER_BASE = TagKey.create(Registries.BLOCK, id("snow_sprinkler_base"));
    public static final TagKey<Block> SULFUR_SPRINKLER_BASE = TagKey.create(Registries.BLOCK, id("sulfur_sprinkler_base"));
    public static final TagKey<Block> SCULK_SPRINKLER_BASE = TagKey.create(Registries.BLOCK, id("sculk_sprinkler_base"));
}
