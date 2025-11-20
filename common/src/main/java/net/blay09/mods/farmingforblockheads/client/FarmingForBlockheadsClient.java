package net.blay09.mods.farmingforblockheads.client;

import net.blay09.mods.balm.client.BalmClientRegistrars;

public class FarmingForBlockheadsClient {

    public static void initialize(BalmClientRegistrars registrars) {
        registrars.menuScreens(ModScreens::initialize);
        registrars.entityRenderers(ModRenderers::initialize);
        registrars.blockEntityRenderers(ModRenderers::initialize);
    }

}
