package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.mixin.RecipeManagerAccessor;
import net.blay09.mods.balm.world.*;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.ShippingBinSalesData;
import net.blay09.mods.farmingforblockheads.entity.ModEntities;
import net.blay09.mods.farmingforblockheads.entity.ShippingBalloonEntity;
import net.blay09.mods.farmingforblockheads.loot.ModLootContextParams;
import net.blay09.mods.farmingforblockheads.menu.ShippingBinMenu;
import net.blay09.mods.farmingforblockheads.recipe.FarmingForBlockheadsRules;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.recipe.ShippingBinRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShippingBinBlockEntity extends BlockEntity implements BalmContainerProvider, BalmMenuProvider<Unit> {

    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, FarmingForBlockheads.id("gameplay/shipping_bin"));

    public static final int INPUT_SLOTS = 6;
    public static final int OUTPUT_SLOTS = 6;
    public static final int CONTAINER_SIZE = INPUT_SLOTS + OUTPUT_SLOTS;
    public static final int DISPLAYED_ITEM_SLOTS = 12;
    public static final int DATA_SHIPMENT_VALUE = 0;
    public static final int DATA_SHIPMENT_CAPACITY = 1;
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
                case DATA_SHIPMENT_VALUE -> level instanceof ServerLevel serverLevel ? getShipmentValue(serverLevel) : 0;
                case DATA_SHIPMENT_CAPACITY -> getShipmentCapacity();
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
    private final NonNullList<ItemStack> displayedItems = NonNullList.withSize(DISPLAYED_ITEM_SLOTS, ItemStack.EMPTY);

    private boolean isDirty;
    private boolean processing;
    private int shipmentValue;

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
        displayedItems.clear();
        input.child("ItemHandler").ifPresent(it -> ContainerHelper.loadAllItems(it, container.getItems()));
        input.child("DisplayedItems").ifPresent(it -> ContainerHelper.loadAllItems(it, displayedItems));
        shipmentValue = input.getIntOr("ShipmentValue", input.getIntOr("FillLevel", 0));
        for (final var stack : input.listOrEmpty("OutputBuffer", ItemStack.CODEC)) {
            if (!stack.isEmpty()) {
                outputBuffer.add(stack);
            }
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output.child("ItemHandler"), container.getItems());
        ContainerHelper.saveAllItems(output.child("DisplayedItems"), displayedItems);
        output.putInt("ShipmentValue", shipmentValue);
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

    public int getShipmentValue() {
        return shipmentValue;
    }

    public int getShipmentCapacity() {
        return FarmingForBlockheadsRules.getShippingBinCapacity(this);
    }

    public List<ItemStack> getDisplayedItems() {
        return displayedItems;
    }

    public static int getDisplayedItemCount(int shipmentValue, int shipmentCapacity) {
        return shipmentValue == 0 || shipmentCapacity <= 0 ? 0 : Math.min(DISPLAYED_ITEM_SLOTS, (int) Math.ceilDiv((long) shipmentValue * DISPLAYED_ITEM_SLOTS, shipmentCapacity));
    }

    private void tryProcessSales(ServerLevel level) {
        if (processing) {
            return;
        }

        processing = true;
        try {
            shipmentValue = getShipmentValue(level);
            boolean changed = updateDisplayedItems();
            changed |= moveBufferedOutputsToOutputSlots();
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
                shipmentValue = getShipmentValue(level);
                changed |= updateDisplayedItems();
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

    private boolean updateDisplayedItems() {
        final var displayCount = getDisplayedItemCount(shipmentValue, getShipmentCapacity());
        boolean changed = false;

        for (int i = 0; i < displayedItems.size(); i++) {
            final var displayedItem = displayedItems.get(i);
            if (i >= displayCount) {
                if (!displayedItem.isEmpty()) {
                    displayedItems.set(i, ItemStack.EMPTY);
                    changed = true;
                }
                continue;
            }

            if (displayedItem.isEmpty() || !hasInputStack(displayedItem)) {
                final var itemStack = pickDisplayedItem(i);
                if (!ItemStack.isSameItemSameComponents(displayedItem, itemStack)) {
                    displayedItems.set(i, itemStack);
                    changed = true;
                }
            }
        }

        return changed;
    }

    private boolean hasInputStack(ItemStack itemStack) {
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            if (ItemStack.isSameItemSameComponents(container.getItem(slot), itemStack)) {
                return true;
            }
        }

        return false;
    }

    private ItemStack pickDisplayedItem(int displaySlot) {
        final var startSlot = Math.floorMod(Long.hashCode(worldPosition.asLong() + displaySlot), INPUT_SLOTS);
        for (int offset = 0; offset < INPUT_SLOTS; offset++) {
            final var stack = container.getItem((startSlot + offset) % INPUT_SLOTS);
            if (!stack.isEmpty()) {
                final var itemStack = stack.copy();
                itemStack.setCount(1);
                return itemStack;
            }
        }

        return ItemStack.EMPTY;
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

    private int getShipmentValue(ServerLevel level) {
        int shipmentValue = 0;
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            final var stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            final var recipeHolder = findRecipe(level, stack);
            if (recipeHolder != null) {
                shipmentValue += stack.getCount() * recipeHolder.value().resolveValue(this, stack, recipeHolder.id().identifier());
            }
        }

        return shipmentValue;
    }

    private boolean processSaleCycle(ServerLevel level) {
        int value = 0;

        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            final var stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            final var recipeHolder = findRecipe(level, stack);
            if (recipeHolder == null) {
                continue;
            }

            value += recipeHolder.value().resolveValue(this, stack, recipeHolder.id().identifier()) * stack.getCount();
        }

        if (value < getShipmentCapacity()) {
            return false;
        }

        final var soldItems = new ArrayList<ItemStack>();
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            final var stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            final var recipeHolder = findRecipe(level, stack);
            if (recipeHolder == null) {
                continue;
            }

            final var removedStack = container.removeItem(slot, stack.getCount());
            if (!removedStack.isEmpty()) {
                soldItems.add(removedStack);
                recordSale(level, recipeHolder, removedStack.getCount());
            }
        }

        rollSaleOutputs(level, soldItems, value);
        spawnShippingBalloon(level);
        return true;
    }

    private void rollSaleOutputs(ServerLevel level, List<ItemStack> shippedItems, int shipmentValue) {
        final var lootTable = level.getServer().reloadableRegistries().getLootTable(LOOT_TABLE);
        final var lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(ModLootContextParams.SHIPMENT_VALUE, shipmentValue)
                .withParameter(ModLootContextParams.SHIPMENT_ITEMS, List.copyOf(shippedItems))
                .create(ModLootContextParams.SHIPPING_BIN_CONTEXT);
        lootTable.getRandomItems(lootParams, itemStack -> {
            final var remainder = ContainerUtils.insertItemStacked(outputContainer, itemStack, false);
            if (!remainder.isEmpty()) {
                outputBuffer.add(remainder.copy());
            }
        });
    }

    private void spawnShippingBalloon(ServerLevel level) {
        final var balloon = new ShippingBalloonEntity(ModEntities.shippingBalloon.value(), level);
        balloon.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5);
        level.addFreshEntity(balloon);
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
