package net.blay09.mods.farmingforblockheads.client;

import net.blay09.mods.balm.client.gui.screens.inventory.BalmMenuScreenRegistrar;
import net.blay09.mods.farmingforblockheads.client.gui.screen.MarketScreen;
import net.blay09.mods.farmingforblockheads.menu.ModMenus;

public class ModScreens {
    public static void initialize(BalmMenuScreenRegistrar screens) {
        screens.register(ModMenus.market, MarketScreen::new);
    }
}
