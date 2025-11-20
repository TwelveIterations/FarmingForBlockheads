package net.blay09.mods.farmingforblockheads.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record MarketRecipeDisplay(SlotDisplay payment, SlotDisplay result, SlotDisplay craftingStation, Identifier category,
                                  int sortIndex,
                                  boolean enabled,
                                  SlotDisplay icon) implements RecipeDisplay {
    public static final MapCodec<MarketRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            SlotDisplay.CODEC.fieldOf("payment").forGetter(MarketRecipeDisplay::payment),
            SlotDisplay.CODEC.fieldOf("result").forGetter(MarketRecipeDisplay::result),
            SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(MarketRecipeDisplay::craftingStation),
            Identifier.CODEC.fieldOf("category").forGetter(MarketRecipeDisplay::category),
            Codec.INT.fieldOf("sort_index").forGetter(MarketRecipeDisplay::sortIndex),
            Codec.BOOL.fieldOf("enabled").forGetter(MarketRecipeDisplay::enabled),
            SlotDisplay.CODEC.fieldOf("icon").forGetter(MarketRecipeDisplay::result)
    ).apply(instance, MarketRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MarketRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC,
            MarketRecipeDisplay::payment,
            SlotDisplay.STREAM_CODEC,
            MarketRecipeDisplay::result,
            SlotDisplay.STREAM_CODEC,
            MarketRecipeDisplay::craftingStation,
            Identifier.STREAM_CODEC,
            MarketRecipeDisplay::category,
            ByteBufCodecs.INT,
            MarketRecipeDisplay::sortIndex,
            ByteBufCodecs.BOOL,
            MarketRecipeDisplay::enabled,
            SlotDisplay.STREAM_CODEC,
            MarketRecipeDisplay::icon,
            MarketRecipeDisplay::new);
    public static final RecipeDisplay.Type<MarketRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public Type<? extends RecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public SlotDisplay craftingStation() {
        return craftingStation;
    }

    public SlotDisplay payment() {
        return this.payment;
    }

    @Override
    public SlotDisplay result() {
        return this.result;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return RecipeDisplay.super.isEnabled(featureFlagSet) && enabled;
    }
}
