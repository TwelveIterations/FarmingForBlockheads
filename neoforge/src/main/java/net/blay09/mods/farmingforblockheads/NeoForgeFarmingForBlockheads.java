package net.blay09.mods.farmingforblockheads;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.neoforge.NeoForgeLoadContext;
import net.blay09.mods.farmingforblockheads.block.entity.ModBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@Mod(FarmingForBlockheads.MOD_ID)
public class NeoForgeFarmingForBlockheads {
    public NeoForgeFarmingForBlockheads(IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modEventBus);
        Balm.initializeMod(FarmingForBlockheads.MOD_ID, context, FarmingForBlockheads::initialize);

        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.chickenNest.get(),
                (blockEntity, context) -> new InvWrapper(blockEntity.getContainer()));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.feedingTrough.get(),
                (blockEntity, context) -> new InvWrapper(blockEntity.getContainer()));
    }
}
