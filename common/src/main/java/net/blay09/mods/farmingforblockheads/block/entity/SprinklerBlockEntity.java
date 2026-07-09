package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.blay09.mods.farmingforblockheads.HydratedFarmlandData;
import net.blay09.mods.farmingforblockheads.block.SprinklerBlock;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class SprinklerBlockEntity extends BlockEntity {

    private static final int HYDRATION_INTERVAL = 20;
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

    private int ticksPassed;
    private ItemStack head = ItemStack.EMPTY;

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
        if (isActive(ticksPassed) && (ticksPassed + 1) % HYDRATION_INTERVAL == 0 && level instanceof ServerLevel serverLevel) {
            if (sprinklesLava(level)) {
                igniteEntities(level);
                ticksPassed = (ticksPassed + 1) % CYCLE_DURATION;
                return;
            }

            final var hydratedFarmlandData = HydratedFarmlandData.get(serverLevel);
            BlockPos.betweenClosed(worldPosition.offset(-RANGE, -1, -RANGE), worldPosition.offset(RANGE, 0, RANGE)).forEach(targetPos -> {
                final var state = level.getBlockState(targetPos);
                if (state.getBlock() instanceof FarmlandBlock) {
                    hydratedFarmlandData.hydrate(serverLevel, targetPos.immutable());
                    level.setBlock(targetPos, state.setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
                }
            });
        }
        ticksPassed = (ticksPassed + 1) % CYCLE_DURATION;
    }

    private boolean sprinklesLava(Level level) {
        return level.getBlockState(worldPosition.below()).is(ModBlockTags.LAVA_SPRINKLER_BASE);
    }

    private void igniteEntities(Level level) {
        final var aabb = new AABB(worldPosition.getX() - RANGE,
                worldPosition.getY() - 1,
                worldPosition.getZ() - RANGE,
                worldPosition.getX() + RANGE + 1,
                worldPosition.getY() + 1,
                worldPosition.getZ() + RANGE + 1);
        for (final Entity entity : level.getEntitiesOfClass(Entity.class, aabb, Entity::isAlive)) {
            entity.igniteForSeconds(LAVA_FIRE_SECONDS);
        }
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
            final boolean sprinklesLava = sprinklesLava(level);
            final SimpleParticleType fallingParticle = sprinklesLava ? ParticleTypes.FALLING_LAVA : ParticleTypes.FALLING_WATER;
            final SimpleParticleType landingParticle = sprinklesLava ? ParticleTypes.LANDING_LAVA : ParticleTypes.SPLASH;
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
}
