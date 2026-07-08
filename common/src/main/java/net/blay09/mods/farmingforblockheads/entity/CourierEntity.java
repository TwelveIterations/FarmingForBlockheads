package net.blay09.mods.farmingforblockheads.entity;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public class CourierEntity extends Mob {

    public static final int LOOK_FORWARD_TICKS = 20;
    public static final int LOOK_DOWN_TICKS = 20;
    public static final int LOOK_AT_PLAYER_TICKS = 20;
    public static final int REACH_DELAY_TICKS = LOOK_FORWARD_TICKS + LOOK_DOWN_TICKS + LOOK_AT_PLAYER_TICKS;
    public static final int DISAPPEAR_DELAY_TICKS = REACH_DELAY_TICKS + 20;
    private static final EntityDataAccessor<Boolean> REACHING = SynchedEntityData.defineId(CourierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Random rand = new Random();

    private int crateId = -1;

    public CourierEntity(EntityType<? extends CourierEntity> type, Level level) {
        super(type, level);
        setNoAi(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (tickCount == 1) {
                playTeleportEffect((ServerLevel) level());
            } else if (tickCount == REACH_DELAY_TICKS) {
                entityData.set(REACHING, true);
            } else if (tickCount == DISAPPEAR_DELAY_TICKS) {
                disappear((ServerLevel) level());
            }
        }
    }

    private void playTeleportEffect(ServerLevel level) {
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.8f, 1f);
        level.sendParticles(ParticleTypes.PORTAL, getX(), getY() + getBbHeight() * 0.5, getZ(), 48, 0.35, 0.7, 0.35, 0.08);
    }

    private void disappear(ServerLevel level) {
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.8f, 1f);
        level.sendParticles(ParticleTypes.PORTAL, getX(), getY() + getBbHeight() * 0.5, getZ(), 48, 0.35, 0.7, 0.35, 0.08);
        final Entity crate = level.getEntity(crateId);
        if (crate instanceof ShippingCrateEntity shippingCrate) {
            shippingCrate.disappear(level);
        }
        discard();
    }

    public void setCrateId(int crateId) {
        this.crateId = crateId;
    }

    public boolean isReaching() {
        return entityData.get(REACHING);
    }

    public float getAnimationTicks(float delta) {
        return tickCount + delta;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(REACHING, false);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        setCustomName(Component.literal(FarmingForBlockheadsConfig.getActive().getRandomMerchantName(rand)));
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float damageAmount) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
