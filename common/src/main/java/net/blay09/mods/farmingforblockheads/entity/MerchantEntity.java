package net.blay09.mods.farmingforblockheads.entity;

import com.mojang.serialization.Codec;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.block.entity.MarketBlockEntity;
import net.blay09.mods.farmingforblockheads.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Random;

public class MerchantEntity extends PathfinderMob {

    public enum SpawnAnimationType {
        MAGIC,
        FALLING,
        DIGGING
    }

    private static final Random rand = new Random();

    private BlockPos marketPos;
    private Direction facing;
    private boolean spawnAnimationStarted;
    private boolean spawnDone;
    private SpawnAnimationType spawnAnimation = SpawnAnimationType.MAGIC;

    private BlockPos marketEntityPos;
    private int diggingAnimation;
    private BlockState diggingBlockState;

    private ResourceLocation textureLocation;

    public MerchantEntity(EntityType<MerchantEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8f, 0.6, 0.6));
        goalSelector.addGoal(5, new MerchantGoal(this, 0.6));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        final var market = getMarketBlockEntity();
        if (market != null) {
            market.openMenu(player);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (marketPos != null) {
            compound.putLong("MarketPos", marketPos.asLong());
        }
        if (facing != null) {
            compound.putByte("Facing", (byte) facing.get3DDataValue());
        }
        compound.putBoolean("SpawnDone", spawnDone);
        compound.putByte("SpawnAnimation", (byte) spawnAnimation.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (!hasCustomName()) {
            String merchantName = FarmingForBlockheadsConfig.getActive().getRandomMerchantName(rand);
            setCustomName(Component.literal(merchantName));
        }
        compound.getLong("MarketPos").map(BlockPos::of).ifPresent(marketPos -> {
            setMarket(marketPos, Direction.from3DDataValue(compound.getByteOr("Facing", (byte) 0)));
        });
        spawnDone = compound.getBooleanOr("SpawnDone", false);
        spawnAnimation = SpawnAnimationType.values()[compound.getByteOr("SpawnAnimation", (byte) 0)];
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public void tick() {
        super.tick();
        var level = level();
        if (!level.isClientSide) {
            if (tickCount % 20 == 0) {
                if (!isMarketValid()) {
                    level.broadcastEntityEvent(this, (byte) 12);
                    remove(RemovalReason.DISCARDED);
                }
            }
        }

        if (!spawnDone && !spawnAnimationStarted) {
            spawnAnimationStarted = true;
            switch (spawnAnimation) {
                case DIGGING -> level.broadcastEntityEvent(this, (byte) 13);
                case FALLING -> level.broadcastEntityEvent(this, (byte) 14);
                case MAGIC -> level.broadcastEntityEvent(this, (byte) 15);
            }
        }

        if (diggingAnimation > 0) {
            diggingAnimation--;

            double posX = getX();
            double posY = getY();
            double posZ = getZ();

            for (int i = 0; i < 4; i++) {
                BlockState diggingState = diggingBlockState != null ? diggingBlockState : Blocks.DIRT.defaultBlockState();
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, diggingState),
                        posX,
                        posY,
                        posZ,
                        Math.random() * 2 - 1,
                        Math.random() * 4,
                        Math.random() * 2 - 1);
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, diggingState),
                        posX,
                        posY,
                        posZ,
                        (Math.random() - 0.5) * 0.5,
                        Math.random() * 0.5f,
                        (Math.random() - 0.5) * 0.5);
            }

            if (diggingAnimation % 2 == 0) {
                level.playLocalSound(posX, posY, posZ, SoundType.GRAVEL.getHitSound(), SoundSource.BLOCKS, 0.2f, (float) (Math.random() + 0.5), false);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        var level = level();
        if (id == 12) {
            disappear();
        } else if (id == 13) {
            diggingBlockState = level.getBlockState(blockPosition().below());
            diggingAnimation = 60;
        } else if (id == 14) {
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPosition());
            level.playLocalSound(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, ModSounds.falling.get(), SoundSource.NEUTRAL, 0.2f, 1f, false);
        } else if (id == 15) {
            double posX = getX();
            double posY = getY();
            double posZ = getZ();

            level.playLocalSound(posX + 0.5, posY + 1, posZ + 0.5, SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.2f, 1f, false);
            for (int i = 0; i < 50; i++) {
                level.addParticle(ParticleTypes.FIREWORK,
                        posX + 0.5,
                        posY + 1,
                        posZ + 0.5,
                        (Math.random() - 0.5) * 0.5f,
                        (Math.random() - 0.5) * 0.5f,
                        (Math.random() - 0.5) * 0.5f);
            }
            level.addParticle(ParticleTypes.EXPLOSION, posX + 0.5, posY + 1, posZ + 0.5, 0, 0, 0);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float damageAmount) {
        if (!spawnDone && damageSource == level.damageSources().fall()) {
            double posX = getX();
            double posY = getY();
            double posZ = getZ();
            level.playLocalSound(posX, posY, posZ, getHurtSound(damageSource), SoundSource.NEUTRAL, 1f, 2f, false);
            spawnDone = true;
            return;
        }
        super.actuallyHurt(level, damageSource, damageAmount);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        if (Math.random() < 0.001) {
            setCustomName(Component.literal(Math.random() <= 0.5 ? "Pam" : "Blay"));
        } else {
            String merchantName = FarmingForBlockheadsConfig.getActive().getRandomMerchantName(rand);
            setCustomName(Component.literal(merchantName));
        }

        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean isBaby() {
        return FarmingForBlockheadsConfig.getActive().treatMerchantsLikeBabies;
    }

    public void setMarket(BlockPos marketPos, Direction facing) {
        this.marketPos = marketPos;
        this.marketEntityPos = marketPos.relative(facing.getOpposite());
        this.facing = facing;
    }

    @Nullable
    public BlockPos getMarketEntityPosition() {
        return marketEntityPos;
    }

    public boolean isAtMarket() {
        return marketEntityPos != null && distanceToSqr(Vec3.atCenterOf(marketEntityPos.relative(facing.getOpposite()))) <= 1;
    }

    @Nullable
    private MarketBlockEntity getMarketBlockEntity() {
        if (marketPos == null) {
            return null;
        }

        BlockEntity tileEntity = level().getBlockEntity(marketPos);
        if (tileEntity instanceof MarketBlockEntity market) {
            return market;
        }

        return null;
    }

    private boolean isMarketValid() {
        return marketPos != null && level().getBlockState(marketPos).getBlock() == ModBlocks.market;
    }

    public void setToFacingAngle() {
        float facingAngle = facing.toYRot();
        setRot(facingAngle, 0f);
        setYHeadRot(facingAngle);
        setYBodyRot(facingAngle);
    }

    private void disappear() {
        double posX = getX();
        double posY = getY();
        double posZ = getZ();

        var level = level();
        level.playLocalSound(posX, posY, posZ, SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1f, 1f, false);
        for (int i = 0; i < 50; i++) {
            level.addParticle(ParticleTypes.FIREWORK,
                    posX,
                    posY + 1,
                    posZ,
                    (Math.random() - 0.5) * 0.5f,
                    (Math.random() - 0.5) * 0.5f,
                    (Math.random() - 0.5) * 0.5f);
        }
        level.addParticle(ParticleTypes.EXPLOSION, posX, posY + 1, posZ, 0, 0, 0);
        remove(RemovalReason.DISCARDED);
    }

    public void setSpawnAnimation(SpawnAnimationType spawnAnimation) {
        this.spawnAnimation = spawnAnimation;
    }

    public int getDiggingAnimation() {
        return diggingAnimation;
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        super.setCustomName(component);
        textureLocation = null; // reset to have it recalculate
    }

    @Nullable
    public ResourceLocation getTextureLocation() {
        Component customName = getCustomName();
        if (textureLocation == null && customName != null) {
            String normalizedName = customName.getString();
            normalizedName = normalizedName.replaceAll("[^A-Za-z0-9]", "_");
            normalizedName = normalizedName.toLowerCase(Locale.ENGLISH);
            textureLocation = ResourceLocation.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "textures/entity/merchant_" + normalizedName + ".png");
        } else if (textureLocation != null && customName == null) {
            textureLocation = null;
        }
        return textureLocation;
    }
}
