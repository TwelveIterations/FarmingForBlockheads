package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.tags.BalmItemTags;
import net.blay09.mods.balm.world.BalmContainerProvider;
import net.blay09.mods.balm.world.ContainerUtils;
import net.blay09.mods.balm.world.DefaultContainer;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.network.ChickenNestEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ChickenNestBlockEntity extends BlockEntity implements BalmContainerProvider {

    private static final int TICK_INTERVAL = 20;

    private final DefaultContainer container = new DefaultContainer(1) {

        @Override
        public boolean canPlaceItem(int i, ItemStack itemStack) {
            return isEggItem(itemStack);
        }

        @Override
        public void setChanged() {
            isDirty = true;
            ChickenNestBlockEntity.this.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 4;
        }
    };

    private int tickTimer;
    private boolean isDirty;

    public ChickenNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.chickenNest.value(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChickenNestBlockEntity blockEntity) {
        blockEntity.serverTick(level, pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        tickTimer++;
        if (tickTimer >= TICK_INTERVAL) {
            stealEgg();
            tickTimer = 0;
        }

        if (isDirty) {
            BalmBlockEntityUtils.sync(this);
            isDirty = false;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        input.child("ItemHandler").ifPresent(it -> ContainerHelper.loadAllItems(it, container.getItems()));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output.child("ItemHandler"), container.getItems());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return BalmBlockEntityUtils.createUpdateTag(registries, this::saveAdditional);
    }

    private void stealEgg() {
        final float range = FarmingForBlockheadsConfig.getActive().chickenNestRange;
        AABB aabb = new AABB(worldPosition.getX() - range,
                worldPosition.getY() - range,
                worldPosition.getZ() - range,
                worldPosition.getX() + range,
                worldPosition.getY() + range,
                worldPosition.getZ() + range);
        List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, aabb, p -> p != null && isEggItem(p.getItem()));
        if (list.isEmpty()) {
            return;
        }
        ItemEntity entityItem = list.get(0);
        ItemStack originalStack = entityItem.getItem();
        ItemStack restStack = entityItem.getItem().copy();
        for (int i = 0; i < container.getContainerSize(); i++) {
            restStack = ContainerUtils.insertItem(container, i, restStack, false);
            if (restStack.isEmpty()) {
                break;
            }
        }
        if (restStack.isEmpty()) {
            entityItem.remove(Entity.RemovalReason.DISCARDED);
        } else {
            entityItem.setItem(restStack);
        }

        if (restStack.isEmpty() || restStack.getCount() != originalStack.getCount()) {
            Balm.networking().sendToTracking(((ServerLevel) level), worldPosition, new ChickenNestEffectMessage(worldPosition));
        }

        setChanged();
    }

    private boolean isEggItem(ItemStack item) {
        return item.is(BalmItemTags.EGGS);
    }

    public int getEggCount() {
        return container.getItem(0).getCount();
    }

    @Override
    public Container getContainer() {
        return container;
    }
}
