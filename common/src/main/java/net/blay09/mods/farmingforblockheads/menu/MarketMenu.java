package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.DefaultContainer;
import net.blay09.mods.farmingforblockheads.api.MarketCategory;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.network.MarketPlaceRecipeMessage;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipe;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeDisplay;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.registry.MarketDefaultsRegistry;
import net.blay09.mods.farmingforblockheads.registry.PaymentImpl;
import net.blay09.mods.farmingforblockheads.registry.SimpleHolder;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MarketMenu extends AbstractContainerMenu {

    private static final int PAYMENT_SLOT = 0;
    private static final int RESULT_SLOT = 1;
    private static final int LISTING_SLOT_START = 2;
    private static final int LISTING_SLOT_END = 14;
    private static final int INV_SLOT_START = 14;
    private static final int INV_SLOT_END = 41;
    private static final int USE_ROW_SLOT_START = 41;
    private static final int USE_ROW_SLOT_END = 50;

    private final ContainerLevelAccess access;
    private final Player player;
    private boolean placingRecipe;

    private final MarketPaymentContainer paymentSlots = new MarketPaymentContainer(this, 1);
    private final MarketResultContainer resultSlots = new MarketResultContainer();
    private final List<MarketListingSlot> listingSlots = new ArrayList<>();

    private List<SimpleHolder<MarketCategory>> categories = List.of();
    private List<RecipeDisplayEntry> recipes = List.of();

    private String currentSearch;
    private SimpleHolder<MarketCategory> currentCategory;

    private boolean scrollOffsetDirty;
    private int scrollOffset;

    private final List<RecipeDisplayEntry> filteredRecipes = new ArrayList<>();

    private RecipeDisplayEntry selectedRecipeDisplayEntry;
    private RecipeHolder<MarketRecipe> serverSelectedRecipe;

    public MarketMenu(int windowId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.market.value(), windowId);
        this.access = access;
        this.player = playerInventory.player;

        addSlot(new MarketPaymentSlot(paymentSlots, 0, 23, 39));
        addSlot(new MarketResultSlot(player, paymentSlots, resultSlots, 0, 61, 39));

        final var fakeInventory = new DefaultContainer(4 * 3);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                MarketListingSlot slot = new MarketListingSlot(fakeInventory, j + i * 3, 102 + j * 18, 11 + i * 18, player.level());
                listingSlots.add(slot);
                addSlot(slot);
            }
        }

        addStandardInventorySlots(playerInventory, 8, 92);
    }

    protected static void slotChangedMarket(AbstractContainerMenu menu, ServerLevel level, Player player, MarketPaymentContainer container, ResultContainer resultContainer, @Nullable RecipeHolder<MarketRecipe> recipeHolder) {
        final var recipeInput = container.asRecipeInput();
        final var serverPlayer = (ServerPlayer) player;
        var resultItem = ItemStack.EMPTY;
        final var foundRecipe = level.getServer().getRecipeManager().getRecipeFor(ModRecipes.marketRecipe.type(), recipeInput, level, recipeHolder);
        if (foundRecipe.isPresent()) {
            final var foundRecipeHolder = foundRecipe.get();
            final var recipe = foundRecipeHolder.value();
            if (resultContainer.setRecipeUsed(serverPlayer, foundRecipeHolder)) {
                final var assembledStack = recipe.assemble(recipeInput, level.registryAccess());
                if (assembledStack.isItemEnabled(level.enabledFeatures())) {
                    resultItem = assembledStack;
                }
            }
        }

        resultContainer.setItem(0, resultItem);
        menu.setRemoteSlot(RESULT_SLOT, resultItem);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), RESULT_SLOT, resultItem));
    }

    @Override
    public void slotsChanged(Container pContainer) {
        if (!placingRecipe) {
            access.execute((level, pos) -> {
                if (level instanceof ServerLevel serverLevel) {
                    slotChangedMarket(this, serverLevel, player, paymentSlots, resultSlots, serverSelectedRecipe);
                }
            });
        }
    }

    public RecipeBookMenu.PostPlaceAction handlePlacement(boolean useMaxItems, boolean creative, RecipeHolder<?> genericRecipeHolder, ServerLevel level, Inventory inventory) {
        final var recipeHolder = ((RecipeHolder<MarketRecipe>) genericRecipeHolder);
        beginPlacingRecipe();
        if (serverSelectedRecipe != recipeHolder) {
            clearContainer(player, paymentSlots);
        }

        RecipeBookMenu.PostPlaceAction postPlaceAction;
        try {
            postPlaceAction = ServerPlaceMarketRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<>() {
                @Override
                public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
                    MarketMenu.this.fillPaymentSlotsStackedContents(stackedItemContents);
                }

                @Override
                public void clearCraftingContent() {
                    MarketMenu.this.resultSlots.clearContent();
                    MarketMenu.this.paymentSlots.clearContent();
                }

                @Override
                public boolean recipeMatches(RecipeHolder<MarketRecipe> recipe) {
                    return recipe.value().matches(MarketMenu.this.paymentSlots.asRecipeInput(), MarketMenu.this.owner().level());
                }
            }, getPaymentSlot(), inventory, recipeHolder, useMaxItems, creative);
        } finally {
            finishPlacingRecipe(level, recipeHolder);
        }

        return postPlaceAction;
    }

    private void fillPaymentSlotsStackedContents(StackedItemContents stackedItemContents) {
        paymentSlots.fillStackedContents(stackedItemContents);
    }

    public void beginPlacingRecipe() {
        this.placingRecipe = true;
    }

    public void finishPlacingRecipe(ServerLevel level, RecipeHolder<MarketRecipe> recipeHolder) {
        this.placingRecipe = false;
        serverSelectedRecipe = recipeHolder;
        slotChangedMarket(this, level, player, paymentSlots, resultSlots, recipeHolder);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        clearContainer(player, paymentSlots);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.market.asBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var itemStack = ItemStack.EMPTY;
        final var slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            final var slotStack = slot.getItem();
            itemStack = slotStack.copy();
            if (index == RESULT_SLOT) {
                if (!moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(slotStack, itemStack);
            } else if (index == PAYMENT_SLOT) {
                if (!moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (isPayment(slotStack)) {
                if (!moveItemStackTo(slotStack, PAYMENT_SLOT, PAYMENT_SLOT + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END) {
                if (!moveItemStackTo(slotStack, INV_SLOT_START, INV_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
                if (!moveItemStackTo(slotStack, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
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

    private boolean isPayment(ItemStack itemStack) {
        if (selectedRecipeDisplayEntry != null && selectedRecipeDisplayEntry.display() instanceof MarketRecipeDisplay marketRecipeDisplay) {
            final var contextMap = SlotDisplayContext.fromLevel(player.level());
            final var paymentStacks = marketRecipeDisplay.payment().resolveForStacks(contextMap);
            for (final var paymentStack : paymentStacks) {
                if (ItemStack.isSameItemSameComponents(paymentStack, itemStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    public Slot getPaymentSlot() {
        // Awful suggestion by IntelliJ. What if PAYMENT_SLOT changes in the future?
        // noinspection SequencedCollectionMethodCanBeUsed
        return this.slots.get(PAYMENT_SLOT);
    }

    public Slot getResultSlot() {
        return this.slots.get(RESULT_SLOT);
    }

    protected Player owner() {
        return this.player;
    }

    @Override
    public void clicked(int slotNumber, int dragType, ClickType clickType, Player player) {
        if (slotNumber >= 0 && slotNumber < slots.size()) {
            final var slot = slots.get(slotNumber);
            if (player.level().isClientSide() && clickType != ClickType.PICKUP_ALL) {
                if (slot instanceof MarketListingSlot listingSlot) {
                    final var recipe = listingSlot.getRecipeDisplayEntry();
                    if (recipe != null) {
                        selectedRecipeDisplayEntry = recipe;
                        Balm.networking().sendToServer(new MarketPlaceRecipeMessage(containerId, recipe.id(), clickType == ClickType.QUICK_MOVE));
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
        filteredRecipes.sort(sorting());
    }

    private boolean searchMatches(RecipeDisplayEntry recipeDisplayEntry) {
        if (currentSearch == null || currentSearch.trim().isEmpty()) {
            return true;
        }

        final var contextMap = SlotDisplayContext.fromLevel(player.level());
        final var resultItem = recipeDisplayEntry.display().result().resolveForFirstStack(contextMap);
        final var lowerCaseSearch = currentSearch.toLowerCase();
        if (resultItem.getDisplayName().getString().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearch)) {
            return true;
        } else {
            final var tooltips = resultItem.getTooltipLines(Item.TooltipContext.of(player.level()), player, TooltipFlag.Default.NORMAL);
            for (final var tooltip : tooltips) {
                if (tooltip.getString().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearch)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean categoryMatches(RecipeDisplayEntry recipeDisplayEntry) {
        if (currentCategory == null) {
            return true;
        }

        final var display = recipeDisplayEntry.display();
        if (display instanceof MarketRecipeDisplay marketRecipeDisplay) {
            final var marketCategory = marketRecipeDisplay.category();
            return marketCategory.equals(currentCategory.id());
        }
        return true;
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
        for (final var slot : listingSlots) {
            if (i < filteredRecipes.size()) {
                slot.setRecipeDisplayEntry(filteredRecipes.get(i));
                i++;
            } else {
                slot.setRecipeDisplayEntry(null);
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

    public void setRecipes(List<RecipeDisplayEntry> recipes) {
        this.recipes = recipes;
        updateFilteredRecipes();
        setScrollOffset(0);
    }

    public void setCategories(List<SimpleHolder<MarketCategory>> categories) {
        this.categories = categories;
        setScrollOffsetDirty(true);
    }

    private Optional<MarketCategory> resolveMarketCategory(Identifier identifier) {
        return categories.stream().filter(it -> it.id().equals(identifier)).findFirst().map(SimpleHolder::value);
    }

    private Comparator<RecipeDisplayEntry> sorting() {
        final var contextMap = SlotDisplayContext.fromLevel(player.level());
        return Comparator.comparingInt(
                        (RecipeDisplayEntry recipe) -> recipe.display() instanceof MarketRecipeDisplay marketRecipeDisplay ? resolveMarketCategory(marketRecipeDisplay.category())
                                .map(MarketCategory::sortIndex)
                                .orElse(0) : 0)
                .thenComparing(recipe -> recipe.display() instanceof MarketRecipeDisplay marketRecipeDisplay ? marketRecipeDisplay.sortIndex() : 0)
                .thenComparing(recipe -> (recipe.display() instanceof MarketRecipeDisplay marketRecipeDisplay ? marketRecipeDisplay.icon()
                        .resolveForFirstStack(contextMap) : recipe.display().result().resolveForFirstStack(contextMap))
                        .getDisplayName()
                        .getString());
    }

    public boolean containsRecipeDisplayId(RecipeDisplayId recipeDisplayId) {
        return recipes.stream().anyMatch(it -> it.id().equals(recipeDisplayId));
    }

    public Optional<Payment> getExpectedPayment() {
        if (serverSelectedRecipe != null) {
            return Optional.of(MarketDefaultsRegistry.resolvePayment(serverSelectedRecipe.value()));
        } else if (selectedRecipeDisplayEntry != null && selectedRecipeDisplayEntry.display() instanceof MarketRecipeDisplay marketRecipeDisplay) {
            final var contextMap = SlotDisplayContext.fromLevel(player.level());
            final var paymentItems = marketRecipeDisplay.payment().resolveForStacks(contextMap);
            final var ingredient = Ingredient.of(paymentItems.stream().map(ItemStack::getItem));
            return Optional.of(new PaymentImpl(ingredient, 0, Optional.empty()));
        }
        return Optional.empty();
    }
}
