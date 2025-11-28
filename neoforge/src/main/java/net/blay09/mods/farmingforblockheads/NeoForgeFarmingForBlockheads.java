package net.blay09.mods.farmingforblockheads;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(FarmingForBlockheads.MOD_ID)
public class NeoForgeFarmingForBlockheads {
    public NeoForgeFarmingForBlockheads(IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modEventBus);
        Balm.initializeMod(FarmingForBlockheads.MOD_ID, context, FarmingForBlockheads::initialize);
    }
}
