package net.blay09.mods.farmingforblockheads.registry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.api.MarketDefault;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.io.BufferedReader;
import java.util.*;

public class MarketDefaultsRegistry {

    private static final Codec<MarketDefault> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.BOOL.optionalFieldOf("enabledByDefault").forGetter(MarketDefault::enabledByDefault),
            Identifier.CODEC.optionalFieldOf("category").forGetter(MarketDefault::category),
            PaymentImpl.CODEC.optionalFieldOf("payment").forGetter(MarketDefault::payment)
    ).apply(instance, MarketDefaultImpl::new));

    public static final MarketDefaultsRegistry INSTANCE = new MarketDefaultsRegistry();
    private static final MarketDefault EMPTY_DEFAULT = new MarketDefaultImpl(Optional.empty(), Optional.empty(), Optional.empty());

    private final Map<String, MarketDefault> defaultsByGroup = new HashMap<>();

    public void register(String group, MarketDefault preset) {
        defaultsByGroup.put(group, preset);
    }

    public void clear() {
        defaultsByGroup.clear();
    }

    public void loadAdditionally(HolderLookup.Provider registries, BufferedReader reader) {
        final var gson = new Gson();
        final var json = gson.fromJson(reader, JsonObject.class);
        final var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        for (final var entry : json.entrySet()) {
            final var defaults = CODEC.parse(ops, entry.getValue()).getOrThrow();
            register(entry.getKey(), defaults);
        }
    }

    public static MarketDefault resolveExactDefault(String defaults) {
        return INSTANCE.defaultsByGroup.getOrDefault(defaults, EMPTY_DEFAULT);
    }

    public static List<String> flattenDefaults(String defaults) {
        final var result = new ArrayList<String>();
        final var parts = defaults.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            final var sb = new StringBuilder();
            for (int j = 0; j <= i; j++) {
                if (j > 0) {
                    sb.append('.');
                }
                sb.append(parts[j]);
            }
            final var key = sb.toString();
            result.add(key);
        }
        return result;
    }

    public static MarketDefault resolveDefaults(String defaultsPath) {
        final var result = flattenDefaults(defaultsPath).stream().map(MarketDefaultsRegistry::resolveExactDefault).toList();
        return new CompositeMarketDefault(result);
    }

    public static MarketDefault resolveDefaults(MarketRecipeImpl recipe) {
        return resolveDefaults(recipe.getDefaults());
    }

    public static Identifier resolveCategory(MarketRecipeImpl recipe) {
        final var defaults = resolveDefaults(recipe);
        return recipe.getCategory().orElse(defaults.category().orElseGet(MarketDefaultsRegistry::defaultCategory));
    }

    public static Payment resolvePayment(MarketRecipeImpl recipe) {
        final var defaults = resolveDefaults(recipe);
        return recipe.getPayment().orElse(defaults.payment().orElseGet(MarketDefaultsRegistry::defaultPayment));
    }

    public static boolean isEnabled(String groupPath) {
        final var defaults = resolveDefaults(groupPath);
        final var recipeGroups = flattenDefaults(groupPath);
        final var includedGroups = FarmingForBlockheadsConfig.getActive().includedGroups;
        final var excludedGroups = FarmingForBlockheadsConfig.getActive().excludedGroups;
        final var useDefaultIncludedGroups = includedGroups.contains("default") && !excludedGroups.contains("default");
        var enabled = false;
        for (final var group : recipeGroups.reversed()) {
            if (excludedGroups.contains(group)) {
                break;
            } else if (includedGroups.contains(group)) {
                enabled = true;
                break;
            } else if (useDefaultIncludedGroups && defaults.enabledByDefault().orElse(false)) {
                enabled = true;
                break;
            }
        }

        return enabled;
    }

    private static Identifier defaultCategory() {
        return Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "other");
    }

    private static Payment defaultPayment() {
        return new PaymentImpl(Ingredient.of(Items.EMERALD), 1, Optional.empty());
    }
}
