package net.blay09.mods.farmingforblockheads.tag;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> FERTILIZED_FARMLAND = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "fertilized_farmland"));
    public static final TagKey<Item> RICH_FARMLAND = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "rich_farmland"));
    public static final TagKey<Item> HEALTHY_FARMLAND = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "healthy_farmland"));
    public static final TagKey<Item> STABLE_FARMLAND = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "stable_farmland"));
}
