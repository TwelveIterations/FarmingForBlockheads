package net.blay09.mods.farmingforblockheads;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.forge.ForgeLoadContext;
import net.blay09.mods.farmingforblockheads.client.FarmingForBlockheadsClient;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(FarmingForBlockheads.MOD_ID)
public class ForgeFarmingForBlockheads {
    public ForgeFarmingForBlockheads(FMLJavaModLoadingContext context) {
        final var loadContext = new ForgeLoadContext(context.getModBusGroup());
        Balm.initializeMod(FarmingForBlockheads.MOD_ID, loadContext, FarmingForBlockheads::initialize);
        if (FMLEnvironment.dist.isClient()) {
            BalmClient.initializeMod(FarmingForBlockheads.MOD_ID, loadContext, FarmingForBlockheadsClient::initialize);
        }
    }
}
