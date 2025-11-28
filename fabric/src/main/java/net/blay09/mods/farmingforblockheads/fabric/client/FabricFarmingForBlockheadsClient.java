package net.blay09.mods.farmingforblockheads.fabric.client;

import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.client.FarmingForBlockheadsClient;
import net.fabricmc.api.ClientModInitializer;

public class FabricFarmingForBlockheadsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BalmClient.initializeMod(FarmingForBlockheads.MOD_ID, FabricLoadContext.INSTANCE, FarmingForBlockheadsClient::initialize);
    }
}
