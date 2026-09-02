package net.blay09.mods.farmingforblockheads.network;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.menu.MarketMenu;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeImpl;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public record MarketPlaceRecipeMessage(int containerId, RecipeDisplayId recipe, boolean useMaxItems) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketPlaceRecipeMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.CONTAINER_ID,
            MarketPlaceRecipeMessage::containerId,
            RecipeDisplayId.STREAM_CODEC,
            MarketPlaceRecipeMessage::recipe,
            ByteBufCodecs.BOOL,
            MarketPlaceRecipeMessage::useMaxItems,
            MarketPlaceRecipeMessage::new);

    public static final CustomPacketPayload.Type<MarketPlaceRecipeMessage> TYPE = new CustomPacketPayload.Type<>(id("market_put_in_basket"));

    public static void handle(ServerPlayer player, MarketPlaceRecipeMessage message) {
        player.resetLastActionTime();
        if (!player.isSpectator() && player.containerMenu.containerId == message.containerId()) {
            if (!player.containerMenu.stillValid(player)) {
                FarmingForBlockheads.logger.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else {
                final var serverDisplayInfo = player.level().getServer().getRecipeManager().getRecipeFromDisplay(message.recipe());
                if (serverDisplayInfo != null) {
                    final var recipeHolder = serverDisplayInfo.parent();
                    if (player.containerMenu instanceof MarketMenu marketMenu) {
                        if (recipeHolder.value() instanceof MarketRecipeImpl marketRecipe
                                && marketRecipe.isEnabled()
                                && marketMenu.containsRecipeDisplayId(message.recipe())) {
                            if (recipeHolder.value().placementInfo().isImpossibleToPlace()) {
                                FarmingForBlockheads.logger.debug("Player {} tried to place impossible recipe {}", player, recipeHolder.id().identifier());
                                return;
                            }

                            final var postPlaceAction = marketMenu.handlePlacement(message.useMaxItems(),
                                    player.isCreative(),
                                    recipeHolder,
                                    player.level(),
                                    player.getInventory());
                            if (postPlaceAction == RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE) {
                                player.connection.send(new ClientboundPlaceGhostRecipePacket(player.containerMenu.containerId,
                                        serverDisplayInfo.display().display()));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
