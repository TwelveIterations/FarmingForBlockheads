package net.blay09.mods.farmingforblockheads.registry;

import net.blay09.mods.farmingforblockheads.api.MarketDefault;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record MarketDefaultImpl(Optional<Boolean> enabledByDefault, Optional<Identifier> category, Optional<Payment> payment) implements MarketDefault {
}
