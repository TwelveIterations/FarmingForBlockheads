package net.blay09.mods.farmingforblockheads.block.entity;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.blay09.mods.farmingforblockheads.HydratedFarmlandData;
import net.blay09.mods.farmingforblockheads.block.SprinklerBlock;
import net.blay09.mods.farmingforblockheads.recipe.FarmingForBlockheadsRules;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SprinklerBlockEntity extends BlockEntity {

    private static final int ENTITY_EFFECT_INTERVAL = 10;
    private static final int WATER_HYDRATION_INTERVAL = 20;
    private static final int SPECIAL_BLOCK_EFFECT_INTERVAL = 40;
    private static final float SPECIAL_BLOCK_EFFECT_CHANCE = 0.35f;
    private static final int PARTICLE_INTERVAL = 2;
    private static final int ACTIVE_DURATION = 10 * 20;
    private static final int BREAK_DURATION = 60 * 20;
    private static final int CYCLE_DURATION = ACTIVE_DURATION + BREAK_DURATION;
    public static final int RANGE = 2;
    private static final double PIPE_END_OFFSET = 5.5 / 16d;
    private static final double NOZZLE_CLEARANCE = 2d / 16d;
    private static final double PIPE_HEIGHT = 12d / 16d;
    private static final double STREAM_DISTANCE = 2d;
    private static final double STREAM_END_HEIGHT = 1.5d / 16d;
    private static final int STREAM_PARTICLES = 7;
    private static final double PARTICLE_SPEED = 0.08d;
    private static final double ROTATION_SPEED = 20d;
    private static final int LAVA_FIRE_SECONDS = 4;
    private static final int SLIME_SLOWNESS_TICKS = 6 * 20;
    private static final int SULFUR_NAUSEA_TICKS = 4 * 20;

    private int ticksPassed;
    private ItemStack head = ItemStack.EMPTY;

    private enum SprinkleMode {
        WATER,
        LAVA,
        HONEY,
        SLIME,
        SNOW,
        SULFUR,
        SCULK
    }

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.sprinkler.value(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity blockEntity) {
        blockEntity.serverTick(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity blockEntity) {
        blockEntity.clientTick(level);
    }

    public static boolean isActive(long ticksPassed) {
        return ticksPassed % CYCLE_DURATION < ACTIVE_DURATION;
    }

    public static long getActiveGameTime(long ticksPassed) {
        final long cycles = ticksPassed / CYCLE_DURATION;
        final long cycleTick = ticksPassed % CYCLE_DURATION;
        return cycles * ACTIVE_DURATION + Math.min(cycleTick, ACTIVE_DURATION);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        head = input.read("Head", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        ticksPassed = input.getIntOr("TicksPassed", input.getIntOr("CycleAge", 0));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!head.isEmpty()) {
            output.store("Head", ItemStack.OPTIONAL_CODEC, head);
        }
        output.putInt("TicksPassed", ticksPassed);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return BalmBlockEntityUtils.createUpdateTag(registries, this::saveAdditional);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return BalmBlockEntityUtils.createUpdatePacket(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && !head.isEmpty()) {
            Block.popResource(level, pos, head);
            head = ItemStack.EMPTY;
        }
    }

    public ItemStack getHead() {
        return head;
    }

    public boolean hasHead() {
        return !head.isEmpty();
    }

    public int getTicksPassed() {
        return ticksPassed;
    }

    public void setHead(ItemStack head) {
        this.head = head.copyWithCount(1);
        setChanged();
        if (level != null && !level.isClientSide()) {
            updateLitState(level);
            BalmBlockEntityUtils.sync(this);
        }
    }

    public ItemStack removeHead() {
        final var result = head;
        head = ItemStack.EMPTY;
        setChanged();
        if (level != null && !level.isClientSide()) {
            updateLitState(level);
            BalmBlockEntityUtils.sync(this);
        }
        return result;
    }

    private void serverTick(Level level) {
        updateLitState(level);
        if (isActive(ticksPassed)) {
            final var sprinkleMode = getSprinkleMode(level);
            if ((ticksPassed + 1) % ENTITY_EFFECT_INTERVAL == 0) {
                applyEntityEffects(sprinkleMode);
            }
            if (sprinkleMode == SprinkleMode.WATER && (ticksPassed + 1) % WATER_HYDRATION_INTERVAL == 0 && level instanceof ServerLevel serverLevel) {
                hydrateFarmland(level, serverLevel);
            } else if ((ticksPassed + 1) % SPECIAL_BLOCK_EFFECT_INTERVAL == 0) {
                applySpecialBlockEffects(level, sprinkleMode);
            }
        }
        ticksPassed = (ticksPassed + 1) % CYCLE_DURATION;
    }

    private void applyEntityEffects(SprinkleMode sprinkleMode) {
        switch (sprinkleMode) {
            case LAVA -> FarmingForBlockheadsRules.LAVA_SPRINKLER_ENTITY_EFFECTS.get(this);
            case HONEY -> FarmingForBlockheadsRules.HONEY_SPRINKLER_ENTITY_EFFECTS.get(this);
            case SLIME -> FarmingForBlockheadsRules.SLIME_SPRINKLER_ENTITY_EFFECTS.get(this);
            case SULFUR -> FarmingForBlockheadsRules.SULFUR_SPRINKLER_ENTITY_EFFECTS.get(this);
            case WATER, SNOW, SCULK -> {
            }
        }
    }

    private void applySpecialBlockEffects(Level level, SprinkleMode sprinkleMode) {
        switch (sprinkleMode) {
            case LAVA -> meltSnowAndIce(level);
            case SNOW -> freezeWaterAndCreateSnow(level);
            case SCULK -> emitVibrations(level);
            case WATER, HONEY, SLIME, SULFUR -> {
            }
        }
    }

    private SprinkleMode getSprinkleMode(Level level) {
        final var stateBelow = level.getBlockState(worldPosition.below());
        if (stateBelow.is(ModBlockTags.LAVA_SPRINKLER_BASE)) {
            return SprinkleMode.LAVA;
        } else if (stateBelow.is(ModBlockTags.HONEY_SPRINKLER_BASE)) {
            return SprinkleMode.HONEY;
        } else if (stateBelow.is(ModBlockTags.SLIME_SPRINKLER_BASE)) {
            return SprinkleMode.SLIME;
        } else if (stateBelow.is(ModBlockTags.SNOW_SPRINKLER_BASE)) {
            return SprinkleMode.SNOW;
        } else if (stateBelow.is(ModBlockTags.SULFUR_SPRINKLER_BASE)) {
            return SprinkleMode.SULFUR;
        } else if (stateBelow.is(ModBlockTags.SCULK_SPRINKLER_BASE)) {
            return SprinkleMode.SCULK;
        }

        return SprinkleMode.WATER;
    }

    private void hydrateFarmland(Level level, ServerLevel serverLevel) {
        final var hydratedFarmlandData = HydratedFarmlandData.get(serverLevel);
        BlockPos.betweenClosed(worldPosition.offset(-RANGE, -1, -RANGE), worldPosition.offset(RANGE, 0, RANGE)).forEach(targetPos -> {
            final var state = level.getBlockState(targetPos);
            if (state.getBlock() instanceof FarmlandBlock) {
                hydratedFarmlandData.hydrate(serverLevel, targetPos.immutable());
                level.setBlock(targetPos, state.setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
            }
        });
    }

    public Either<Object, ?> igniteEntities() {
        for (final Entity entity : getEntitiesInRange(Entity.class)) {
            entity.igniteForSeconds(LAVA_FIRE_SECONDS);
        }
        return Either.left(true);
    }

    private void meltSnowAndIce(Level level) {
        if (!FarmingForBlockheadsRules.LAVA_SPRINKLER_CAN_MELT.getOrDefault(this)) {
            return;
        }

        final RandomSource random = level.getRandom();
        BlockPos.betweenClosed(worldPosition.offset(-RANGE, -1, -RANGE), worldPosition.offset(RANGE, 0, RANGE)).forEach(targetPos -> {
            if (random.nextFloat() >= SPECIAL_BLOCK_EFFECT_CHANCE) {
                return;
            }

            final var state = level.getBlockState(targetPos);
            if (!FarmingForBlockheadsRules.LAVA_SPRINKLER_CAN_MELT_AT.getOrDefault(targetContext(level, targetPos, state))) {
                return;
            }

            if (state.is(Blocks.SNOW)) {
                final int layers = state.getValue(SnowLayerBlock.LAYERS);
                if (layers > 1) {
                    level.setBlock(targetPos, state.setValue(SnowLayerBlock.LAYERS, layers - 1), Block.UPDATE_CLIENTS);
                } else {
                    level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            } else if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
                level.setBlock(targetPos, Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
            } else if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        });
    }

    public Either<Object, ?> removePoison() {
        for (final LivingEntity entity : getEntitiesInRange(LivingEntity.class)) {
            entity.removeEffect(MobEffects.POISON);
        }
        return Either.left(true);
    }

    public Either<Object, ?> slowEntities() {
        for (final LivingEntity entity : getEntitiesInRange(LivingEntity.class)) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLIME_SLOWNESS_TICKS, 3));
        }
        return Either.left(true);
    }

    public Either<Object, ?> nauseateEntities() {
        for (final LivingEntity entity : getEntitiesInRange(LivingEntity.class)) {
            entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, SULFUR_NAUSEA_TICKS));
        }
        return Either.left(true);
    }

    private void emitVibrations(Level level) {
        if (!FarmingForBlockheadsRules.SCULK_SPRINKLER_CAN_EMIT_VIBRATIONS.getOrDefault(this)) {
            return;
        }

        final RandomSource random = level.getRandom();
        BlockPos.betweenClosed(worldPosition.offset(-RANGE, -1, -RANGE), worldPosition.offset(RANGE, 0, RANGE)).forEach(targetPos -> {
            if (random.nextFloat() < SPECIAL_BLOCK_EFFECT_CHANCE
                    && FarmingForBlockheadsRules.SCULK_SPRINKLER_CAN_EMIT_VIBRATIONS_AT.getOrDefault(targetContext(level, targetPos))) {
                level.gameEvent(GameEvent.BLOCK_ACTIVATE, targetPos, GameEvent.Context.of(getBlockState()));
            }
        });
    }

    private <T extends Entity> List<T> getEntitiesInRange(Class<T> entityClass) {
        if (level == null) {
            return List.of();
        }

        final var aabb = new AABB(worldPosition.getX() - RANGE,
                worldPosition.getY() - 1,
                worldPosition.getZ() - RANGE,
                worldPosition.getX() + RANGE + 1,
                worldPosition.getY() + 1,
                worldPosition.getZ() + RANGE + 1);
        return level.getEntitiesOfClass(entityClass, aabb, Entity::isAlive);
    }

    private void freezeWaterAndCreateSnow(Level level) {
        final boolean canFreeze = FarmingForBlockheadsRules.SNOW_SPRINKLER_CAN_FREEZE.getOrDefault(this);
        final boolean canCreateSnow = FarmingForBlockheadsRules.SNOW_SPRINKLER_CAN_CREATE_SNOW.getOrDefault(this);
        if (!canFreeze && !canCreateSnow) {
            return;
        }

        final RandomSource random = level.getRandom();
        BlockPos.betweenClosed(worldPosition.offset(-RANGE, -1, -RANGE), worldPosition.offset(RANGE, -1, RANGE)).forEach(targetPos -> {
            if (random.nextFloat() >= SPECIAL_BLOCK_EFFECT_CHANCE) {
                return;
            }

            final var state = level.getBlockState(targetPos);
            if (canFreeze && state.is(Blocks.WATER) && FarmingForBlockheadsRules.SNOW_SPRINKLER_CAN_FREEZE_AT.getOrDefault(targetContext(level, targetPos, state))) {
                level.setBlock(targetPos, Blocks.ICE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }

            if (!canCreateSnow) {
                return;
            }

            final var snowPos = targetPos.above();
            final var snowState = level.getBlockState(snowPos);
            if (!FarmingForBlockheadsRules.SNOW_SPRINKLER_CAN_CREATE_SNOW_AT.getOrDefault(targetContext(level, snowPos, snowState))) {
                return;
            }

            final int maxLayers = getMaxSnowLayersForPosition(snowPos);
            if (snowState.is(Blocks.SNOW)) {
                final int layers = snowState.getValue(SnowLayerBlock.LAYERS);
                if (layers < maxLayers) {
                    level.setBlock(snowPos, snowState.setValue(SnowLayerBlock.LAYERS, layers + 1), Block.UPDATE_CLIENTS);
                }
            } else if (snowState.isAir()) {
                final var newSnowState = Blocks.SNOW.defaultBlockState();
                if (newSnowState.canSurvive(level, snowPos)) {
                    level.setBlock(snowPos, newSnowState, Block.UPDATE_CLIENTS);
                }
            }
        });
    }

    private ShogiContext targetContext(Level level, BlockPos targetPos) {
        return targetContext(level, targetPos, level.getBlockState(targetPos));
    }

    private ShogiContext targetContext(Level level, BlockPos targetPos, BlockState targetState) {
        return MutableShogiContext.of(this)
                .withLevel(level)
                .withBlockPos(targetPos)
                .withBlockState(targetState);
    }

    private int getMaxSnowLayersForPosition(BlockPos pos) {
        return Math.floorMod(pos.getX() * 31 + pos.getY() * 13 + pos.getZ() * 17, 4) + 1;
    }

    private void updateLitState(Level level) {
        final var state = getBlockState();
        if (!state.hasProperty(SprinklerBlock.LIT)) {
            return;
        }

        final boolean lit = headEmitsLight();
        if (state.getValue(SprinklerBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(SprinklerBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    private boolean headEmitsLight() {
        if (head.getItem() instanceof BlockItem blockItem) {
            var blockState = blockItem.getBlock().defaultBlockState();
            final var blockStateProperties = head.get(DataComponents.BLOCK_STATE);
            if (blockStateProperties != null) {
                blockState = blockStateProperties.apply(blockState);
            }

            return blockState.getLightEmission() > 0;
        }

        return false;
    }

    private void clientTick(Level level) {
        if (isActive(ticksPassed) && (ticksPassed + 1) % PARTICLE_INTERVAL == 0) {
            final RandomSource random = level.getRandom();
            final SprinkleMode sprinkleMode = getSprinkleMode(level);
            final SimpleParticleType fallingParticle = getFallingParticle(sprinkleMode);
            final SimpleParticleType landingParticle = getLandingParticle(sprinkleMode);
            final double rotation = Math.toRadians(getActiveGameTime(ticksPassed) * ROTATION_SPEED);
            final double directionX = Math.cos(rotation);
            final double directionZ = Math.sin(rotation);
            final double offsetX = directionX * (PIPE_END_OFFSET + NOZZLE_CLEARANCE);
            final double offsetZ = directionZ * (PIPE_END_OFFSET + NOZZLE_CLEARANCE);
            final double centerX = worldPosition.getX() + 0.5;
            final double y = worldPosition.getY() + PIPE_HEIGHT;
            final double centerZ = worldPosition.getZ() + 0.5;
            for (int direction = -1; direction <= 1; direction += 2) {
                final double motionX = directionX * PARTICLE_SPEED * direction;
                final double motionZ = directionZ * PARTICLE_SPEED * direction;
                final double startX = centerX + offsetX * direction;
                final double startZ = centerZ + offsetZ * direction;
                final double endX = centerX + directionX * STREAM_DISTANCE * direction;
                final double endY = worldPosition.getY() + STREAM_END_HEIGHT;
                final double endZ = centerZ + directionZ * STREAM_DISTANCE * direction;
                for (int i = 0; i < STREAM_PARTICLES; i++) {
                    final double progress = Math.min(1d, (i + random.nextDouble() * 0.2) / (STREAM_PARTICLES - 1));
                    final double particleX = startX + (endX - startX) * progress + (random.nextDouble() - 0.5) * 0.035;
                    final double particleY = y + (endY - y) * progress * progress + (random.nextDouble() - 0.5) * 0.025;
                    final double particleZ = startZ + (endZ - startZ) * progress + (random.nextDouble() - 0.5) * 0.035;
                    level.addParticle(fallingParticle,
                            particleX,
                            particleY,
                            particleZ,
                            motionX,
                            -0.02 - progress * 0.04,
                            motionZ);
                    if (i == STREAM_PARTICLES - 1 && random.nextBoolean()) {
                        level.addParticle(landingParticle,
                                particleX,
                                particleY,
                                particleZ,
                                motionX * 0.4,
                                0.01,
                                motionZ * 0.4);
                    }
                }
            }
        }
        ticksPassed = (ticksPassed + 1) % CYCLE_DURATION;
    }

    private SimpleParticleType getFallingParticle(SprinkleMode sprinkleMode) {
        return switch (sprinkleMode) {
            case WATER -> ParticleTypes.FALLING_WATER;
            case LAVA -> ParticleTypes.FALLING_LAVA;
            case HONEY -> ParticleTypes.FALLING_HONEY;
            case SLIME -> ParticleTypes.ITEM_SLIME;
            case SNOW -> ParticleTypes.SNOWFLAKE;
            case SULFUR -> ParticleTypes.NOXIOUS_GAS;
            case SCULK -> ParticleTypes.SCULK_CHARGE_POP;
        };
    }

    private SimpleParticleType getLandingParticle(SprinkleMode sprinkleMode) {
        return switch (sprinkleMode) {
            case WATER -> ParticleTypes.SPLASH;
            case LAVA -> ParticleTypes.LANDING_LAVA;
            case HONEY -> ParticleTypes.LANDING_HONEY;
            case SLIME -> ParticleTypes.ITEM_SLIME;
            case SNOW -> ParticleTypes.SNOWFLAKE;
            case SULFUR -> ParticleTypes.NOXIOUS_GAS_CLOUD;
            case SCULK -> ParticleTypes.SCULK_SOUL;
        };
    }
}
