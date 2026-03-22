package net.blay09.mods.farmingforblockheads.network;

import net.blay09.mods.farmingforblockheads.api.MarketCategory;
import net.blay09.mods.farmingforblockheads.menu.MarketMenu;
import net.blay09.mods.farmingforblockheads.registry.MarketCategoryImpl;
import net.blay09.mods.farmingforblockheads.registry.SimpleHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.List;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public record MarketCategoriesMessage(List<SimpleHolder<MarketCategory>> categories) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MarketCategoriesMessage> TYPE = new CustomPacketPayload.Type<>(id("market_categories"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MarketCategoriesMessage> STREAM_CODEC = StreamCodec.of(
            MarketCategoriesMessage::encode,
            MarketCategoriesMessage::decode
    );

    public static MarketCategoriesMessage decode(RegistryFriendlyByteBuf buf) {
        final var count = buf.readInt();
        final var categories = new ArrayList<SimpleHolder<MarketCategory>>();
        for (int i = 0; i < count; i++) {
            final var id = buf.readIdentifier();
            final var iconStack = ItemStackTemplate.STREAM_CODEC.decode(buf);
            final var sortIndex = buf.readInt();
            final var tooltip = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
            final var category = new MarketCategoryImpl(iconStack, sortIndex, tooltip);
            categories.add(SimpleHolder.of(id, category));
        }
        return new MarketCategoriesMessage(categories);
    }

    public static void encode(RegistryFriendlyByteBuf buf, MarketCategoriesMessage message) {
        buf.writeInt(message.categories.size());
        message.categories.forEach(holder -> {
            buf.writeIdentifier(holder.id());
            final var category = holder.value();
            ItemStackTemplate.STREAM_CODEC.encode(buf, category.iconStack());
            buf.writeInt(category.sortIndex());
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, category.tooltip());
        });
    }

    public static void handle(Player player, MarketCategoriesMessage message) {
        if (player.containerMenu instanceof MarketMenu marketMenu) {
            marketMenu.setCategories(message.categories);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
