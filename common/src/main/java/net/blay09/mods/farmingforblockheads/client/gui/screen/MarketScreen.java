package net.blay09.mods.farmingforblockheads.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.blay09.mods.balm.mixin.ScreenAccessor;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.client.gui.widget.MarketFilterButton;
import net.blay09.mods.farmingforblockheads.menu.MarketListingSlot;
import net.blay09.mods.farmingforblockheads.menu.MarketMenu;
import net.blay09.mods.farmingforblockheads.mixin.GhostSlotsAccessor;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeDisplay;
import net.blay09.mods.kuma.api.Kuma;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MarketScreen extends AbstractContainerScreen<MarketMenu> implements RecipeUpdateListener {

    private static final int SCROLLBAR_COLOR = 0xFFAAAAAA;
    private static final int SCROLLBAR_Y = 8;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_HEIGHT = 77;
    private static final int VISIBLE_ROWS = 4;

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "textures/gui/market.png");

    private final List<MarketFilterButton> filterButtons = new ArrayList<>();

    private float time;
    private final GhostSlots ghostSlots;

    private int scrollBarScaledHeight;
    private int scrollBarXPos;
    private int scrollBarYPos;
    private int currentOffset;

    private int mouseClickY = -1;
    private int indexWhenClicked;
    private int lastNumberOfMoves;

    private @Nullable EditBox searchBar;

    public MarketScreen(MarketMenu container, Inventory playerInventory, Component displayName) {
        super(container, playerInventory, displayName, 176, 174);

        ghostSlots = new GhostSlots(() -> Mth.floor(this.time / 30f));
    }

    @Override
    public void init() {
        super.init();

        Font font = Minecraft.getInstance().font;

        searchBar = new EditBox(font, leftPos + imageWidth - 78, topPos - 5, 70, 10, searchBar, Component.empty());
        setInitialFocus(searchBar);
        addRenderableWidget(searchBar);

        updateCategoryFilters();

        recalculateScrollBar();
    }

    private void updateCategoryFilters() {
        for (MarketFilterButton filterButton : filterButtons) {
            ((ScreenAccessor) this).balm$getChildren().remove(filterButton);
            ((ScreenAccessor) this).balm$getRenderables().remove(filterButton);
            ((ScreenAccessor) this).balm$getNarratables().remove(filterButton);
        }
        filterButtons.clear();

        int curX = 87;
        int curY = -80;
        final var categories = menu.getCategories();
        for (final var category : categories) {
            MarketFilterButton filterButton = new MarketFilterButton(width / 2 + curX, height / 2 + curY, menu, category, _ -> {
                if (menu.getCurrentCategory().map(it -> it.equals(category)).orElse(false)) {
                    menu.setCategory(null);
                } else {
                    menu.setCategory(category);
                }
                menu.updateListingSlots();
                setCurrentOffset(currentOffset);
            });

            addRenderableWidget(filterButton);
            filterButtons.add(filterButton);

            curY += 20;
            if (curY > 60) {
                curY = -80;
                curX += 20;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (Math.abs(deltaY) > 0f) {
            setCurrentOffset(deltaY > 0 ? currentOffset - 1 : currentOffset + 1);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() != -1 && mouseClickY != -1) {
            mouseClickY = -1;
            indexWhenClicked = 0;
            lastNumberOfMoves = 0;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (searchBar != null && event.button() == InputConstants.MOUSE_BUTTON_RIGHT && event.x() >= searchBar.getX() && event.y() < searchBar.getX() + searchBar.getWidth() && event.y() >= searchBar.getY() && event.y() < searchBar.getY() + searchBar.getHeight()) {
            searchBar.setValue("");
            menu.setSearch(null);
            menu.updateListingSlots();
            setCurrentOffset(currentOffset);
            return true;
        } else if (event.x() >= scrollBarXPos && event.x() <= scrollBarXPos + SCROLLBAR_WIDTH && event.y() >= scrollBarYPos && event.y() <= scrollBarYPos + scrollBarScaledHeight) {
            mouseClickY = (int) event.y();
            indexWhenClicked = currentOffset;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean result = super.charTyped(event);

        if (searchBar != null) {
            menu.setSearch(searchBar.getValue());
            menu.updateListingSlots();
            setCurrentOffset(currentOffset);
        }

        return result;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchBar != null && (searchBar.keyPressed(event) || searchBar.isFocused())) {
            if (event.isEscape()) {
                minecraft.player.closeContainer();
            } else {
                menu.setSearch(searchBar.getValue());
                menu.updateListingSlots();
                setCurrentOffset(currentOffset);
            }

            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!Kuma.hasControlDown()) {
            time += partialTicks;
        }
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        if (menu.isScrollOffsetDirty()) {
            updateCategoryFilters();
            recalculateScrollBar();
            menu.setScrollOffsetDirty(false);
        }

        Font font = minecraft.font;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos - 10, 0, 0, imageWidth, imageHeight + 10, 256, 256);

        if (mouseClickY != -1) {
            float pixelsPerFilter = (SCROLLBAR_HEIGHT - scrollBarScaledHeight) / (float) Math.max(1,
                    (int) Math.ceil(menu.getFilteredListCount() / 3f) - VISIBLE_ROWS);
            if (pixelsPerFilter != 0) {
                int numberOfFiltersMoved = (int) ((mouseY - mouseClickY) / pixelsPerFilter);
                if (numberOfFiltersMoved != lastNumberOfMoves) {
                    setCurrentOffset(indexWhenClicked + numberOfFiltersMoved);
                    lastNumberOfMoves = numberOfFiltersMoved;
                }
            }
        }

        graphics.text(font, I18n.get("container.farmingforblockheads.market"), leftPos + 10, topPos + 10, 0xFFFFFF, true);

        graphics.fill(scrollBarXPos, scrollBarYPos, scrollBarXPos + SCROLLBAR_WIDTH, scrollBarYPos + scrollBarScaledHeight, SCROLLBAR_COLOR);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
    }

    @Override
    protected void extractSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractSlots(graphics, mouseX, mouseY);
        ghostSlots.extractRenderState(graphics, minecraft, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        ghostSlots.extractTooltip(graphics, minecraft, mouseX, mouseY, hoveredSlot);
    }

    @Override
    protected void slotClicked(Slot slot, int pSlotId, int pMouseButton, ContainerInput pType) {
        super.slotClicked(slot, pSlotId, pMouseButton, pType);
        if (slot == menu.getResultSlot() || slot == menu.getPaymentSlot() || slot instanceof MarketListingSlot) {
            ghostSlots.clear();
        }
    }

    public Collection<MarketFilterButton> getFilterButtons() {
        return filterButtons;
    }

    private void recalculateScrollBar() {
        int scrollBarTotalHeight = SCROLLBAR_HEIGHT - 1;
        this.scrollBarScaledHeight = (int) (scrollBarTotalHeight * Math.min(1f,
                ((float) VISIBLE_ROWS / (Math.ceil(menu.getFilteredListCount() / 3f)))));
        this.scrollBarXPos = leftPos + imageWidth - SCROLLBAR_WIDTH - 9;
        this.scrollBarYPos = topPos + SCROLLBAR_Y + ((scrollBarTotalHeight - scrollBarScaledHeight) * currentOffset / Math.max(1,
                (int) Math.ceil((menu.getFilteredListCount() / 3f)) - VISIBLE_ROWS));
    }

    private void setCurrentOffset(int currentOffset) {
        this.currentOffset = Math.clamp(currentOffset, 0, Math.max(0, Mth.ceil(menu.getFilteredListCount() / 3f) - VISIBLE_ROWS));

        menu.setScrollOffset(this.currentOffset);

        recalculateScrollBar();
    }

    @Override
    public void recipesUpdated() {
    }

    @Override
    public void fillGhostRecipe(RecipeDisplay recipeDisplay) {
        ghostSlots.clear();
        final var level = Minecraft.getInstance().level;
        final var contextMap = SlotDisplayContext.fromLevel(level);
        if (ghostSlots instanceof GhostSlotsAccessor accessor) {
            accessor.callSetResult(menu.getResultSlot(), contextMap, recipeDisplay.result());

            if (recipeDisplay instanceof MarketRecipeDisplay marketRecipeDisplay) {
                // We pretend the input is a result slot so it renders the count too
                accessor.callSetSlot(menu.getPaymentSlot(), contextMap, marketRecipeDisplay.payment(), true);
            }
        }
    }
}
