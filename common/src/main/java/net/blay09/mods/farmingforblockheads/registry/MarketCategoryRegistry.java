package net.blay09.mods.farmingforblockheads.registry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.farmingforblockheads.api.*;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.io.BufferedReader;
import java.util.*;

public class MarketCategoryRegistry {

    private static final Codec<MarketCategory> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("icon").forGetter(MarketCategory::iconStack),
            ExtraCodecs.POSITIVE_INT.fieldOf("sortIndex").orElse(0).forGetter(MarketCategory::sortIndex),
            ComponentSerialization.CODEC.fieldOf("tooltip").forGetter(MarketCategory::tooltip)
    ).apply(instance, MarketCategoryImpl::new));

    public static final MarketCategoryRegistry INSTANCE = new MarketCategoryRegistry();

    private final Map<Identifier, MarketCategory> categories = new HashMap<>();

    public void register(Identifier id, MarketCategory category) {
        categories.put(id, category);
    }

    public Map<Identifier, MarketCategory> getAll() {
        return INSTANCE.categories;
    }

    public Optional<MarketCategory> get(Identifier id) {
        return Optional.ofNullable(INSTANCE.categories.get(id));
    }

    public void clear() {
        categories.clear();
    }

    public void loadAdditionally(Identifier id, BufferedReader reader) {
        final var gson = new Gson();
        final var json = gson.fromJson(reader, JsonElement.class);
        final var category = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        register(id, category);
    }

    public void load(Map<Identifier, MarketCategory> categories) {
        this.categories.clear();
        categories.forEach(this::register);
    }
}
