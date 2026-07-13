package net.blay09.mods.farmingforblockheads.menu;

import net.blay09.mods.balm.world.inventory.QuickMove;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.block.entity.FishingBarrelBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class FishingBarrelMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData containerData;
    private final ContainerLevelAccess access;
    private final QuickMove.Routing quickMove;

    public FishingBarrelMenu(int windowId, Inventory playerInventory) {
        this(windowId,
                playerInventory,
                new SimpleContainer(FishingBarrelBlockEntity.CONTAINER_SIZE),
                new SimpleContainerData(FishingBarrelBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }

    public FishingBarrelMenu(int windowId, Inventory playerInventory, Container container, ContainerData containerData, ContainerLevelAccess access) {
        super(ModMenus.fishingBarrel.value(), windowId);
        this.container = container;
        this.containerData = containerData;
        this.access = access;

        checkContainerDataCount(containerData, FishingBarrelBlockEntity.DATA_COUNT);
        addDataSlots(containerData);

        container.startOpen(playerInventory.player);

        addSlot(new FishingBarrelRodSlot(container, FishingBarrelBlockEntity.ROD_SLOT, 37, 35));

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                addSlot(new ShippingBinOutputSlot(container, 1 + j + i * 4, 62 + j * 18, 26 + i * 18));
            }
        }

        addStandardInventorySlots(playerInventory, 8, 84);

        quickMove = QuickMove.create(this, this::moveItemStackTo)
                .slotRange("rod", FishingBarrelBlockEntity.ROD_SLOT, FishingBarrelBlockEntity.ROD_SLOT + 1)
                .route(FishingBarrelBlockEntity::isFishingRod, QuickMove.PLAYER, "rod")
                .build();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.fishingBarrel.asBlock());
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

    public int getRemainingFishingTicks() {
        return containerData.get(FishingBarrelBlockEntity.DATA_REMAINING_FISHING_TICKS);
    }

    public int getFishingIntervalTicks() {
        return containerData.get(FishingBarrelBlockEntity.DATA_FISHING_INTERVAL_TICKS);
    }
}
