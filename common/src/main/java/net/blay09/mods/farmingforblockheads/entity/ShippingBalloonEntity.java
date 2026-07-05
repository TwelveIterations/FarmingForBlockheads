package net.blay09.mods.farmingforblockheads.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ShippingBalloonEntity extends Entity {

    private static final int INFLATION_DURATION_TICKS = 20;
    private static final double FLOAT_DISTANCE = 172.0;
    private static final double FLOAT_SPEED = 0.12;
    private static final double DRIFT_SPEED = 0.01;
    private static final double GOLDEN_RATIO = 1.618033988749895;

    private static final EntityDataAccessor<Float> WIND_DRIFT_X = SynchedEntityData.defineId(ShippingBalloonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WIND_DRIFT_Z = SynchedEntityData.defineId(ShippingBalloonEntity.class, EntityDataSerializers.FLOAT);

    private double startY;

    public ShippingBalloonEntity(EntityType<? extends ShippingBalloonEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        noPhysics = true;
        setNoGravity(true);

        if (tickCount == 1) {
            startY = getY();

            if (!level().isClientSide()) {
                initializeWindDrift();
            }
        }

        if (tickCount <= INFLATION_DURATION_TICKS) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }

        setDeltaMovement(entityData.get(WIND_DRIFT_X), FLOAT_SPEED, entityData.get(WIND_DRIFT_Z));
        move(MoverType.SELF, getDeltaMovement());

        if (!level().isClientSide() && getY() >= startY + FLOAT_DISTANCE) {
            ((ServerLevel) level()).sendParticles(ParticleTypes.SMOKE, getX(), getY() + getBbHeight() * 0.5, getZ(), 16, 0.35, 0.35, 0.35, 0.02);
            discard();
        }
    }

    private void initializeWindDrift() {
        final var windTime = level().getGameTime() / 6000L;
        final var angle = (windTime * GOLDEN_RATIO) % (Math.PI * 2);
        entityData.set(WIND_DRIFT_X, (float) (Math.cos(angle) * DRIFT_SPEED));
        entityData.set(WIND_DRIFT_Z, (float) (Math.sin(angle) * DRIFT_SPEED));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WIND_DRIFT_X, 0f);
        builder.define(WIND_DRIFT_Z, 0f);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        discard();
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
