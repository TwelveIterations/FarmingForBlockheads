package net.blay09.mods.farmingforblockheads.block.entity;

import com.google.common.collect.ArrayListMultimap;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.BalmContainerProvider;
import net.blay09.mods.balm.world.ContainerUtils;
import net.blay09.mods.balm.world.DefaultContainer;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.api.FeedingTroughEvent;
import net.blay09.mods.farmingforblockheads.network.ChickenNestEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedingTroughBlockEntity extends BlockEntity implements BalmContainerProvider {

    private static final int TICK_INTERVAL = 20 * 5;

    private final DefaultContainer container = new DefaultContainer(1) {
        @Override
        public void setChanged() {
            isDirty = true;
            FeedingTroughBlockEntity.this.setChanged();
        }
    };

    private int tickTimer;
    private boolean isDirty;

    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.feedingTrough.value(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FeedingTroughBlockEntity blockEntity) {
        blockEntity.serverTick(level, pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        tickTimer++;
        if (tickTimer >= TICK_INTERVAL) {
            teehee();
            tickTimer = 0;
        }

        if (isDirty) {
            BalmBlockEntityUtils.sync(this);
            isDirty = false;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        container.clearContent();
        input.child("ItemHandler").ifPresent(it -> ContainerHelper.loadAllItems(it, container.getItems()));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output.child("ItemHandler"), container.getItems());
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return BalmBlockEntityUtils.createUpdatePacket(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return BalmBlockEntityUtils.createUpdateTag(registries, this::saveAdditional);
    }

    private void teehee() {
        ItemStack itemStack = container.getItem(0);
        if (itemStack.isEmpty() || itemStack.getCount() < 2) {
            return;
        }

        final float range = FarmingForBlockheadsConfig.getActive().feedingTroughRange;
        AABB aabb = new AABB(worldPosition.getX() - range,
                worldPosition.getY() - range,
                worldPosition.getZ() - range,
                worldPosition.getX() + range,
                worldPosition.getY() + range,
                worldPosition.getZ() + range);
        List<Animal> entities = level.getEntitiesOfClass(Animal.class, aabb);
        if (entities.isEmpty()) {
            return;
        }

        ArrayListMultimap<Class<? extends Animal>, Animal> map = ArrayListMultimap.create();
        for (Animal animal : entities) {
            map.put(animal.getClass(), animal);
        }

        List<Class<? extends Animal>> keys = new ArrayList<>(map.keySet());
        Collections.shuffle(keys);

        for (Class<? extends Animal> key : keys) {
            List<Animal> list = map.get(key);
            if (list.size() < FarmingForBlockheadsConfig.getActive().feedingTroughMaxAnimals) {
                final var breedingCandidates = new ArrayList<Animal>();
                for (Animal animal : list) {
                    if (isSubmissiveAndBreedable(animal, itemStack)) {
                        breedingCandidates.add(animal);
                        if (breedingCandidates.size() >= 2) {
                            break;
                        }
                    }
                }
                if (breedingCandidates.size() == 2) {
                    for (Animal animal : breedingCandidates) {
                        feed(animal, itemStack);
                    }
                }
            }
        }
    }

    private void feed(Animal animal, ItemStack itemStack) {
        FeedingTroughEvent event = new FeedingTroughEvent(this, animal, itemStack);
        FeedingTroughEvent.EVENT.invoker().accept(new FeedingTroughEvent(this, animal, itemStack));
        if (!event.isCanceled()) {
            animal.setInLove(null);
            ContainerUtils.extractItem(container, 0, 1, false);
            setChanged();
        }
        if (event.shouldPlayEffect()) {
            Balm.networking().sendToTracking(((ServerLevel) level), worldPosition, new ChickenNestEffectMessage(worldPosition));
        }
    }

    private boolean isSubmissiveAndBreedable(Animal animal, ItemStack itemStack) {
        // age is negative for babies, 0 for adults, and positive for adults that just created a baby (cooldown)
        return animal.getAge() == 0 && animal.canFallInLove() && !animal.isBaby() && animal.isFood(itemStack);
    }

    public ItemStack getContentStack() {
        return container.getItem(0);
    }

    @Override
    public Container getContainer() {
        return container;
    }
}
