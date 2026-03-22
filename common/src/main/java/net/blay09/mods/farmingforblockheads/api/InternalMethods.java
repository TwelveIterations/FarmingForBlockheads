package net.blay09.mods.farmingforblockheads.api;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;

public interface InternalMethods {
    Optional<MarketCategory> getMarketCategory(Identifier id);

    Map<Identifier, MarketCategory> getMarketCategories();
}
