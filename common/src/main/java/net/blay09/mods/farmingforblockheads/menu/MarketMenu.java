package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.container.DefaultContainer;
import net.blay09.mods.farmingforblockheads.api.MarketCategory;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.network.MarketPutInBasketMessage;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipe;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.registry.MarketCategoryRegistry;
import net.blay09.mods.farmingforblockheads.registry.MarketPresetRegistry;
import net.blay09.mods.farmingforblockheads.registry.PaymentImpl;
import net.blay09.mods.farmingforblockheads.registry.SimpleHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MarketMenu extends AbstractContainerMenu {

    public record Data(BlockPos pos, Set<ResourceLocation> presetFilters, Set<ResourceLocation> categoryFilters) {
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeBlockPos(data.pos());
        buf.writeInt(data.presetFilters().size());
        for (ResourceLocation location : data.presetFilters()) {
            buf.writeResourceLocation(location);
        }
        buf.writeInt(data.categoryFilters().size());
        for (ResourceLocation location : data.categoryFilters()) {
            buf.writeResourceLocation(location);
        }
    }, (buf) -> {
        BlockPos pos = buf.readBlockPos();
        int presetFilterSize = buf.readInt();
        Set<ResourceLocation> presetFilters = new HashSet<>();
        for (int i = 0; i < presetFilterSize; i++) {
            presetFilters.add(buf.readResourceLocation());
        }
        int categoryFilterSize = buf.readInt();
        Set<ResourceLocation> categoryFilters = new HashSet<>();
        for (int i = 0; i < categoryFilterSize; i++) {
            categoryFilters.add(buf.readResourceLocation());
        }
        return new Data(pos, presetFilters, categoryFilters);
    });

    private final Player player;
    private final BlockPos pos;

    private final DefaultContainer marketInputBuffer = new DefaultContainer(1);
    private final DefaultContainer marketOutputBuffer = new DefaultContainer(1);
    private final List<MarketListingSlot> marketSlots = new ArrayList<>();
    private final MarketPaymentSlot paymentSlot;

    private final List<SimpleHolder<MarketCategory>> categories;
    private final List<RecipeHolder<MarketRecipe>> recipes;

    private String currentSearch;
    private SimpleHolder<MarketCategory> currentCategory;
    private final Comparator<RecipeHolder<MarketRecipe>> currentSorting = Comparator.comparingInt(
                    (RecipeHolder<MarketRecipe> recipe) -> MarketCategoryRegistry.INSTANCE.get(recipe.value().getCategory())
                            .map(MarketCategory::sortIndex)
                            .orElse(0))
            .thenComparing(recipe -> recipe.value()
                    .getIcon()
                    .getDisplayName()
                    .getString());

    private boolean scrollOffsetDirty;
    private int scrollOffset;

    private final List<RecipeHolder<MarketRecipe>> filteredRecipes = new ArrayList<>();
    private RecipeHolder<MarketRecipe> selectedRecipe;

    public MarketMenu(int windowId, Inventory playerInventory, BlockPos pos, Set<ResourceLocation> alwaysIncludedPresets, Set<ResourceLocation> includeOnlyCategories) {
        super(ModMenus.market.get(), windowId);
        this.player = playerInventory.player;
        this.pos = pos;

        recipes = player.level()
                .getRecipeManager()
                .getAllRecipesFor(ModRecipes.marketRecipeType)
                .stream()
                .filter(recipe -> MarketPresetRegistry.isRecipeEnabled(recipe.value(), alwaysIncludedPresets))
                .toList();

        categories = MarketCategoryRegistry.INSTANCE.getAll().entrySet()
                .stream()
                .filter(entry -> includeOnlyCategories.isEmpty() || includeOnlyCategories.contains(entry.getKey()))
                .filter(entry -> recipes.stream().anyMatch(it -> it.value().getCategory().equals(entry.getKey())))
                .sorted(Comparator.comparingInt(entry -> entry.getValue().sortIndex()))
                .map(it -> new SimpleHolder<>(it.getKey(), it.getValue()))
                .toList();

        addSlot(paymentSlot = new MarketPaymentSlot(marketInputBuffer, 0, 23, 39));
        addSlot(new MarketBasketSlot(this, marketOutputBuffer, 0, 61, 39));

        final var fakeInventory = new DefaultContainer(4 * 3);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                MarketListingSlot slot = new MarketListingSlot(fakeInventory, j + i * 3, 102 + j * 18, 11 + i * 18);
                marketSlots.add(slot);
                addSlot(slot);
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 92 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 150));
        }

        updateFilteredRecipes();
        setScrollOffset(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            if (index == 1) {
                if (!isReadyToBuy()) {
                    return ItemStack.EMPTY;
                }
                if (!moveItemStackTo(slotStack, 14, 50, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemStack);
            } else if (index == 0) {
                if (!moveItemStackTo(slotStack, 14, 50, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (getExpectedPayment(selectedRecipe).ingredient().test(slotStack)) {
                if (!moveItemStackTo(slotStack, 0, 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 41 && index < 50) {
                if (!moveItemStackTo(slotStack, 14, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 14 && index < 41) {
                if (!moveItemStackTo(slotStack, 41, 50, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            ItemStack itemStack = this.marketInputBuffer.removeItemNoUpdate(0);
            if (!itemStack.isEmpty() && !player.addItem(itemStack)) {
                player.drop(itemStack, false);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).getBlock() == ModBlocks.market
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }

    @Override
    public void slotsChanged(Container container) {
        if (selectedRecipe != null) {
            marketOutputBuffer.setItem(0, selectedRecipe.value().assemble(new TransientCraftingContainer(this, 0, 0).asCraftInput(), RegistryAccess.EMPTY));
        } else {
            marketOutputBuffer.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.marketOutputBuffer && super.canTakeItemForPickAll(itemStack, slot);
    }

    private Payment getExpectedPayment(@Nullable RecipeHolder<MarketRecipe> recipe) {
        if (recipe == null) {
            return new PaymentImpl(Ingredient.of(Items.EMERALD), 1, Optional.empty());
        }

        return recipe.value().getPaymentOrDefault();
    }

    public void selectMarketEntry(ResourceLocation recipeId, boolean stack) {
        selectedRecipe = recipes.stream().filter(it -> it.id().equals(recipeId)).findFirst().orElse(null);
        if (selectedRecipe != null) {
            final var payment = selectedRecipe.value().getPaymentOrDefault();
            final var currentInput = marketInputBuffer.getItem(0);
            if (!payment.ingredient().test(currentInput)) {
                quickMoveStack(player, 0);
                quickMoveCost(payment, stack ? 64 : 1);
            } else if (stack && currentInput.getCount() < 64) {
                quickMoveCost(payment, 64);
            } else if (!stack && currentInput.getCount() > 1) {
                quickMoveStack(player, 0);
                quickMoveCost(payment, 1);
            }
        }
        slotsChanged(marketInputBuffer);
    }

    private void quickMoveCost(Payment payment, int desiredCount) {
        paymentSlot.setMaxStackSizeOverride(desiredCount);
        for (int i = 14; i < slots.size(); i++) {
            final var slotStack = slots.get(i).getItem();
            if (payment.ingredient().test(slotStack)) {
                quickMoveStack(player, i);
            }

            if (paymentSlot.getItem().getCount() >= desiredCount) {
                break;
            }
        }
        paymentSlot.resetMaxStackSizeOverride();
    }

    @Nullable
    public RecipeHolder<MarketRecipe> getSelectedRecipe() {
        return selectedRecipe;
    }

    public boolean isReadyToBuy() {
        if (selectedRecipe == null) {
            return false;
        }

        final var expectedPayment = getExpectedPayment(selectedRecipe);
        final var currentInput = marketInputBuffer.getItem(0);
        return !currentInput.isEmpty() && expectedPayment.ingredient().test(currentInput) && currentInput.getCount() >= expectedPayment.count();
    }

    public void onItemBought() {
        if (selectedRecipe != null) {
            marketInputBuffer.removeItem(0, selectedRecipe.value().getPaymentOrDefault().count());
            slotsChanged(marketInputBuffer);
        }
    }

    @Override
    public void clicked(int slotNumber, int dragType, ClickType clickType, Player player) {
        if (slotNumber >= 0 && slotNumber < slots.size()) {
            final var slot = slots.get(slotNumber);
            if (player.level().isClientSide) {
                if (slot instanceof MarketListingSlot marketSlot) {
                    final var recipe = marketSlot.getRecipe();
                    if (recipe != null) {
                        selectedRecipe = recipe;
                        Balm.getNetworking().sendToServer(new MarketPutInBasketMessage(recipe.id(), clickType == ClickType.QUICK_MOVE));
                    }
                }
            }
        }

        super.clicked(slotNumber, dragType, clickType, player);
    }

    public void setSearch(@Nullable String term) {
        this.currentSearch = term;
        updateFilteredRecipes();
        setScrollOffset(0);
    }

    public void setCategory(@Nullable SimpleHolder<MarketCategory> category) {
        this.currentCategory = category;
        updateFilteredRecipes();
        setScrollOffset(0);
    }

    private void updateFilteredRecipes() {
        filteredRecipes.clear();
        for (final var recipe : recipes) {
            if (searchMatches(recipe) && categoryMatches(recipe)) {
                filteredRecipes.add(recipe);
            }
        }
        filteredRecipes.sort(currentSorting);
    }

    private boolean searchMatches(RecipeHolder<MarketRecipe> recipeHolder) {
        if (currentSearch == null || currentSearch.trim().isEmpty()) {
            return true;
        }

        final var recipe = recipeHolder.value();
        final var itemStack = recipe.getIcon();
        final var lowerCaseSearch = currentSearch.toLowerCase();
        if (itemStack.getDisplayName().getString().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearch)) {
            return true;
        } else {
            final var tooltips = itemStack.getTooltipLines(Item.TooltipContext.of(player.level()), player, TooltipFlag.Default.NORMAL);
            for (final var tooltip : tooltips) {
                if (tooltip.getString().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearch)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean categoryMatches(RecipeHolder<MarketRecipe> recipeHolder) {
        if (currentCategory == null) {
            return true;
        }

        final var recipe = recipeHolder.value();
        return recipe.getCategory().equals(currentCategory.id());
    }

    public int getFilteredListCount() {
        return filteredRecipes.size();
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
        updateListingSlots();
    }

    public void updateListingSlots() {
        int i = scrollOffset * 3;
        for (final var slot : marketSlots) {
            if (i < filteredRecipes.size()) {
                slot.setRecipe(filteredRecipes.get(i));
                i++;
            } else {
                slot.setRecipe(null);
            }
        }
    }

    public boolean isScrollOffsetDirty() {
        return scrollOffsetDirty;
    }

    public void setScrollOffsetDirty(boolean scrollOffsetDirty) {
        this.scrollOffsetDirty = scrollOffsetDirty;
    }

    public Optional<SimpleHolder<MarketCategory>> getCurrentCategory() {
        return Optional.ofNullable(currentCategory);
    }

    public List<SimpleHolder<MarketCategory>> getCategories() {
        return categories;
    }

}
