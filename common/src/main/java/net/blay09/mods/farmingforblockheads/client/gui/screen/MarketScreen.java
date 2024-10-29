package net.blay09.mods.farmingforblockheads.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.balm.mixin.ScreenAccessor;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.blay09.mods.farmingforblockheads.client.gui.widget.MarketFilterButton;
import net.blay09.mods.farmingforblockheads.menu.MarketMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.List;

public class MarketScreen extends AbstractContainerScreen<MarketMenu> {

    private static final int SCROLLBAR_COLOR = 0xFFAAAAAA;
    private static final int SCROLLBAR_Y = 8;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_HEIGHT = 77;
    private static final int VISIBLE_ROWS = 4;

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "textures/gui/market.png");

    private final List<MarketFilterButton> filterButtons = Lists.newArrayList();

    private int scrollBarScaledHeight;
    private int scrollBarXPos;
    private int scrollBarYPos;
    private int currentOffset;

    private int mouseClickY = -1;
    private int indexWhenClicked;
    private int lastNumberOfMoves;

    private EditBox searchBar;

    public MarketScreen(MarketMenu container, Inventory playerInventory, Component displayName) {
        super(container, playerInventory, displayName);
    }

    @Override
    public void init() {
        imageHeight = 174;
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
            ((ScreenAccessor) this).balm_getChildren().remove(filterButton);
            ((ScreenAccessor) this).balm_getRenderables().remove(filterButton);
            ((ScreenAccessor) this).balm_getNarratables().remove(filterButton);
        }
        filterButtons.clear();

        int curX = 87;
        int curY = -80;
        final var categories = menu.getCategories();
        for (final var category : categories) {
            MarketFilterButton filterButton = new MarketFilterButton(width / 2 + curX, height / 2 + curY, menu, category, button -> {
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != -1 && mouseClickY != -1) {
            mouseClickY = -1;
            indexWhenClicked = 0;
            lastNumberOfMoves = 0;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && mouseX >= searchBar.getX() && mouseX < searchBar.getX() + searchBar.getWidth() && mouseY >= searchBar.getY() && mouseY < searchBar.getY() + searchBar.getHeight()) {
            searchBar.setValue("");
            menu.setSearch(null);
            menu.updateListingSlots();
            setCurrentOffset(currentOffset);
            return true;
        } else if (mouseX >= scrollBarXPos && mouseX <= scrollBarXPos + SCROLLBAR_WIDTH && mouseY >= scrollBarYPos && mouseY <= scrollBarYPos + scrollBarScaledHeight) {
            mouseClickY = (int) mouseY;
            indexWhenClicked = currentOffset;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        boolean result = super.charTyped(c, keyCode);

        menu.setSearch(searchBar.getValue());
        menu.updateListingSlots();
        setCurrentOffset(currentOffset);

        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBar.keyPressed(keyCode, scanCode, modifiers) || searchBar.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                minecraft.player.closeContainer();
            } else {
                menu.setSearch(searchBar.getValue());
                menu.updateListingSlots();
                setCurrentOffset(currentOffset);
            }

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (menu.isScrollOffsetDirty()) {
            updateCategoryFilters();
            recalculateScrollBar();
            menu.setScrollOffsetDirty(false);
        }

        Font font = minecraft.font;

        guiGraphics.setColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(TEXTURE, leftPos, topPos - 10, 0, 0, imageWidth, imageHeight + 10);
        if (menu.getSelectedRecipe() != null && !menu.isReadyToBuy()) {
            guiGraphics.blit(TEXTURE, leftPos + 43, topPos + 40, 176, 0, 14, 14);
        }

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

        guiGraphics.drawString(font, I18n.get("container.farmingforblockheads.market"), leftPos + 10, topPos + 10, 0xFFFFFF, true);

        final var selectedRecipe = menu.getSelectedRecipe();
        if (selectedRecipe == null) {
            guiGraphics.drawCenteredString(font, I18n.get("gui.farmingforblockheads.market.no_selection"), leftPos + 49, topPos + 65, 0xFFFFFF);
        } else {
            final var payment = selectedRecipe.value().getPaymentOrDefault();
            final var paymentComponent = payment.tooltip().orElseGet(() -> FarmingForBlockheads.getDefaultPaymentComponent(payment));
            final var component = Component.translatable("gui.farmingforblockheads.market.cost", paymentComponent)
                    .withStyle(ChatFormatting.GREEN);
            final var width = font.width(component);
            guiGraphics.fillGradient((int) (leftPos + 49 - width / 2f - 2), topPos + 65 - 2,
                    (int) (leftPos + 49 + width / 2f + 2), topPos + 65 + 9, 0x88000000, 0x99000000);
            guiGraphics.drawCenteredString(font, component, leftPos + 49, topPos + 65, 0xFFFFFF);
        }

        guiGraphics.fill(scrollBarXPos, scrollBarYPos, scrollBarXPos + SCROLLBAR_WIDTH, scrollBarYPos + scrollBarScaledHeight, SCROLLBAR_COLOR);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
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
        this.currentOffset = Math.max(0, Math.min(currentOffset, (int) Math.ceil(menu.getFilteredListCount() / 3f) - VISIBLE_ROWS));

        menu.setScrollOffset(this.currentOffset);

        recalculateScrollBar();
    }

}
