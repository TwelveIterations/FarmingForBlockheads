package net.blay09.mods.farmingforblockheads.client.gui.screen;

import net.blay09.mods.farmingforblockheads.menu.ShippingBinMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ShippingBinScreen extends AbstractContainerScreen<ShippingBinMenu> {

    private static final Identifier TEXTURE = id("textures/gui/shipping_bin.png");

    private static final Identifier FILL_SPRITE = id("shipping_bin_fill");
    private static final int FILL_WIDTH = 52;
    private static final int FILL_HEIGHT = 7;

    public ShippingBinScreen(ShippingBinMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        final int fillCapacity = menu.getFillCapacity();
        if (fillCapacity > 0) {
            final int fillWidth = Math.max(0, Math.min(FILL_WIDTH, menu.getFill() * FILL_WIDTH / fillCapacity));
            if (fillWidth > 0) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FILL_SPRITE, FILL_WIDTH, FILL_HEIGHT, 0, 0, leftPos + 20, topPos + 59, fillWidth, FILL_HEIGHT);
            }
        }
    }
}
