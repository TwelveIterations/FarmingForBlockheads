package net.blay09.mods.farmingforblockheads.api;

import net.minecraft.resources.Identifier;

import java.util.Optional;

public interface MarketDefault {
	Optional<Identifier> category();
	Optional<Payment> payment();
}
