package net.blay09.mods.farmingforblockheads.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ShippingBinRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient input;
    private final int value;
    private final @Nullable ShogiEffect<?> valueEffect;
    private final @Nullable ShogiEffect<?> predicate;

    public ShippingBinRecipe(Ingredient input, int value) {
        this(input, value, Optional.empty(), Optional.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public ShippingBinRecipe(Ingredient input, int value, Optional<ShogiEffect<?>> valueEffect, Optional<ShogiEffect<?>> predicate) {
        this(input, value, valueEffect.orElse(null), predicate.orElse(null));
    }

    public ShippingBinRecipe(Ingredient input, int value, @Nullable ShogiEffect<?> valueEffect, @Nullable ShogiEffect<?> predicate) {
        this.input = input;
        this.value = value;
        this.valueEffect = valueEffect;
        this.predicate = predicate;
    }

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return input.test(recipeInput.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                List.of(input.display()),
                SlotDisplay.Empty.INSTANCE,
                new SlotDisplay.ItemSlotDisplay(ModBlocks.shippingBin.asItem())));
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipes.shippingBinRecipe.serializer();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.shippingBinRecipe.type();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(input);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.shippingBinRecipe.bookCategory();
    }

    public Ingredient input() {
        return input;
    }

    public int value() {
        return value;
    }

    public Optional<ShogiEffect<?>> getValueEffect() {
        return Optional.ofNullable(valueEffect);
    }

    public Optional<ShogiEffect<?>> getPredicate() {
        return Optional.ofNullable(predicate);
    }

    public boolean canSell(@Nullable BlockEntity blockEntity, ItemStack itemStack, @Nullable Identifier recipeId) {
        return predicate == null || predicate.test(createContext(blockEntity, itemStack, recipeId));
    }

    public int resolveValue(@Nullable BlockEntity blockEntity, ItemStack itemStack, @Nullable Identifier recipeId) {
        if (valueEffect == null) {
            return value;
        }

        try {
            return Math.max(0, valueEffect.apply(createContext(blockEntity, itemStack, recipeId)).mapLeft(Coercion.INT).left().orElse(value));
        } catch (Throwable throwable) {
            return value;
        }
    }

    private MutableShogiContext createContext(@Nullable BlockEntity blockEntity, ItemStack itemStack, @Nullable Identifier recipeId) {
        final var context = blockEntity != null ? MutableShogiContext.of(blockEntity) : MutableShogiContext.of(itemStack);
        context.withItemStack(itemStack);
        if (recipeId != null) {
            context.withVariable("recipe", recipeId);
        }
        return context;
    }

    public static RecipeSerializer<ShippingBinRecipe> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    private static final MapCodec<ShippingBinRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(ShippingBinRecipe::input),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("value").forGetter(ShippingBinRecipe::value),
            FarmingForBlockheadsRules.SCOPE.getEffectCodec().optionalFieldOf("valueEffect").forGetter(ShippingBinRecipe::getValueEffect),
            FarmingForBlockheadsRules.SCOPE.getEffectCodec().optionalFieldOf("predicate").forGetter(ShippingBinRecipe::getPredicate)
    ).apply(instance, ShippingBinRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShippingBinRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            ShippingBinRecipe::input,
            ByteBufCodecs.VAR_INT,
            ShippingBinRecipe::value,
            ShippingBinRecipe::new);
}
