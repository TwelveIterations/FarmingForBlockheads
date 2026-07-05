package net.blay09.mods.farmingforblockheads.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FallingShippingCrateEntity extends Entity {

    private static final double GRAVITY = 0.04;
    private static final double DRAG = 0.98;

    public FallingShippingCrateEntity(EntityType<? extends FallingShippingCrateEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (onGround()) {
            breakApart();
            return;
        }

        setDeltaMovement(getDeltaMovement().add(0, -GRAVITY, 0).scale(DRAG));
        move(MoverType.SELF, getDeltaMovement());

        if (onGround()) {
            breakApart();
        }
    }

    private void breakApart() {
        if (!level().isClientSide()) {
            final var particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_PLANKS.defaultBlockState());
            level().playSound(null, getX(), getY() + getBbHeight() * 0.5, getZ(), SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 1f, 0.8f);
            level().playSound(null, getX(), getY() + getBbHeight() * 0.5, getZ(), SoundEvents.WOOD_BREAK, SoundSource.NEUTRAL, 1f, 0.8f);
            ((ServerLevel) level()).sendParticles(particle, getX(), getY() + getBbHeight() * 0.5, getZ(), 48, 0.35, 0.35, 0.35, 0.08);
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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
