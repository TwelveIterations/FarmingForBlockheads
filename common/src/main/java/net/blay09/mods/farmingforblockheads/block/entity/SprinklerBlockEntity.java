package net.blay09.mods.farmingforblockheads.block.entity;

import net.blay09.mods.farmingforblockheads.HydratedFarmlandData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SprinklerBlockEntity extends BlockEntity {

    private static final int HYDRATION_INTERVAL = 20;
    private static final int PARTICLE_INTERVAL = 4;
    private static final int RANGE = 4;

    private int tickTimer;

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.sprinkler.value(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity blockEntity) {
        blockEntity.serverTick(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity blockEntity) {
        blockEntity.clientTick(level);
    }

    private void serverTick(Level level) {
        tickTimer++;
        if (tickTimer < HYDRATION_INTERVAL || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        tickTimer = 0;
        final var hydratedFarmlandData = HydratedFarmlandData.get(serverLevel);
        BlockPos.betweenClosed(worldPosition.offset(-RANGE, -1, -RANGE), worldPosition.offset(RANGE, 0, RANGE)).forEach(targetPos -> {
            final var state = level.getBlockState(targetPos);
            if (state.getBlock() instanceof FarmlandBlock) {
                hydratedFarmlandData.hydrate(serverLevel, targetPos.immutable());
                level.setBlock(targetPos, state.setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
            }
        });
    }

    private void clientTick(Level level) {
        tickTimer++;
        if (tickTimer % PARTICLE_INTERVAL != 0) {
            return;
        }

        final RandomSource random = level.getRandom();
        final double x = worldPosition.getX() + 0.5;
        final double y = worldPosition.getY() + 0.85;
        final double z = worldPosition.getZ() + 0.5;
        for (int i = 0; i < 4; i++) {
            final double angle = random.nextDouble() * Math.PI * 2;
            final double speed = 0.08 + random.nextDouble() * 0.08;
            level.addParticle(ParticleTypes.SPLASH,
                    x,
                    y,
                    z,
                    Math.cos(angle) * speed,
                    0.03 + random.nextDouble() * 0.02,
                    Math.sin(angle) * speed);
        }
    }
}
