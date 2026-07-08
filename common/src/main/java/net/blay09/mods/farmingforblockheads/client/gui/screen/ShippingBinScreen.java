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

    private static final Identifier FILL_PROGRESS_SPRITE = id("container/shipping_bin/fill_progress");
    private static final Identifier SALE_PROGRESS_SPRITE = id("container/shipping_bin/sale_progress");
    private static final int FILL_PROGRESS_WIDTH = 52;
    private static final int FILL_PROGRESS_HEIGHT = 7;
    private static final int SALE_PROGRESS_WIDTH = 24;
    private static final int SALE_PROGRESS_HEIGHT = 16;

    public ShippingBinScreen(ShippingBinMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        final int shipmentCapacity = menu.getShipmentCapacity();
        if (shipmentCapacity > 0) {
            final int progressWidth = Math.max(0, Math.min(FILL_PROGRESS_WIDTH, menu.getShipmentValue() * FILL_PROGRESS_WIDTH / shipmentCapacity));
            if (progressWidth > 0) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FILL_PROGRESS_SPRITE, FILL_PROGRESS_WIDTH, FILL_PROGRESS_HEIGHT, 0, 0, leftPos + 20, topPos + 59, progressWidth, FILL_PROGRESS_HEIGHT);
            }
        }

        final int saleProgressMax = menu.getSaleProgressMax();
        if (saleProgressMax > 0) {
            final int progressWidth = Math.max(0, Math.min(SALE_PROGRESS_WIDTH, menu.getSaleProgress() * SALE_PROGRESS_WIDTH / saleProgressMax));
            if (progressWidth > 0) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SALE_PROGRESS_SPRITE, SALE_PROGRESS_WIDTH, SALE_PROGRESS_HEIGHT, 0, 0, leftPos + 80, topPos + 28, progressWidth, SALE_PROGRESS_HEIGHT);
            }
        }
    }
}
