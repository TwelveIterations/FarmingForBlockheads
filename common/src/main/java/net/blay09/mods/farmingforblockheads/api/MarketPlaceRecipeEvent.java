package net.blay09.mods.farmingforblockheads.api;

import net.blay09.mods.balm.platform.event.BidirectionalEventMapper;
import net.blay09.mods.balm.platform.event.EventMapper;
import net.blay09.mods.farmingforblockheads.menu.MarketPaymentContainer;
import net.blay09.mods.farmingforblockheads.menu.MarketResultContainer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public final class MarketPlaceRecipeEvent {

    public static final BidirectionalEventMapper<Consumer<MarketPlaceRecipeEvent>> EVENT = EventMapper.createBound(MarketPlaceRecipeEvent.class);

    private final boolean useMaxItems;
    private final boolean creative;
    private final ResourceKey<Recipe<?>> recipeId;
    private final MarketRecipe recipe;
    private final ServerLevel level;
    private final Inventory inventory;
    private final MarketPaymentContainer paymentSlots;
    private final MarketResultContainer resultSlots;
    private RecipeBookMenu.@Nullable PostPlaceAction postPlaceActionOverride;

    public MarketPlaceRecipeEvent(boolean useMaxItems, boolean creative, ResourceKey<Recipe<?>> recipeId,
                                  MarketRecipe recipe, ServerLevel level, Inventory inventory,
                                  MarketPaymentContainer paymentSlots, MarketResultContainer resultSlots) {
        this.useMaxItems = useMaxItems;
        this.creative = creative;
        this.recipeId = recipeId;
        this.recipe = recipe;
        this.level = level;
        this.inventory = inventory;
        this.paymentSlots = paymentSlots;
        this.resultSlots = resultSlots;
    }

    public boolean useMaxItems() {
        return useMaxItems;
    }

    public boolean creative() {
        return creative;
    }

    public ResourceKey<Recipe<?>> recipeId() {
        return recipeId;
    }

    public MarketRecipe recipe() {
        return recipe;
    }

    public ServerLevel level() {
        return level;
    }

    public Inventory inventory() {
        return inventory;
    }

    public MarketPaymentContainer paymentSlots() {
        return paymentSlots;
    }

    public MarketResultContainer resultSlots() {
        return resultSlots;
    }

    public RecipeBookMenu.@Nullable PostPlaceAction postPlaceActionOverride() {
        return postPlaceActionOverride;
    }

    public void overridePostPlaceAction(RecipeBookMenu.PostPlaceAction postPlaceActionOverride) {
        this.postPlaceActionOverride = postPlaceActionOverride;
    }

}
