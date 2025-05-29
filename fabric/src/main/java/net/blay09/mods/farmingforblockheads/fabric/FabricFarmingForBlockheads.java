package net.blay09.mods.farmingforblockheads.fabric;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.EmptyLoadContext;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.fabricmc.api.ModInitializer;

public class FabricFarmingForBlockheads implements ModInitializer {
    @Override
    public void onInitialize() {
        Balm.initializeMod(FarmingForBlockheads.MOD_ID, EmptyLoadContext.INSTANCE, FarmingForBlockheads::initialize);
    }
}
