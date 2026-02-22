package net.blay09.mods.farmingforblockheads.registry;

import net.blay09.mods.farmingforblockheads.api.MarketDefault;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public class CompositeMarketDefault implements MarketDefault {

    private final List<MarketDefault> marketDefaults;

    public CompositeMarketDefault(List<MarketDefault> marketDefaults) {
        this.marketDefaults = marketDefaults;
    }

    @Override
    public Optional<Boolean> enabledByDefault() {
        for (int i = marketDefaults.size() - 1; i >= 0; i--) {
            final var enabledByDefault = marketDefaults.get(i).enabledByDefault();
            if (enabledByDefault.isPresent()) {
                return enabledByDefault;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Identifier> category() {
        for (int i = marketDefaults.size() - 1; i >= 0; i--) {
            final var category = marketDefaults.get(i).category();
            if (category.isPresent()) {
                return category;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Payment> payment() {
        for (int i = marketDefaults.size() - 1; i >= 0; i--) {
            final var payment = marketDefaults.get(i).payment();
            if (payment.isPresent()) {
                return payment;
            }
        }
        return Optional.empty();
    }
}
