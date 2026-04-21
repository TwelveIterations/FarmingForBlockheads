package net.blay09.mods.farmingforblockheads.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.farmingforblockheads.api.MarketPreset;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.blay09.mods.farmingforblockheads.registry.MarketPresetRegistry;
import net.blay09.mods.farmingforblockheads.registry.PaymentImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class MarketRecipe implements Recipe<RecipeInput> {

    private final ResourceLocation preset;
    private final ResourceLocation category;
    private final ItemStack resultItem;
    private final ItemStack icon;
    private final Payment payment;
    private final int sortIndex;

    public MarketRecipe(ItemStack resultItem, ResourceLocation category, ResourceLocation preset, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Payment> payment, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ItemStack> icon, int sortIndex) {
        this.preset = preset;
        this.category = category;
        this.resultItem = resultItem;
        this.payment = payment.orElse(null);
        this.icon = icon.orElse(resultItem);
        this.sortIndex = sortIndex;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider provider) {
        return resultItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return resultItem;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.marketRecipeSerializer;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.marketRecipeType;
    }

    public ResourceLocation getPreset() {
        return preset;
    }

    public ResourceLocation getCategory() {
        return category;
    }

    private Payment getDefaultPayment(ResourceLocation presetId) {
        return MarketPresetRegistry.INSTANCE.get(presetId)
                .map(MarketPreset::payment)
                .orElseGet(() -> new PaymentImpl(Ingredient.of(Items.EMERALD), 1, Optional.empty()));
    }

    public Payment getPaymentOrDefault() {
        return payment != null ? payment : getDefaultPayment(preset);
    }

    public ItemStack getIcon() {
        return icon;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    static class Serializer implements RecipeSerializer<MarketRecipe> {

        private static final MapCodec<ItemStack> RESULT_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item")
                        .orElse(BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR))
                        .forGetter(ItemStack::getItemHolder),
                ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
        ).apply(instance, ItemStack::new));

        private static final MapCodec<MarketRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RESULT_CODEC.fieldOf("result").forGetter(recipe -> recipe.resultItem),
                ResourceLocation.CODEC.fieldOf("category").forGetter(recipe -> recipe.category),
                ResourceLocation.CODEC.fieldOf("preset").forGetter(recipe -> recipe.preset),
                PaymentImpl.CODEC.optionalFieldOf("payment").forGetter(recipe -> Optional.ofNullable(recipe.payment)),
                ItemStack.CODEC.optionalFieldOf("icon").forGetter(recipe -> Optional.ofNullable(recipe.icon)),
                Codec.INT.fieldOf("sortIndex").orElse(0).forGetter(MarketRecipe::getSortIndex)
        ).apply(instance, MarketRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MarketRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<MarketRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MarketRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static MarketRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            final var resultItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            final var category = buf.readResourceLocation();
            final var preset = buf.readResourceLocation();
            final var payment = PaymentImpl.fromNetwork(buf);
            final var icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            final var sortIndex = buf.readVarInt();
            return new MarketRecipe(resultItem, category, preset, Optional.of(payment), Optional.of(icon), sortIndex);
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, MarketRecipe recipe) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.resultItem);
            buf.writeResourceLocation(recipe.category);
            buf.writeResourceLocation(recipe.preset);
            PaymentImpl.toNetwork(buf, recipe.getPaymentOrDefault());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.icon);
            buf.writeVarInt(recipe.sortIndex);
        }
    }

}
