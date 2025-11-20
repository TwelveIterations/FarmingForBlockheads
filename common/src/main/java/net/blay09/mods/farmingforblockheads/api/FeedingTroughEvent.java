package net.blay09.mods.farmingforblockheads.api;

import net.blay09.mods.balm.platform.event.BidirectionalEventMapper;
import net.blay09.mods.balm.platform.event.EventMapper;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public class FeedingTroughEvent {

    public static final BidirectionalEventMapper<Consumer<FeedingTroughEvent>> EVENT = EventMapper.createBound(FeedingTroughEvent.class);

    private final BlockEntity blockEntity;
    private final Animal entity;
    private final ItemStack itemStack;

    private boolean shouldPlayEffect = true;
    private boolean canceled;

    public FeedingTroughEvent(BlockEntity blockEntity, Animal entity, ItemStack itemStack) {
        this.blockEntity = blockEntity;
        this.entity = entity;
        this.itemStack = itemStack;
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public Animal getEntity() {
        return entity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean shouldPlayEffect() {
        return shouldPlayEffect;
    }

    public void setShouldPlayEffect(boolean shouldPlayEffect) {
        this.shouldPlayEffect = shouldPlayEffect;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
