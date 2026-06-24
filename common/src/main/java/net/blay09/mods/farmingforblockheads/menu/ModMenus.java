package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.balm.world.BalmMenuFactory;
import net.blay09.mods.balm.world.inventory.BalmMenuTypeRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
    public static Holder<MenuType<MarketMenu>> market;
    public static Holder<MenuType<ShippingBinMenu>> shippingBin;

    public static void initialize(BalmMenuTypeRegistrar menus) {
        market = menus.register("market", new BalmMenuFactory<MarketMenu, BlockPos>() {
            @Override
            public MarketMenu create(int windowId, Inventory inventory, BlockPos data) {
                return new MarketMenu(windowId, inventory, ContainerLevelAccess.create(inventory.player.level(), data));
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, BlockPos> getStreamCodec() {
                return BlockPos.STREAM_CODEC.cast();
            }
        }).asHolder();
        shippingBin = menus.register("shipping_bin", new BalmMenuFactory<ShippingBinMenu, Unit>() {
            @Override
            public ShippingBinMenu create(int windowId, Inventory inventory, Unit data) {
                return new ShippingBinMenu(windowId, inventory);
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, Unit> getStreamCodec() {
                return Unit.STREAM_CODEC.cast();
            }
        }).asHolder();
    }

}
