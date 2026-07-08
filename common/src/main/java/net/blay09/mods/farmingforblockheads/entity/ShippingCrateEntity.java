package net.blay09.mods.farmingforblockheads.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ShippingCrateEntity extends Entity {

    private static final int COURIER_DELAY_TICKS = 40;
    private static final EntityDataAccessor<Integer> FACING = SynchedEntityData.defineId(ShippingCrateEntity.class, EntityDataSerializers.INT);

    public ShippingCrateEntity(EntityType<? extends ShippingCrateEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        noPhysics = true;
        setNoGravity(true);

        if (!level().isClientSide() && tickCount == 1) {
            level().playSound(null, getX(), getY() + getBbHeight() * 0.5, getZ(), SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.NEUTRAL, 0.6f, 0.7f);
        }

        if (!level().isClientSide() && tickCount == COURIER_DELAY_TICKS) {
            spawnCourier((ServerLevel) level());
        }
    }

    private void spawnCourier(ServerLevel level) {
        final var courier = new CourierEntity(ModEntities.courier.value(), level);
        final var courierDirection = pickCourierDirection(level, courier);
        final var courierX = getX() + courierDirection.getStepX() * 0.85;
        final var courierZ = getZ() + courierDirection.getStepZ() * 0.85;
        final var facingCrate = courierDirection.getOpposite();
        courier.setPos(courierX, getY(), courierZ);
        courier.setYRot(facingCrate.toYRot());
        courier.setYHeadRot(facingCrate.toYRot());
        courier.setYBodyRot(facingCrate.toYRot());
        courier.setCrateId(getId());
        level.addFreshEntity(courier);
        courier.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPosition()), EntitySpawnReason.STRUCTURE, null);
    }

    private Direction pickCourierDirection(ServerLevel level, CourierEntity courier) {
        final var defaultDirection = getFacing().getOpposite();
        final var directions = new Direction[]{
                defaultDirection,
                defaultDirection.getClockWise(),
                defaultDirection.getCounterClockWise(),
                defaultDirection.getOpposite()
        };

        for (final var direction : directions) {
            final var pos = blockPosition().relative(direction);
            if (canSpawnCourierAt(level, courier, pos, direction)) {
                return direction;
            }
        }

        return defaultDirection;
    }

    private boolean canSpawnCourierAt(ServerLevel level, CourierEntity courier, BlockPos pos, Direction direction) {
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) {
            return false;
        }

        courier.setPos(getX() + direction.getStepX() * 0.85, getY(), getZ() + direction.getStepZ() * 0.85);
        return level.noCollision(courier);
    }

    public void setFacing(Direction facing) {
        entityData.set(FACING, facing.get2DDataValue());
        setYRot(facing.toYRot());
    }

    public Direction getFacing() {
        return Direction.from2DDataValue(entityData.get(FACING));
    }

    public void disappear(ServerLevel level) {
        level.playSound(null, getX(), getY() + getBbHeight() * 0.5, getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.8f, 1f);
        level.sendParticles(ParticleTypes.PORTAL, getX(), getY() + getBbHeight() * 0.5, getZ(), 48, 0.35, 0.35, 0.35, 0.08);
        discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FACING, Direction.NORTH.get2DDataValue());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
