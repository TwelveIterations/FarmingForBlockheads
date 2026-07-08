package net.blay09.mods.farmingforblockheads.entity;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class FallingShippingCrateEntity extends Entity {

    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, FarmingForBlockheads.id("entities/falling_shipping_crate"));
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
            dropFromLootTable((ServerLevel) level());
            discard();
        }
    }

    private void dropFromLootTable(ServerLevel level) {
        final var lootTable = level.getServer().reloadableRegistries().getLootTable(LOOT_TABLE);
        final var lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(lootParams, itemStack -> spawnAtLocation(level, itemStack, Vec3.ZERO));
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
