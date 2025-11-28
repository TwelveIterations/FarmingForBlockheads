package net.blay09.mods.farmingforblockheads.fabric;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.fabricmc.api.ModInitializer;

public class FabricFarmingForBlockheads implements ModInitializer {
    @Override
    public void onInitialize() {
        Balm.initializeMod(FarmingForBlockheads.MOD_ID, FabricLoadContext.INSTANCE, FarmingForBlockheads::initialize);
    }
}
