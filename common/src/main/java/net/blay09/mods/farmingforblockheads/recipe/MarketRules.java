package net.blay09.mods.farmingforblockheads.recipe;

import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.scope.ShogiScope;

import java.util.List;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public final class MarketRules {
    public static final ShogiScope SCOPE = Shogi.scope(id("rules"), scope -> scope.setDefaultNamespaces(List.of("farmingforblockheads", "shogi")));

    private MarketRules() {
    }
}
