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
import net.minecraft.world.item.ItemStackTemplate;
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
    private final int fill;
    private final ItemStackTemplate output;
    private final @Nullable ShogiEffect<?> predicate;
    private final @Nullable ShogiEffect<?> count;

    public ShippingBinRecipe(Ingredient input, int fill, ItemStackTemplate output) {
        this(input, fill, output, Optional.empty(), Optional.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public ShippingBinRecipe(Ingredient input, int fill, ItemStackTemplate output, Optional<ShogiEffect<?>> predicate, Optional<ShogiEffect<?>> count) {
        this(input, fill, output, predicate.orElse(null), count.orElse(null));
    }

    public ShippingBinRecipe(Ingredient input, int fill, ItemStackTemplate output, @Nullable ShogiEffect<?> predicate, @Nullable ShogiEffect<?> count) {
        this.input = input;
        this.fill = fill;
        this.output = output;
        this.predicate = predicate;
        this.count = count;
    }

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return input.test(recipeInput.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput) {
        return output.create();
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
                new SlotDisplay.ItemStackSlotDisplay(output),
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

    public int fill() {
        return fill;
    }

    public ItemStackTemplate output() {
        return output;
    }

    public ItemStack result() {
        return result(null, ItemStack.EMPTY);
    }

    public ItemStack result(@Nullable BlockEntity blockEntity, ItemStack soldStack) {
        return result(blockEntity, soldStack, null);
    }

    public ItemStack result(@Nullable BlockEntity blockEntity, ItemStack soldStack, @Nullable Identifier recipeId) {
        final var result = output.create();
        result.setCount(resolveCount(blockEntity, soldStack, recipeId));
        return result;
    }

    public Optional<ShogiEffect<?>> getPredicate() {
        return Optional.ofNullable(predicate);
    }

    public Optional<ShogiEffect<?>> getCount() {
        return Optional.ofNullable(count);
    }

    public boolean canSell(@Nullable BlockEntity blockEntity, ItemStack itemStack, @Nullable Identifier recipeId) {
        return predicate == null || predicate.test(createContext(blockEntity, itemStack, recipeId));
    }

    public int resolveCount(@Nullable BlockEntity blockEntity, ItemStack itemStack, @Nullable Identifier recipeId) {
        if (count == null) {
            return output.count();
        }

        try {
            return Math.max(0, count.apply(createContext(blockEntity, itemStack, recipeId)).mapLeft(Coercion.INT).left().orElse(output.count()));
        } catch (Throwable throwable) {
            return output.count();
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
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("fill", 1).forGetter(ShippingBinRecipe::fill),
            ItemStackTemplate.CODEC.fieldOf("output").forGetter(ShippingBinRecipe::output),
            MarketRules.SCOPE.getEffectCodec().optionalFieldOf("predicate").forGetter(ShippingBinRecipe::getPredicate),
            MarketRules.SCOPE.getEffectCodec().optionalFieldOf("count").forGetter(ShippingBinRecipe::getCount)
    ).apply(instance, ShippingBinRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShippingBinRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            ShippingBinRecipe::input,
            ByteBufCodecs.VAR_INT,
            ShippingBinRecipe::fill,
            ItemStackTemplate.STREAM_CODEC,
            ShippingBinRecipe::output,
            ShippingBinRecipe::new);
}
