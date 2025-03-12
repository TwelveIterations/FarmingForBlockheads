package net.blay09.mods.farmingforblockheads.network;

import net.blay09.mods.farmingforblockheads.menu.MarketMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

import java.util.ArrayList;
import java.util.List;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public record MarketRecipesMessage(List<RecipeDisplayEntry> recipes) implements CustomPacketPayload {

    public static final Type<MarketRecipesMessage> TYPE = new Type<>(id("market_recipes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MarketRecipesMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, RecipeDisplayEntry.STREAM_CODEC),
            MarketRecipesMessage::recipes,
            MarketRecipesMessage::new
    );

    public static void handle(Player player, MarketRecipesMessage message) {
        if (player.containerMenu instanceof MarketMenu marketMenu) {
            marketMenu.setRecipes(message.recipes);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
