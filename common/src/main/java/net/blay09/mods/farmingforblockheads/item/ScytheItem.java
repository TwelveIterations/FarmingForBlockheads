package net.blay09.mods.farmingforblockheads.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ScytheItem extends Item {

    private static final int HARVEST_RADIUS = 1;
    private static final ThreadLocal<Boolean> HARVESTING = ThreadLocal.withInitial(() -> false);

    public ScytheItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return super.useOn(context);
        }

        final var player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }

        final var clickedPos = context.getClickedPos();
        final var center = context.getLevel().getBlockState(clickedPos).is(BlockTags.SUPPORTS_CROPS)
                ? clickedPos.above()
                : clickedPos;
        return harvestArea(player, center) > 0 ? InteractionResult.SUCCESS : super.useOn(context);
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide()
                && entity instanceof ServerPlayer serverPlayer
                && isMatureCrop(state)
                && !HARVESTING.get()) {
            harvestArea(serverPlayer, pos);
        }

        return super.mineBlock(itemStack, level, state, pos, entity);
    }

    private int harvestArea(Player player, BlockPos center) {
        if (HARVESTING.get()) {
            return 0;
        }

        final var heldItem = player.getMainHandItem();
        if (!heldItem.is(this)) {
            return 0;
        }

        int harvested = 0;
        HARVESTING.set(true);
        try {
            final var level = player.level();
            for (final var pos : BlockPos.betweenClosed(
                    center.offset(-HARVEST_RADIUS, 0, -HARVEST_RADIUS),
                    center.offset(HARVEST_RADIUS, 0, HARVEST_RADIUS))) {
                if (heldItem.isEmpty()) {
                    break;
                }

                if (isMatureCrop(level.getBlockState(pos))
                        && (!(player instanceof ServerPlayer serverPlayer)
                        || serverPlayer.gameMode.destroyBlock(pos))) {
                    harvested++;
                }
            }
        } finally {
            HARVESTING.set(false);
        }

        return harvested;
    }

    private static boolean isMatureCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
    }

}
