package net.blay09.mods.farmingforblockheads.client.gui.screen;

import net.blay09.mods.farmingforblockheads.menu.FishingBarrelMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class FishingBarrelScreen extends AbstractContainerScreen<FishingBarrelMenu> {

    private static final Identifier TEXTURE = id("textures/gui/fishing_barrel.png");

    public FishingBarrelScreen(FishingBarrelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }
}
