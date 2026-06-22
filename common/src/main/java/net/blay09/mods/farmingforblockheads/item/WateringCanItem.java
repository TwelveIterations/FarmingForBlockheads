package net.blay09.mods.farmingforblockheads.item;

import net.blay09.mods.farmingforblockheads.HydratedFarmlandData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class WateringCanItem extends Item {

    private static final int USE_DURATION = 72000;
    private static final int WATERING_INTERVAL = 10;

    public WateringCanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final var level = context.getLevel();
        final var pos = context.getClickedPos();
        final var stack = context.getItemInHand();

        if (isWaterSource(level, pos)) {
            return refill(level, pos, stack, context.getPlayer());
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        final var hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        final var pos = hitResult.getBlockPos();
        if (isWaterSource(level, pos)) {
            return refill(level, pos, player.getItemInHand(hand), player);
        }

        final var stack = player.getItemInHand(hand);
        if (!hasWater(stack)) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!hasWater(stack)) {
            livingEntity.stopUsingItem();
            return;
        }

        final var usedTicks = getUseDuration(stack, livingEntity) - remainingUseDuration;
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        final var direction = livingEntity.getDirection();
        final var groundPos = livingEntity.blockPosition();
        final var wateringArea = getWateringArea(groundPos, direction);
        if (usedTicks % 2 == 0) {
            spawnWaterParticles(serverLevel, wateringArea, livingEntity.getY() + 0.1f);
        }

        stack.setDamageValue(Math.min(stack.getDamageValue() + 1, stack.getMaxDamage() - 1));

        if (usedTicks % WATERING_INTERVAL != 0) {
            return;
        }

        final var data = HydratedFarmlandData.get(serverLevel);
        var hasTargets = false;
        var hydratedAny = false;
        for (final var targetPos : wateringArea) {
            final var pos = level.getBlockState(targetPos).getBlock() instanceof FarmlandBlock ? targetPos : targetPos.below();
            final var state = level.getBlockState(pos);
            if (state.getBlock() instanceof FarmlandBlock) {
                hasTargets = true;
                hydratedAny |= data.hydrate(serverLevel, pos);
                level.setBlock(pos, state.setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
            }
        }

        if (hasTargets && (hydratedAny || usedTicks == WATERING_INTERVAL) && livingEntity instanceof Player player) {
            player.sendOverlayMessage(Component.translatable("message.farmingforblockheads.fully_watered")
                    .withStyle(ChatFormatting.BLUE));
        }

        final var soundPos = groundPos.relative(direction, 2);
        level.playSound(null, soundPos, SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.5f, 0.8f);
        if (!hasWater(stack)) {
            livingEntity.stopUsingItem();
        }
    }

    private void spawnWaterParticles(ServerLevel level, BlockPos[] wateringArea, double y) {
        for (final var pos : wateringArea) {
            level.sendParticles(ParticleTypes.FALLING_WATER,
                    pos.getX() + 0.5,
                    y + 0.05,
                    pos.getZ() + 0.5,
                    2,
                    0.3,
                    0.1,
                    0.3,
                    0.05);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF3495DA;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (!hasWater(stack)) {
            tooltip.accept(Component.translatable("tooltip.farmingforblockheads.watering_can.refill"));
        }
    }

    static BlockPos[] getWateringArea(BlockPos origin, Direction direction) {
        final var side = direction.getClockWise();
        final var positions = new BlockPos[6];
        int index = 0;
        for (int distance = 1; distance <= 2; distance++) {
            final var rowCenter = origin.relative(direction, distance);
            for (int offset = -1; offset <= 1; offset++) {
                positions[index++] = rowCenter.relative(side, offset);
            }
        }
        return positions;
    }

    private InteractionResult refill(Level level, BlockPos pos, ItemStack stack, @Nullable Player player) {
        if (stack.getDamageValue() == 0) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            stack.setDamageValue(0);
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1f, 1f);
        }

        return InteractionResult.SUCCESS;
    }

    private boolean isWaterSource(Level level, BlockPos pos) {
        return level.getFluidState(pos).isSourceOfType(Fluids.WATER);
    }

    private boolean hasWater(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage() - 1;
    }
}
