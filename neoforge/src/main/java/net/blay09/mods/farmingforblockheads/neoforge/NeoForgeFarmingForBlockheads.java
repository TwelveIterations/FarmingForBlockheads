package net.blay09.mods.farmingforblockheads.neoforge;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(FarmingForBlockheads.MOD_ID)
public class NeoForgeFarmingForBlockheads {
    public NeoForgeFarmingForBlockheads(ModContainer modContainer, IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modContainer, modEventBus);
        Balm.initializeMod(FarmingForBlockheads.MOD_ID, context, FarmingForBlockheads::initialize);
    }
}
