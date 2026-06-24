package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.mixin.RecipeManagerAccessor;
import net.blay09.mods.balm.world.*;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.blay09.mods.farmingforblockheads.ShippingBinSalesData;
import net.blay09.mods.farmingforblockheads.menu.ShippingBinMenu;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.recipe.ShippingBinRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ShippingBinBlockEntity extends BlockEntity implements BalmContainerProvider, BalmMenuProvider<Unit> {

    public static final int INPUT_SLOTS = 6;
    public static final int OUTPUT_SLOTS = 6;
    public static final int CONTAINER_SIZE = INPUT_SLOTS + OUTPUT_SLOTS;
    public static final int FILL_CAPACITY = 64;
    public static final int DATA_FILL = 0;
    public static final int DATA_FILL_CAPACITY = 1;
    public static final int DATA_COUNT = 2;

    private final DefaultContainer container = new DefaultContainer(CONTAINER_SIZE) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack itemStack) {
            return slot < INPUT_SLOTS;
        }

        @Override
        public void setChanged() {
            isDirty = true;
            ShippingBinBlockEntity.this.setChanged();
            if (!processing && level instanceof ServerLevel serverLevel) {
                tryProcessSales(serverLevel);
            }
        }
    };

    private final Container outputContainer = new SubContainer(container, INPUT_SLOTS, CONTAINER_SIZE);
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_FILL -> level instanceof ServerLevel serverLevel ? getFill(serverLevel) : 0;
                case DATA_FILL_CAPACITY -> FILL_CAPACITY;
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

    private boolean isDirty;
    private boolean processing;
    private int fillLevel;

    public ShippingBinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.shippingBin.value(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ShippingBinBlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel) {
            blockEntity.tryProcessSales(serverLevel);
            if (blockEntity.isDirty) {
                BalmBlockEntityUtils.sync(blockEntity);
                blockEntity.isDirty = false;
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        container.clearContent();
        outputBuffer.clear();
        input.child("ItemHandler").ifPresent(it -> ContainerHelper.loadAllItems(it, container.getItems()));
        fillLevel = input.getIntOr("FillLevel", 0);
        for (final var stack : input.listOrEmpty("OutputBuffer", ItemStack.CODEC)) {
            if (!stack.isEmpty()) {
                outputBuffer.add(stack);
            }
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output.child("ItemHandler"), container.getItems());
        output.putInt("FillLevel", fillLevel);
        final var outputBufferList = output.list("OutputBuffer", ItemStack.CODEC);
        for (final var stack : outputBuffer) {
            outputBufferList.add(stack);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            ContainerUtils.dropItems(container, level, pos);
            for (final var stack : outputBuffer) {
                if (!stack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(level,
                            pos.getX() + 0.5f,
                            pos.getY() + 0.5f,
                            pos.getZ() + 0.5f,
                            stack);
                    itemEntity.setDeltaMovement(0f, 0.2f, 0f);
                    level.addFreshEntity(itemEntity);
                }
            }
            outputBuffer.clear();
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
        return Component.translatable("container.farmingforblockheads.shipping_bin");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new ShippingBinMenu(windowId, playerInventory, container, containerData, ContainerLevelAccess.create(level, worldPosition));
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

    public int getFillLevel() {
        return fillLevel;
    }

    private void tryProcessSales(ServerLevel level) {
        if (processing) {
            return;
        }

        processing = true;
        try {
            fillLevel = getFill(level);
            boolean changed = moveBufferedOutputsToOutputSlots();
            if (hasBufferedOutputs()) {
                if (changed) {
                    setChanged();
                    isDirty = true;
                }
                return;
            }

            while (processSaleCycle(level)) {
                changed = true;
                changed |= moveBufferedOutputsToOutputSlots();
                fillLevel = getFill(level);
                if (hasBufferedOutputs()) {
                    break;
                }
            }

            if (changed) {
                setChanged();
                isDirty = true;
            }
        } finally {
            processing = false;
        }
    }

    private @Nullable RecipeHolder<ShippingBinRecipe> findRecipe(ServerLevel level, ItemStack itemStack) {
        final var recipeInput = new SingleRecipeInput(itemStack);
        final var recipeMap = ((RecipeManagerAccessor) level.getServer().getRecipeManager()).balm$getRecipeMap();
        return recipeMap.byType(ModRecipes.shippingBinRecipe.type()).stream()
                .filter(it -> it.value().matches(recipeInput, level))
                .filter(it -> it.value().canSell(this, itemStack, it.id().identifier()))
                .findFirst()
                .orElse(null);
    }

    private int getFill(ServerLevel level) {
        int fillLevel = 0;
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            final var stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            final var recipeHolder = findRecipe(level, stack);
            if (recipeHolder != null) {
                fillLevel += stack.getCount() * recipeHolder.value().fill();
            }
        }

        return fillLevel;
    }

    private boolean processSaleCycle(ServerLevel level) {
        final var sales = new LinkedHashMap<RecipeHolder<ShippingBinRecipe>, Sale>();
        final var fillLevel = getFill(level);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            final var stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            final var recipeHolder = findRecipe(level, stack);
            if (recipeHolder == null) {
                continue;
            }

            final var sale = sales.get(recipeHolder);
            if (sale != null) {
                sales.put(recipeHolder, new Sale(sale.stack(), sale.count() + stack.getCount()));
            } else {
                sales.put(recipeHolder, new Sale(stack.copy(), stack.getCount()));
            }
        }

        if (fillLevel < FILL_CAPACITY) {
            return false;
        }

        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            final var stack = container.getItem(slot);
            if (!stack.isEmpty() && findRecipe(level, stack) != null) {
                container.removeItem(slot, stack.getCount());
            }
        }

        sales.forEach((recipeHolder, sale) -> {
            final var remainder = ContainerUtils.insertItemStacked(outputContainer, recipeHolder.value().result(this, sale.stack(), recipeHolder.id().identifier()), false);
            if (!remainder.isEmpty()) {
                outputBuffer.add(remainder.copy());
            }
            recordSale(level, recipeHolder, sale.count());
        });
        return true;
    }

    private record Sale(ItemStack stack, int count) {
    }

    private boolean moveBufferedOutputsToOutputSlots() {
        boolean changed = false;
        for (int slot = 0; slot < outputBuffer.size(); slot++) {
            final var stack = outputBuffer.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            final var remainder = ContainerUtils.insertItemStacked(outputContainer, stack, false);
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

    private boolean hasBufferedOutputs() {
        for (final var stack : outputBuffer) {
            if (!stack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void recordSale(ServerLevel level, RecipeHolder<ShippingBinRecipe> recipeHolder, long amount) {
        final Identifier recipeId = recipeHolder.id().identifier();
        ShippingBinSalesData.get(level).recordSale(level, level.getChunk(worldPosition).getPos(), recipeId, amount);
    }

}
