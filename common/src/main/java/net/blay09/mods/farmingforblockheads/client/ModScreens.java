package net.blay09.mods.farmingforblockheads.client;

import net.blay09.mods.balm.api.client.screen.BalmScreens;
import net.blay09.mods.farmingforblockheads.client.gui.screen.MarketScreen;
import net.blay09.mods.farmingforblockheads.menu.ModMenus;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModScreens {
    public static void initialize(BalmScreens screens) {
        screens.registerScreen(id("market"), ModMenus.market::get, MarketScreen::new);
    }
}
