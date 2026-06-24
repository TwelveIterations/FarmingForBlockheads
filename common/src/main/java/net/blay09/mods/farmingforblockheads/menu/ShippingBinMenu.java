package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.balm.world.inventory.QuickMove;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.block.entity.ShippingBinBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShippingBinMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData containerData;
    private final ContainerLevelAccess access;
    private final QuickMove.Routing quickMove;

    public ShippingBinMenu(int windowId, Inventory playerInventory) {
        this(windowId, playerInventory, new SimpleContainer(ShippingBinBlockEntity.CONTAINER_SIZE), new SimpleContainerData(ShippingBinBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public ShippingBinMenu(int windowId, Inventory playerInventory, Container container, ContainerData containerData, ContainerLevelAccess access) {
        super(ModMenus.shippingBin.value(), windowId);
        this.container = container;
        this.containerData = containerData;
        this.access = access;

        checkContainerDataCount(containerData, ShippingBinBlockEntity.DATA_COUNT);
        addDataSlots(containerData);

        container.startOpen(playerInventory.player);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new Slot(container, j + i * 3, 20 + j * 18, 20 + i * 18));
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new ShippingBinOutputSlot(container, ShippingBinBlockEntity.INPUT_SLOTS + j + i * 3, 112 + j * 18, 20 + i * 18));
            }
        }

        addStandardInventorySlots(playerInventory, 8, 84);

        quickMove = QuickMove.create(this, this::moveItemStackTo)
                .slotRange("input", 0, ShippingBinBlockEntity.INPUT_SLOTS)
                .route(QuickMove.PLAYER, "input")
                .build();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.shippingBin.asBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMove.transfer(this, player, index);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    public int getFill() {
        return containerData.get(ShippingBinBlockEntity.DATA_FILL);
    }

    public int getFillCapacity() {
        return containerData.get(ShippingBinBlockEntity.DATA_FILL_CAPACITY);
    }
}
