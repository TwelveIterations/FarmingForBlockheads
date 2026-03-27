package net.blay09.mods.farmingforblockheads.neoforge.client;

import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.client.FarmingForBlockheadsClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = FarmingForBlockheads.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeFarmingForBlockheadsClient {
    public NeoForgeFarmingForBlockheadsClient(ModContainer modContainer, IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modContainer, modEventBus);
        BalmClient.initializeMod(FarmingForBlockheads.MOD_ID, context, FarmingForBlockheadsClient::initialize);
    }
}
