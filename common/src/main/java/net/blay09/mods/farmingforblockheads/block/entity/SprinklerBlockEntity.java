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
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SprinklerBlockEntity extends BlockEntity {

    private static final int ACTIVE_DURATION = 10 * 20;
    private static final int COOLDOWN_DURATION = 60 * 20;
    private static final int CYCLE_DURATION = ACTIVE_DURATION + COOLDOWN_DURATION;
    public static final int RANGE = 2;

    private int ticksPassed;
    private ItemStack head = ItemStack.EMPTY;

    private enum SprinkleMode {
        WATER,
        LAVA,
        HONEY,
        SLIME,
        SNOW
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
            if ((ticksPassed + 1) % 10 == 0) {
                applyEntityEffects(sprinkleMode);
            }
            if (sprinkleMode == SprinkleMode.WATER && (ticksPassed + 1) % 20 == 0 && level instanceof ServerLevel serverLevel) {
                hydrateFarmland(level, serverLevel);
            } else if ((ticksPassed + 1) % 40 == 0) {
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
            case SNOW -> FarmingForBlockheadsRules.SNOW_SPRINKLER_ENTITY_EFFECTS.get(this);
            case WATER -> FarmingForBlockheadsRules.WATER_SPRINKLER_ENTITY_EFFECTS.get(this);
        }
    }

    private void applySpecialBlockEffects(Level level, SprinkleMode sprinkleMode) {
        switch (sprinkleMode) {
            case WATER -> extinguishFires(level);
            case LAVA -> meltAndIgnite(level);
            case SNOW -> {
                extinguishFires(level);
                freezeWaterAndCreateSnow(level);
            }
            case HONEY, SLIME -> {
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
        for (final var entity : getEntitiesInRange(Entity.class)) {
            entity.igniteForSeconds(4);
        }
        return Either.left(true);
    }

    public Either<Object, ?> extinguishEntities() {
        for (final var entity : getEntitiesInRange(Entity.class)) {
            entity.extinguishFire();
        }
        return Either.left(true);
    }

    private void extinguishFires(Level level) {
        BlockPos.betweenClosed(worldPosition.offset(-RANGE, -1, -RANGE), worldPosition.offset(RANGE, 0, RANGE)).forEach(targetPos -> {
            final var state = level.getBlockState(targetPos);
            if (state.is(BlockTags.FIRE)) {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            } else if (state.is(BlockTags.CAMPFIRES) && state.getValueOrElse(BlockStateProperties.LIT, false)) {
                level.setBlock(targetPos, state.setValue(BlockStateProperties.LIT, false), Block.UPDATE_CLIENTS);
            }
        });
    }

    private void meltAndIgnite(Level level) {
        final boolean canMelt = FarmingForBlockheadsRules.LAVA_SPRINKLER_CAN_MELT.getOrDefault(this);
        final boolean canIgnite = FarmingForBlockheadsRules.LAVA_SPRINKLER_CAN_IGNITE.getOrDefault(this);
        if (!canMelt && !canIgnite) {
            return;
        }

        final RandomSource random = level.getRandom();
        BlockPos.betweenClosed(worldPosition.offset(-RANGE, -1, -RANGE), worldPosition.offset(RANGE, 0, RANGE)).forEach(targetPos -> {
            if (random.nextFloat() < 0.35f) {
                final var state = level.getBlockState(targetPos);
                final var targetContext = targetContext(level, targetPos, state);
                if (canMelt && (state.is(ModBlockTags.MELTS_INTO_AIR) || state.is(ModBlockTags.MELTS_INTO_WATER)) && FarmingForBlockheadsRules.LAVA_SPRINKLER_CAN_MELT_AT.getOrDefault(targetContext)) {
                    meltBlock(level, targetPos, state);
                }

                if (canIgnite && state.ignitedByLava() && FarmingForBlockheadsRules.LAVA_SPRINKLER_CAN_IGNITE_AT.getOrDefault(targetContext)) {
                    igniteFlammableBlock(level, targetPos, random);
                }
            }
        });
    }

    private void meltBlock(Level level, BlockPos targetPos, BlockState state) {
        if (state.is(ModBlockTags.MELTS_INTO_AIR)) {
            if (!state.hasProperty(BlockStateProperties.LAYERS)) {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                return;
            }

            final int layers = state.getValue(BlockStateProperties.LAYERS);
            if (layers > 1) {
                level.setBlock(targetPos, state.setValue(BlockStateProperties.LAYERS, layers - 1), Block.UPDATE_CLIENTS);
            } else {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        } else if (state.is(ModBlockTags.MELTS_INTO_WATER)) {
            level.setBlock(targetPos, Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void igniteFlammableBlock(Level level, BlockPos targetPos, RandomSource random) {
        for (final Direction direction : Direction.allShuffled(random)) {
            final var firePos = targetPos.relative(direction);
            if (!level.isEmptyBlock(firePos)) {
                continue;
            }

            final var fireState = BaseFireBlock.getState(level, firePos);
            if (fireState.canSurvive(level, firePos)) {
                level.setBlock(firePos, fireState, Block.UPDATE_CLIENTS);
                break;
            }
        }
    }

    public Either<Object, ?> removePoison() {
        for (final LivingEntity entity : getEntitiesInRange(LivingEntity.class)) {
            entity.removeEffect(MobEffects.POISON);
        }
        return Either.left(true);
    }

    public Either<Object, ?> slowEntities() {
        for (final LivingEntity entity : getEntitiesInRange(LivingEntity.class)) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 6 * 20, 3));
        }
        return Either.left(true);
    }

    public Either<Object, ?> freezeEntities() {
        for (final var entity : getEntitiesInRange(Entity.class)) {
            entity.extinguishFire();
            if (entity.canFreeze()) {
                entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), entity.getTicksFrozen() + 2 * 20));
            }
        }
        return Either.left(true);
    }

    public Either<Object, ?> nauseateEntities() {
        for (final LivingEntity entity : getEntitiesInRange(LivingEntity.class)) {
            entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 4 * 20));
        }
        return Either.left(true);
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
            if (random.nextFloat() < 0.35f) {
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
            }
        });
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
        if (isActive(ticksPassed) && (ticksPassed + 1) % 2 == 0) {
            final RandomSource random = level.getRandom();
            final SprinkleMode sprinkleMode = getSprinkleMode(level);
            final SimpleParticleType fallingParticle = getFallingParticle(sprinkleMode);
            final SimpleParticleType landingParticle = getLandingParticle(sprinkleMode);
            final double rotation = Math.toRadians(getActiveGameTime(ticksPassed) * 20d);
            final double directionX = Math.cos(rotation);
            final double directionZ = Math.sin(rotation);
            final double offsetX = directionX * (5.5 / 16d + 2d / 16d);
            final double offsetZ = directionZ * (5.5 / 16d + 2d / 16d);
            final double centerX = worldPosition.getX() + 0.5;
            final double y = worldPosition.getY() + 12d / 16d;
            final double centerZ = worldPosition.getZ() + 0.5;
            final var particleSpeed = 0.08d;
            final var particleDistance = 2d;
            for (int direction = -1; direction <= 1; direction += 2) {
                final double motionX = directionX * particleSpeed * direction;
                final double motionZ = directionZ * particleSpeed * direction;
                final double startX = centerX + offsetX * direction;
                final double startZ = centerZ + offsetZ * direction;
                final double endX = centerX + directionX * particleDistance * direction;
                final double endY = worldPosition.getY() + 1.5d / 16d;
                final double endZ = centerZ + directionZ * particleDistance * direction;
                final var particleCount = 7;
                for (int i = 0; i < particleCount; i++) {
                    final double progress = Math.min(1d, (i + random.nextDouble() * 0.2) / (particleCount - 1));
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
                    if (i == particleCount - 1 && random.nextBoolean()) {
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
        };
    }

    private SimpleParticleType getLandingParticle(SprinkleMode sprinkleMode) {
        return switch (sprinkleMode) {
            case WATER -> ParticleTypes.SPLASH;
            case LAVA -> ParticleTypes.LANDING_LAVA;
            case HONEY -> ParticleTypes.LANDING_HONEY;
            case SLIME -> ParticleTypes.ITEM_SLIME;
            case SNOW -> ParticleTypes.SNOWFLAKE;
        };
    }
}
