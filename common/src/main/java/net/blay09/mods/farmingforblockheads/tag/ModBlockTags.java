package net.blay09.mods.farmingforblockheads.tag;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> FERTILIZED_FARMLAND = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "fertilized_farmland"));
    public static final TagKey<Block> RICH_FARMLAND = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "rich_farmland"));
    public static final TagKey<Block> HEALTHY_FARMLAND = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "healthy_farmland"));
    public static final TagKey<Block> STABLE_FARMLAND = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "stable_farmland"));
}
