package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.*;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.blay09.mods.farmingforblockheads.block.FishingBarrelBlock;
import net.blay09.mods.farmingforblockheads.menu.FishingBarrelMenu;
import net.blay09.mods.farmingforblockheads.recipe.FarmingForBlockheadsRules;
import net.blay09.mods.farmingforblockheads.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FishingBarrelBlockEntity extends BlockEntity implements BalmContainerProvider, BalmMenuProvider<Unit> {

    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("gameplay/fishing"));

    public static final int ROD_SLOT = 0;
    public static final int STORAGE_SLOTS = 8;
    public static final int CONTAINER_SIZE = 1 + STORAGE_SLOTS;
    public static final int DATA_REMAINING_FISHING_TICKS = 0;
    public static final int DATA_FISHING_INTERVAL_TICKS = 1;
    public static final int DATA_COUNT = 2;
    public static final int CATCH_ANIMATION_TICKS = 36;
    public static final int CATCH_BITE_TICKS = 10;
    public static final int CATCH_RETRIEVE_TICKS = 18;
    private static final int CATCH_SPLASH_TICKS = CATCH_BITE_TICKS + 1;

    private final DefaultContainer container = new DefaultContainer(CONTAINER_SIZE) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack itemStack) {
            return slot == ROD_SLOT && isFishingRod(itemStack);
        }

        @Override
        public boolean canTakeItem(Container into, int slot, ItemStack itemStack) {
            return slot != ROD_SLOT;
        }

        @Override
        public void setChanged() {
            needsClientSync = true;
            FishingBarrelBlockEntity.this.setChanged();
        }
    };
    private final Container storageContainer = new SubContainer(container, 1, CONTAINER_SIZE);
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_REMAINING_FISHING_TICKS -> remainingFishingTicks;
                case DATA_FISHING_INTERVAL_TICKS -> fishingIntervalTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };
    private final List<ItemStack> outputBuffer = new ArrayList<>();
    private final List<ItemStack> pendingCatchItems = new ArrayList<>();

    private boolean needsClientSync;
    private int remainingFishingTicks;
    private int fishingIntervalTicks;
    private int catchAnimationId;
    private int clientCatchAnimationTicks;
    private int clientCatchAnimationId = -1;

    public FishingBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.fishingBarrel.value(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FishingBarrelBlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel) {
            blockEntity.tick(serverLevel);
            if (blockEntity.needsClientSync) {
                BalmBlockEntityUtils.sync(blockEntity);
                blockEntity.needsClientSync = false;
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FishingBarrelBlockEntity blockEntity) {
        blockEntity.clientTick();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        container.clearContent();
        outputBuffer.clear();
        pendingCatchItems.clear();
        input.child("ItemHandler").ifPresent(it -> ContainerHelper.loadAllItems(it, container.getItems()));
        remainingFishingTicks = input.getIntOr("RemainingFishingTicks", input.getIntOr("TicksUntilFishing", 0));
        fishingIntervalTicks = input.getIntOr("FishingIntervalTicks", input.getIntOr("NextFishingInterval", 0));
        catchAnimationId = input.getIntOr("CatchAnimationId", 0);
        for (final var stack : input.listOrEmpty("OutputBuffer", ItemStack.CODEC)) {
            if (!stack.isEmpty()) {
                outputBuffer.add(stack);
            }
        }
        for (final var stack : input.listOrEmpty("PendingCatchItems", ItemStack.CODEC)) {
            if (!stack.isEmpty()) {
                pendingCatchItems.add(stack);
            }
        }
        if (!pendingCatchItems.isEmpty()) {
            remainingFishingTicks = CATCH_ANIMATION_TICKS;
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output.child("ItemHandler"), container.getItems());
        output.putInt("RemainingFishingTicks", remainingFishingTicks);
        output.putInt("FishingIntervalTicks", fishingIntervalTicks);
        output.putInt("CatchAnimationId", catchAnimationId);
        final var outputBufferList = output.list("OutputBuffer", ItemStack.CODEC);
        for (final var stack : outputBuffer) {
            outputBufferList.add(stack);
        }
        final var pendingCatchItemsList = output.list("PendingCatchItems", ItemStack.CODEC);
        for (final var stack : pendingCatchItems) {
            pendingCatchItemsList.add(stack);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            ContainerUtils.dropItems(container, level, pos);
            for (final var stack : outputBuffer) {
                if (!stack.isEmpty()) {
                    final var itemEntity = new ItemEntity(level, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, stack);
                    itemEntity.setDeltaMovement(0f, 0.2f, 0f);
                    level.addFreshEntity(itemEntity);
                }
            }
            for (final var stack : pendingCatchItems) {
                if (!stack.isEmpty()) {
                    final var itemEntity = new ItemEntity(level, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, stack);
                    itemEntity.setDeltaMovement(0f, 0.2f, 0f);
                    level.addFreshEntity(itemEntity);
                }
            }
            outputBuffer.clear();
            pendingCatchItems.clear();
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return BalmBlockEntityUtils.createUpdatePacket(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return BalmBlockEntityUtils.createUpdateTag(registries, this::saveAdditional);
    }

    public void openMenu(Player player) {
        Balm.networking().openMenu(player, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.farmingforblockheads.fishing_barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new FishingBarrelMenu(windowId, playerInventory, container, containerData, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public Unit getScreenOpeningData(ServerPlayer serverPlayer) {
        return Unit.INSTANCE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Unit> getScreenStreamCodec() {
        return Unit.STREAM_CODEC.cast();
    }

    @Override
    public Container getContainer() {
        return container;
    }

    public int getCatchAnimationTicks() {
        return level != null && level.isClientSide() ? clientCatchAnimationTicks : getServerCatchAnimationTicks();
    }

    public List<ItemStack> getPendingCatchItems() {
        return pendingCatchItems;
    }

    public Direction getFacing() {
        final var state = getBlockState();
        return state.hasProperty(FishingBarrelBlock.FACING) ? state.getValue(FishingBarrelBlock.FACING) : Direction.NORTH;
    }

    public boolean hasFishingRod() {
        final var rod = container.getItem(ROD_SLOT);
        return !rod.isEmpty() && isFishingRod(rod);
    }

    public static boolean isFishingRod(ItemStack itemStack) {
        return itemStack.is(ModItemTags.FISHING_RODS);
    }

    private void clientTick() {
        if (catchAnimationId != clientCatchAnimationId) {
            clientCatchAnimationId = catchAnimationId;
            clientCatchAnimationTicks = hasPendingCatchItems() ? CATCH_ANIMATION_TICKS : 0;
            return;
        }

        if (clientCatchAnimationTicks > 0) {
            clientCatchAnimationTicks--;
        }
    }

    private void tick(ServerLevel level) {
        if (moveBufferedOutputsToStorageSlots()) {
            setChanged();
            needsClientSync = true;
        }
        continueCatch(level);

        final var rod = container.getItem(ROD_SLOT);
        if (rod.isEmpty() || !isFishingRod(rod)) {
            stopFishing();
            return;
        }

        if (isCatchAnimationActive() || hasBufferedOutputs() || hasPendingCatchItems()) {
            return;
        }

        if (!canFishAtCurrentPosition(level)) {
            stopFishing();
            return;
        }

        if (remainingFishingTicks <= 0) {
            scheduleNextFishing(rod);
            setChanged();
        } else {
            remainingFishingTicks--;
            setChanged();
        }

        if (remainingFishingTicks <= 0 && fish(level, rod)) {
            damageRod(level, rod);
            remainingFishingTicks = CATCH_ANIMATION_TICKS;
            catchAnimationId++;
            setChanged();
            needsClientSync = true;
        }

        if (moveBufferedOutputsToStorageSlots()) {
            setChanged();
            needsClientSync = true;
        }
    }

    private void continueCatch(ServerLevel level) {
        if (!hasPendingCatchItems()) {
            return;
        }

        if (remainingFishingTicks > 0) {
            spawnCatchParticles(level);
            remainingFishingTicks--;
        }

        if (remainingFishingTicks <= 0 && movePendingCatchToStorageSlots()) {
            setChanged();
            needsClientSync = true;
        }
    }

    private void stopFishing() {
        if (remainingFishingTicks != 0) {
            remainingFishingTicks = 0;
            setChanged();
        }

        if (!pendingCatchItems.isEmpty()) {
            needsClientSync = true;
            if (movePendingCatchToStorageSlots()) {
                setChanged();
            }
        }
    }

    private boolean fish(ServerLevel level, ItemStack rod) {
        final var lootTable = level.getServer().reloadableRegistries().getLootTable(LOOT_TABLE);
        final var lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.TOOL, rod)
                .create(LootContextParamSets.FISHING);
        final var loot = lootTable.getRandomItems(lootParams);
        if (loot.isEmpty()) {
            return false;
        }

        pendingCatchItems.clear();
        for (final var itemStack : loot) {
            pendingCatchItems.add(itemStack.copy());
        }
        return true;
    }

    private void damageRod(ServerLevel level, ItemStack rod) {
        final var brokenRod = rod.copy();
        brokenRod.setCount(1);
        rod.hurtAndBreak(1, level, null, _ -> spawnRodBreakEffects(level, brokenRod));
    }

    private void spawnRodBreakEffects(ServerLevel level, ItemStack brokenRod) {
        final var facing = getFacing();
        final double x = worldPosition.getX() + 0.5 + facing.getStepX() * 0.18;
        final double y = worldPosition.getY() + 0.9;
        final double z = worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.18;

        level.playSound(null, x, y, z, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, brokenRod.getItem()), x, y, z, 16, 0.18, 0.12, 0.18, 0.05);
    }

    private void scheduleNextFishing(ItemStack rod) {
        fishingIntervalTicks = FarmingForBlockheadsRules.getFishingBarrelInterval(this, rod);
        remainingFishingTicks = fishingIntervalTicks;
    }

    private boolean moveBufferedOutputsToStorageSlots() {
        boolean changed = false;
        for (int slot = 0; slot < outputBuffer.size(); slot++) {
            final var stack = outputBuffer.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            final var remainder = ContainerUtils.insertItemStacked(storageContainer, stack, false);
            if (remainder.getCount() != stack.getCount()) {
                if (remainder.isEmpty()) {
                    outputBuffer.remove(slot);
                    slot--;
                } else {
                    outputBuffer.set(slot, remainder);
                }
                changed = true;
            }
        }

        return changed;
    }

    private boolean movePendingCatchToStorageSlots() {
        boolean changed = false;
        for (int slot = 0; slot < pendingCatchItems.size(); slot++) {
            final var stack = pendingCatchItems.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            final var remainder = ContainerUtils.insertItemStacked(storageContainer, stack, false);
            if (remainder.isEmpty()) {
                pendingCatchItems.remove(slot);
                slot--;
            } else {
                outputBuffer.add(remainder.copy());
                pendingCatchItems.remove(slot);
                slot--;
            }
            changed = true;
        }

        return changed;
    }

    private boolean canFishAtCurrentPosition(ServerLevel level) {
        final var state = getBlockState();
        if (!state.hasProperty(FishingBarrelBlock.FACING)) {
            return false;
        }

        final var fishingPos = worldPosition.relative(state.getValue(FishingBarrelBlock.FACING)).below();
        return level.getFluidState(fishingPos).is(FluidTags.WATER);
    }

    private boolean hasBufferedOutputs() {
        for (final var stack : outputBuffer) {
            if (!stack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasPendingCatchItems() {
        for (final var stack : pendingCatchItems) {
            if (!stack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean isCatchAnimationActive() {
        return getServerCatchAnimationTicks() > 0;
    }

    private int getServerCatchAnimationTicks() {
        return hasPendingCatchItems() ? remainingFishingTicks : 0;
    }

    private void spawnCatchParticles(ServerLevel level) {
        final int elapsed = CATCH_ANIMATION_TICKS - getServerCatchAnimationTicks();
        final var facing = getFacing();
        final double x = worldPosition.getX() + 0.5 + facing.getStepX();
        final double y = worldPosition.getY() + 0.02;
        final double z = worldPosition.getZ() + 0.5 + facing.getStepZ();

        if (elapsed == 0) {
            level.sendParticles(ParticleTypes.BUBBLE, x, y - 0.1, z, 3, 0.14, 0.05, 0.14, 0.01);
            level.sendParticles(ParticleTypes.BUBBLE_POP, x, y + 0.02, z, 1, 0.1, 0.01, 0.1, 0.005);
        } else if (elapsed == CATCH_SPLASH_TICKS) {
            level.sendParticles(ParticleTypes.SPLASH, x, y + 0.02, z, 24, 0.28, 0.08, 0.28, 0.08);
            level.sendParticles(ParticleTypes.BUBBLE_POP, x, y + 0.02, z, 6, 0.2, 0.02, 0.2, 0.02);
        }
    }
}
