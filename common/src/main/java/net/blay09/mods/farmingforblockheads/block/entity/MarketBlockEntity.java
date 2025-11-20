package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.BalmMenuProvider;
import net.blay09.mods.farmingforblockheads.api.FarmingForBlockheadsAPI;
import net.blay09.mods.farmingforblockheads.api.MarketCategory;
import net.blay09.mods.farmingforblockheads.menu.MarketMenu;
import net.blay09.mods.farmingforblockheads.mixin.RecipeManagerAccessor;
import net.blay09.mods.farmingforblockheads.network.MarketCategoriesMessage;
import net.blay09.mods.farmingforblockheads.network.MarketRecipesMessage;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeDisplay;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.registry.SimpleHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MarketBlockEntity extends BlockEntity implements BalmMenuProvider<BlockPos> {
    public MarketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.market.value(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.farmingforblockheads.market");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        final var displays = getRecipeDisplays();
        final var categories = getCategories(displays);
        final var menu = new MarketMenu(windowId, playerInventory, ContainerLevelAccess.create(level, worldPosition));
        menu.setCategories(categories);
        menu.setRecipes(displays);
        return menu;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
        return worldPosition;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BlockPos> getScreenStreamCodec() {
        return BlockPos.STREAM_CODEC.cast();
    }

    private List<SimpleHolder<MarketCategory>> getCategories(List<RecipeDisplayEntry> displays) {
        final var categories = FarmingForBlockheadsAPI.getMarketCategories();
        final var usedCategories = displays.stream().map(it -> ((MarketRecipeDisplay) it.display()).category()).collect(Collectors.toSet());
        return usedCategories.stream().map(it -> SimpleHolder.of(it, categories.get(it))).filter(Objects::nonNull).toList();
    }

    private List<RecipeDisplayEntry> getRecipeDisplays() {
        final var recipeManager = level.getServer().getRecipeManager();
        final var result = new ArrayList<RecipeDisplayEntry>();
        if (recipeManager instanceof RecipeManagerAccessor accessor) {
            final var recipes = accessor.getRecipes().byType(ModRecipes.marketRecipe.type());
            for (final var recipeHolder : recipes) {
                recipeManager.listDisplaysForRecipe(recipeHolder.id(), result::add);
            }
        }
        return result;
    }

    public void openMenu(Player player) {
        Balm.networking().openMenu(player, this);

        final var displays = getRecipeDisplays();
        final var categories = getCategories(displays);
        Balm.networking().sendTo(player, new MarketCategoriesMessage(categories));
        Balm.networking().sendTo(player, new MarketRecipesMessage(displays));
    }
}
