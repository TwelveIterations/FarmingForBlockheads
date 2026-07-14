package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.blay09.mods.farmingforblockheads.block.RabbitTrapBlock;
import net.blay09.mods.farmingforblockheads.mixin.LivingEntityAccessor;
import net.blay09.mods.farmingforblockheads.tag.ModEntityTypeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class RabbitTrapBlockEntity extends BlockEntity {

    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, id("gameplay/rabbit_trap"));

    private static final int TICK_INTERVAL = 10;
    private static final int UNATTENDED_ROLL_INTERVAL = 1200;
    private static final int OFFLINE_CAPTURE_FULL_CHANCE_TICKS = 24000;
    private static final double UNATTENDED_BABY_CHANCE = 0.1;
    private static final double ATTRACT_RANGE = 8.0;
    private static final double CAPTURE_RANGE = 0.45;

    private @Nullable CompoundTag capturedEntity;
    private final List<ItemStack> caughtItems = new ArrayList<>();
    private long caughtGameTime = -1;
    private long setupGameTime = -1;
    private long lastGameTime = -1;
    private int ticksSinceMarkChanged;
    private int unattendedTicks;
    private int damage;
    private int ticksSinceRoll;
    private boolean needsClientSync;

    public RabbitTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.rabbitTrap.value(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RabbitTrapBlockEntity blockEntity) {
        blockEntity.serverTick(level, pos);
    }

    public void serverTick(Level level, BlockPos pos) {
        ticksSinceRoll++;
        if (ticksSinceRoll >= TICK_INTERVAL) {
            ticksSinceRoll = 0;
            if (!hasCatch()) {
                final var gameTime = level.getGameTime();
                final var elapsedTicks = lastGameTime >= 0 ? Math.max(0, gameTime - lastGameTime) : 0;
                if (elapsedTicks >= UNATTENDED_ROLL_INTERVAL) {
                    tryCaptureUnattended(level, elapsedTicks);
                }

                if (!hasCatch()) {
                    if (hasNearbyPlayer(level, pos)) {
                        unattendedTicks = 0;
                        attractAndCapture(level, pos);
                    } else {
                        final var previousUnattendedTicks = unattendedTicks;
                        unattendedTicks = Math.min(OFFLINE_CAPTURE_FULL_CHANCE_TICKS, unattendedTicks + TICK_INTERVAL);
                        if (unattendedTicks / UNATTENDED_ROLL_INTERVAL > previousUnattendedTicks / UNATTENDED_ROLL_INTERVAL) {
                            tryCaptureUnattended(level, unattendedTicks);
                        }
                    }
                }
            }

            lastGameTime = level.getGameTime();
        }

        // We periodically mark as changed so that lastGameTime is saved
        ticksSinceMarkChanged += TICK_INTERVAL;
        if (ticksSinceMarkChanged >= 1200) {
            ticksSinceMarkChanged = 0;
            setChanged();
        }

        if (needsClientSync) {
            BalmBlockEntityUtils.sync(this);
            needsClientSync = false;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        capturedEntity = input.read("CapturedEntity", CompoundTag.CODEC).map(CompoundTag::copy).orElse(null);
        caughtItems.clear();
        for (final var stack : input.listOrEmpty("CaughtItems", ItemStack.CODEC)) {
            if (!stack.isEmpty()) {
                caughtItems.add(stack);
            }
        }
        caughtGameTime = input.getLongOr("CaughtGameTime", hasCatch() ? 0 : -1);
        setupGameTime = input.getLongOr("SetupGameTime", -1);
        lastGameTime = input.getLongOr("LastGameTime", -1);
        unattendedTicks = input.getIntOr("UnattendedTicks", 0);
        damage = input.getIntOr("Damage", 0);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        output.putLong("LastGameTime", lastGameTime);
        output.putLong("SetupGameTime", setupGameTime);
        output.putInt("UnattendedTicks", unattendedTicks);
        output.putInt("Damage", damage);

        if (capturedEntity != null) {
            output.store("CapturedEntity", CompoundTag.CODEC, capturedEntity);
            output.putLong("CaughtGameTime", caughtGameTime);
        }

        if (!caughtItems.isEmpty()) {
            final var caughtItemsList = output.list("CaughtItems", ItemStack.CODEC);
            for (final var stack : caughtItems) {
                caughtItemsList.add(stack);
            }
            output.putLong("CaughtGameTime", caughtGameTime);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return BalmBlockEntityUtils.createUpdateTag(registries, this::saveAdditional);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return BalmBlockEntityUtils.createUpdatePacket(this);
    }

    private void attractAndCapture(Level level, BlockPos pos) {
        final var attractBox = new AABB(pos).inflate(ATTRACT_RANGE, 2.0, ATTRACT_RANGE);
        final var candidates = level.getEntitiesOfClass(Animal.class, attractBox, this::canCatch);
        if (candidates.isEmpty()) {
            return;
        }

        final var trapCenter = Vec3.atCenterOf(pos);
        candidates.sort(Comparator.comparingDouble(it -> it.distanceToSqr(trapCenter)));

        final var captureBox = new AABB(pos).inflate(CAPTURE_RANGE, 0.35, CAPTURE_RANGE);
        for (final var animal : candidates) {
            if (animal.getBoundingBox().intersects(captureBox)) {
                capture(animal);
                return;
            }
        }

        final var nearest = candidates.getFirst();
        if (!nearest.isNoAi()) {
            nearest.getNavigation().moveTo(trapCenter.x, pos.getY(), trapCenter.z, 1);
        }
    }

    private boolean hasNearbyPlayer(Level level, BlockPos pos) {
        final var center = Vec3.atCenterOf(pos);
        return level.hasNearbyAlivePlayer(center.x, center.y, center.z, 64);
    }

    private void tryCaptureUnattended(Level level, long elapsedTicks) {
        if (!(level instanceof ServerLevel serverLevel) || elapsedTicks <= 0 || level.getRandom().nextDouble() >= getUnattendedCaptureChance(elapsedTicks)) {
            return;
        }

        if (tryCatchItems(serverLevel)) {
            triggerTrap(serverLevel);
            return;
        }

        final var candidates = new ArrayList<EntityType<?>>();
        BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(ModEntityTypeTags.RABBIT_TRAP_SPAWNABLE).forEach(holder -> candidates.add(holder.value()));
        if (candidates.isEmpty()) {
            return;
        }

        while (!candidates.isEmpty()) {
            final var candidateIndex = level.getRandom().nextInt(candidates.size());
            final var entityType = candidates.remove(candidateIndex);
            final var entity = entityType.create(level, EntitySpawnReason.TRIGGERED);
            if (entity instanceof Animal animal) {
                if (level.getRandom().nextDouble() < UNATTENDED_BABY_CHANCE) {
                    animal.setBaby(true);
                } else {
                    animal.setAge(0);
                }
                if (saveCatch(serverLevel, animal)) {
                    triggerTrap(serverLevel);
                    return;
                }
            }
        }
    }

    private double getUnattendedCaptureChance(long elapsedTicks) {
        return Math.min(0.95, (double) elapsedTicks / OFFLINE_CAPTURE_FULL_CHANCE_TICKS);
    }

    private boolean canCatch(Animal animal) {
        return animal.isAlive() && animal.is(ModEntityTypeTags.RABBIT_TRAP_CAPTURABLE);
    }

    private void capture(Animal animal) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!saveCatch(serverLevel, animal)) {
            return;
        }

        ((LivingEntityAccessor) animal).callPlayHurtSound(animal.damageSources().generic());
        animal.discard();
        triggerTrap(serverLevel);
    }

    private boolean saveCatch(ServerLevel serverLevel, Animal animal) {
        final var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, serverLevel.registryAccess());
        if (!animal.save(output)) {
            return false;
        }

        capturedEntity = output.buildResult();
        capturedEntity.remove(Entity.TAG_UUID);
        caughtGameTime = serverLevel.getGameTime();
        unattendedTicks = 0;
        return true;
    }

    private boolean tryCatchItems(ServerLevel serverLevel) {
        final var lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(LOOT_TABLE);
        final var lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .create(LootContextParamSets.CHEST);
        final var loot = lootTable.getRandomItems(lootParams);
        if (loot.isEmpty()) {
            return false;
        }

        capturedEntity = null;
        caughtItems.clear();
        for (final var itemStack : loot) {
            if (!itemStack.isEmpty()) {
                caughtItems.add(itemStack.copy());
            }
        }

        caughtGameTime = serverLevel.getGameTime();
        if (!caughtItems.isEmpty()) {
            unattendedTicks = 0;
            return true;
        }
        return false;
    }

    private void triggerTrap(ServerLevel serverLevel) {
        serverLevel.setBlock(worldPosition, getBlockState().setValue(RabbitTrapBlock.TRIGGERED, true), Block.UPDATE_ALL);
        serverLevel.sendParticles(ParticleTypes.POOF, worldPosition.getX() + 0.5, worldPosition.getY() + 0.35, worldPosition.getZ() + 0.5, 8, 0.2, 0.1, 0.2, 0.02);
        serverLevel.playSound(null, worldPosition, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.8f, 0.8f);
        setChanged();
        needsClientSync = true;
    }

    public void releaseCatch() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!caughtItems.isEmpty()) {
            final var facing = getBlockState().getValue(RabbitTrapBlock.FACING);
            final var offsetX = facing.getStepX() * 0.8;
            final var offsetZ = facing.getStepZ() * 0.8;
            final var origin = new Vec3(worldPosition.getX() + 0.5 + offsetX, worldPosition.getY() + 0.3, worldPosition.getZ() + 0.5 + offsetZ);
            for (final var itemStack : caughtItems) {
                final var itemEntity = new ItemEntity(serverLevel, origin.x, origin.y, origin.z, itemStack.copy());
                itemEntity.setDeltaMovement(0f, 0.2f, 0f);
                serverLevel.addFreshEntity(itemEntity);
            }
            serverLevel.sendParticles(ParticleTypes.POOF, origin.x, origin.y, origin.z, 12, 0.25, 0.25, 0.25, 0.03);
            clearCatch();
            return;
        }

        if (capturedEntity == null) {
            return;
        }

        final var entityTag = capturedEntity.copy();
        entityTag.remove(Entity.TAG_UUID);
        final var input = TagValueInput.create(ProblemReporter.DISCARDING, serverLevel.registryAccess(), entityTag);
        EntityType.create(input, level, new EntitySpawnRequest(EntitySpawnReason.TRIGGERED, true)).ifPresent(entity -> {
            final var facing = getBlockState().getValue(RabbitTrapBlock.FACING);
            final var offsetX = facing.getStepX() * 0.8;
            final var offsetZ = facing.getStepZ() * 0.8;
            entity.setPos(worldPosition.getX() + 0.5 + offsetX, worldPosition.getY() + 0.1, worldPosition.getZ() + 0.5 + offsetZ);
            entity.setYRot(facing.toYRot());
            entity.setXRot(0f);
            serverLevel.addFreshEntity(entity);
            serverLevel.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 12, 0.25, 0.25, 0.25, 0.03);
            clearCatch();
        });
    }

    private void clearCatch() {
        capturedEntity = null;
        caughtItems.clear();
        caughtGameTime = -1;
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.8f, 1.0f);
        }
        setChanged();
        needsClientSync = true;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        releaseCatch();
    }

    public void setDamage(int damage) {
        this.damage = Math.max(0, damage);
        setChanged();
    }

    public void startSetupAnimation() {
        if (level == null) {
            return;
        }

        setupGameTime = level.getGameTime();
        setChanged();
        needsClientSync = true;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        final var damage = this.damage + (getBlockState().getValue(RabbitTrapBlock.TRIGGERED) ? 1 : 0);
        if (damage > 0) {
            builder.set(DataComponents.MAX_DAMAGE, 8);
            builder.set(DataComponents.DAMAGE, damage);
        }
    }

    public boolean hasCatch() {
        return capturedEntity != null || !caughtItems.isEmpty();
    }

    public long getCaughtGameTime() {
        return caughtGameTime;
    }

    public long getSetupGameTime() {
        return setupGameTime;
    }

}
