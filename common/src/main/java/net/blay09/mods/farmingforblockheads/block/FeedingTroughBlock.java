package net.blay09.mods.farmingforblockheads.block;

import com.mojang.serialization.MapCodec;
import net.blay09.mods.balm.api.container.ContainerUtils;
import net.blay09.mods.farmingforblockheads.block.entity.FeedingTroughBlockEntity;
import net.blay09.mods.farmingforblockheads.block.entity.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class FeedingTroughBlock extends BaseEntityBlock {

    public static final MapCodec<FeedingTroughBlock> CODEC = simpleCodec(FeedingTroughBlock::new);

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 10, 16);
    private static final VoxelShape RENDER_SHAPE = SHAPE.move(0, 0.01, 0);

    protected FeedingTroughBlock(Properties properties) {
        super(properties.sound(SoundType.WOOD).strength(2f));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeedingTroughBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            ItemStack heldItem = player.getItemInHand(hand);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FeedingTroughBlockEntity feedingTrough) {
                Container container = feedingTrough.getContainer();
                if (player.isShiftKeyDown()) {
                    if (heldItem.isEmpty()) {
                        player.setItemInHand(hand, ContainerUtils.extractItem(container, 0, 64, false));
                    } else {
                        ItemStack restStack = ContainerUtils.extractItem(container, 0, 64, false);
                        if (!player.getInventory().add(restStack)) {
                            ContainerUtils.insertItemStacked(container, restStack, false);
                        }
                    }
                } else {
                    ItemStack restStack = ContainerUtils.insertItemStacked(container, heldItem, false);
                    player.setItemInHand(hand, restStack);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return RENDER_SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.feedingTrough.get(), FeedingTroughBlockEntity::serverTick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
