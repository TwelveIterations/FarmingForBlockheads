package net.blay09.mods.farmingforblockheads.api;

import net.blay09.mods.balm.platform.event.BidirectionalEventMapper;
import net.blay09.mods.balm.platform.event.EventMapper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class MarketClearRecipeEvent {
    public static final BidirectionalEventMapper<Consumer<MarketClearRecipeEvent>> EVENT =  EventMapper.createBound(MarketClearRecipeEvent.class);

    private final Player player;
    private final Container paymentSlots;
    private boolean skipDefault;

    public MarketClearRecipeEvent(Player player, Container paymentSlots) {
        this.player = player;
        this.paymentSlots = paymentSlots;
    }

    public Player player() {
        return player;
    }

    public Container paymentSlots() {
        return paymentSlots;
    }

    public boolean skipsDefault() {
        return skipDefault;
    }

    public void skipDefault(boolean skipDefault) {
        this.skipDefault = skipDefault;
    }
}
