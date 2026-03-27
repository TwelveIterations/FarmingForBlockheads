package net.blay09.mods.farmingforblockheads.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.registry.MarketDefaultsRegistry;
import net.blay09.mods.farmingforblockheads.registry.PaymentImpl;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarketRecipe implements Recipe<RecipeInput> {

    private final String defaults;
    private final @Nullable Identifier category;
    private final ItemStackTemplate result;
    private final ItemStackTemplate icon;
    private final @Nullable Payment payment;
    private final int sortIndex;

    public MarketRecipe(ItemStackTemplate result, String defaults, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Identifier> category, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Payment> payment, int sortIndex, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ItemStackTemplate> icon) {
        this.defaults = defaults;
        this.category = category.orElse(null);
        this.result = result;
        this.icon = icon.orElse(result);
        this.payment = payment.orElse(null);
        this.sortIndex = sortIndex;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        final var effectivePayment = MarketDefaultsRegistry.resolvePayment(this);
        final var ingredient = effectivePayment.ingredient();
        final var itemStack = recipeInput.getItem(0);
        return ingredient.test(itemStack) && itemStack.getCount() >= effectivePayment.count();
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput) {
        return result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    public ItemStackTemplate result() {
        return result;
    }

    public String getDefaults() {
        return defaults;
    }

    public boolean enabled() {
        return MarketDefaultsRegistry.isEnabled(this);
    }

    private SlotDisplay paymentSlotDisplay(Payment payment) {
        final var ingredient = payment.ingredient();
        final List<SlotDisplay> slotDisplays = new ArrayList<>();
        ingredient.items()
                .map(it -> new ItemStackTemplate(it.value(), payment.count()))
                .map(SlotDisplay.ItemStackSlotDisplay::new)
                .forEach(slotDisplays::add);
        return new SlotDisplay.Composite(slotDisplays);
    }

    @Override
    public List<RecipeDisplay> display() {
        final var effectivePayment = MarketDefaultsRegistry.resolvePayment(this);
        final var effectiveCategory = MarketDefaultsRegistry.resolveCategory(this);
        return List.of(new MarketRecipeDisplay(
                paymentSlotDisplay(effectivePayment),
                new SlotDisplay.ItemStackSlotDisplay(result()),
                new SlotDisplay.ItemSlotDisplay(
                        ModBlocks.market.asItem()),
                effectiveCategory,
                sortIndex,
                enabled(),
                new SlotDisplay.ItemStackSlotDisplay(icon)));
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return ModRecipes.marketRecipe.serializer();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ModRecipes.marketRecipe.type();
    }

    @Override
    public PlacementInfo placementInfo() {
        final var effectivePayment = MarketDefaultsRegistry.resolvePayment(this);
        final var ingredients = new ArrayList<Ingredient>();
        for (int i = 0; i < effectivePayment.count(); i++) {
            ingredients.add(effectivePayment.ingredient());
        }
        return PlacementInfo.create(ingredients);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.marketRecipe.bookCategory();
    }

    public Optional<Payment> getPayment() {
        return Optional.ofNullable(payment);
    }

    public Optional<Identifier> getCategory() {
        return Optional.ofNullable(category);
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public static RecipeSerializer<MarketRecipe> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    private static final MapCodec<ItemStackTemplate> RESULT_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemStackTemplate::typeHolder),
            ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStackTemplate::count),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStackTemplate::components)
    ).apply(instance, ItemStackTemplate::new));

    private static final MapCodec<MarketRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RESULT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            ExtraCodecs.NON_EMPTY_STRING.fieldOf("defaults").forGetter(recipe -> recipe.defaults),
            Identifier.CODEC.optionalFieldOf("category").forGetter(MarketRecipe::getCategory),
            PaymentImpl.CODEC.optionalFieldOf("payment").forGetter(MarketRecipe::getPayment),
            Codec.INT.fieldOf("sortIndex").orElse(0).forGetter(MarketRecipe::getSortIndex),
            ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(recipe -> Optional.of(recipe.icon))
    ).apply(instance, MarketRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketRecipe> STREAM_CODEC = StreamCodec.of(MarketRecipe::toNetwork, MarketRecipe::fromNetwork);

    public static MarketRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        final var resultItem = ItemStackTemplate.STREAM_CODEC.decode(buf);
        final var defaults = buf.readUtf();
        final var category = buf.readNullable(FriendlyByteBuf::readIdentifier);
        final var payment = buf.readNullable(buffer -> PaymentImpl.fromNetwork((RegistryFriendlyByteBuf) buffer));
        final var sortIndex = buf.readVarInt();
        final var icon = ItemStackTemplate.STREAM_CODEC.decode(buf);
        return new MarketRecipe(resultItem, defaults, Optional.ofNullable(category), Optional.ofNullable(payment), sortIndex, Optional.of(icon));
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, MarketRecipe recipe) {
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
        buf.writeUtf(recipe.defaults);
        buf.writeNullable(recipe.category, FriendlyByteBuf::writeIdentifier);
        buf.writeNullable(recipe.payment, (buffer, payment) -> PaymentImpl.toNetwork((RegistryFriendlyByteBuf) buffer, payment));
        buf.writeVarInt(recipe.sortIndex);
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.icon);
    }

}
