package net.blay09.mods.farmingforblockheads.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModItemTags {
    public static final TagKey<Item> FISHING_RODS = TagKey.create(Registries.ITEM, id("fishing_rods"));
    public static final TagKey<Item> FERTILIZED_FARMLAND = TagKey.create(Registries.ITEM, id("fertilized_farmland"));
    public static final TagKey<Item> RICH_FARMLAND = TagKey.create(Registries.ITEM, id("rich_farmland"));
    public static final TagKey<Item> HEALTHY_FARMLAND = TagKey.create(Registries.ITEM, id("healthy_farmland"));
    public static final TagKey<Item> STABLE_FARMLAND = TagKey.create(Registries.ITEM, id("stable_farmland"));
    public static final TagKey<Item> SPRINKLER_TOPS = TagKey.create(Registries.ITEM, id("sprinkler_tops"));
}
