package net.blay09.mods.farmingforblockheads.block;

import net.blay09.mods.farmingforblockheads.block.entity.ModBlockEntities;
import net.blay09.mods.farmingforblockheads.block.entity.ShippingBinBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ShippingBinBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = createShape();

    public ShippingBinBlock(Properties properties) {
        super(properties.sound(SoundType.WOOD).strength(2f));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static VoxelShape createShape() {
        return Shapes.or(
                Block.box(0, 0, 0, 2, 2, 2),
                Block.box(14, 0, 0, 16, 2, 2),
                Block.box(0, 0, 14, 2, 2, 16),
                Block.box(14, 0, 14, 16, 2, 16),
                Block.box(2, 0.25, 0.25, 14, 1.75, 1.75),
                Block.box(14.25, 0.25, 2, 15.75, 1.75, 14),
                Block.box(2, 0.25, 14.25, 14, 1.75, 15.75),
                Block.box(0.25, 0.25, 2, 1.75, 1.75, 14),
                Block.box(1.5, 1.75, 1.5, 14.5, 3.25, 14.5),
                Block.box(1.75, 1.75, 0.5, 14.25, 14.25, 1.5),
                Block.box(14.5, 1.75, 1.75, 15.5, 14.25, 14.25),
                Block.box(1.75, 1.75, 14.5, 14.25, 14.25, 15.5),
                Block.box(0.5, 1.75, 1.75, 1.5, 14.25, 14.25),
                Block.box(14.25, 2, 0.25, 15.75, 14, 1.75),
                Block.box(0.25, 2, 0.25, 1.75, 14, 1.75),
                Block.box(14.25, 2, 14.25, 15.75, 14, 15.75),
                Block.box(0.25, 2, 14.25, 1.75, 14, 15.75),
                Block.box(0, 14, 0, 2, 16, 2),
                Block.box(14, 14, 0, 16, 16, 2),
                Block.box(0, 14, 14, 2, 16, 16),
                Block.box(14, 14, 14, 16, 16, 16),
                Block.box(2, 14.25, 0.25, 14, 15.75, 1.75),
                Block.box(14.25, 14.25, 2, 15.75, 15.75, 14),
                Block.box(2, 14.25, 14.25, 14, 15.75, 15.75),
                Block.box(0.25, 14.25, 2, 1.75, 15.75, 14));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShippingBinBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ShippingBinBlockEntity shippingBin) {
            shippingBin.openMenu(player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = state.getValue(FACING);
        return state.setValue(FACING, mirror.getRotation(facing).rotate(facing));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.shippingBin.value(), ShippingBinBlockEntity::serverTick);
    }

}
