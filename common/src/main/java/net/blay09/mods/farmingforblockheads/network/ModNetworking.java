package net.blay09.mods.farmingforblockheads.network;

import net.blay09.mods.balm.api.network.BalmNetworking;

public class ModNetworking {
    public static void initialize(BalmNetworking networking) {
        networking.registerClientboundPacket(MarketCategoriesMessage.TYPE, MarketCategoriesMessage.class, MarketCategoriesMessage.STREAM_CODEC, MarketCategoriesMessage::handle);
        networking.registerClientboundPacket(MarketRecipesMessage.TYPE, MarketRecipesMessage.class, MarketRecipesMessage.STREAM_CODEC, MarketRecipesMessage::handle);
        networking.registerClientboundPacket(ChickenNestEffectMessage.TYPE, ChickenNestEffectMessage.class, ChickenNestEffectMessage.STREAM_CODEC, ChickenNestEffectMessage::handle);
        networking.registerServerboundPacket(MarketPlaceRecipeMessage.TYPE, MarketPlaceRecipeMessage.class, MarketPlaceRecipeMessage.STREAM_CODEC, MarketPlaceRecipeMessage::handle);
    }
}
