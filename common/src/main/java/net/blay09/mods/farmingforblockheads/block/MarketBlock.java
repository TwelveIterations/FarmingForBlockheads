package net.blay09.mods.farmingforblockheads.block;

import net.blay09.mods.farmingforblockheads.block.entity.MarketBlockEntity;
import net.blay09.mods.farmingforblockheads.entity.MerchantEntity;
import net.blay09.mods.farmingforblockheads.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class MarketBlock extends BaseEntityBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape TOP_SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    private static final VoxelShape RENDER_SHAPE = Block.box(0, 0.01, 0, 16, 16, 16);

    public MarketBlock(Properties properties) {
        super(properties.sound(SoundType.WOOD).strength(2f));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
        builder.add(FACING);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos directionPos, BlockState directionState, RandomSource randomSource) {
        final var half = state.getValue(HALF);
        if ((direction.getAxis() != Direction.Axis.Y)
                || ((half == DoubleBlockHalf.LOWER) != (direction == Direction.UP))
                || ((directionState.getBlock() == this)
                && (directionState.getValue(HALF) != half))) {
            if ((half != DoubleBlockHalf.LOWER) || (direction != Direction.DOWN) || state.canSurvive(level, pos)) {
                return state;
            }
        }

        return Blocks.AIR.defaultBlockState();
    }

    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        final var posBelow = pos.below();
        final var stateBelow = level.getBlockState(posBelow);
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? super.canSurvive(state, level, pos) : stateBelow.is(this);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarketBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext useContext) {
        final var world = useContext.getLevel();
        final var pos = useContext.getClickedPos();
        if (pos.getY() < world.getHeight() - 1) {
            if (world.getBlockState(pos.above()).canBeReplaced(useContext)) {
                return defaultBlockState().setValue(FACING, useContext.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER);
            }
        }

        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        BlockPos entityPos = pos.relative(facing.getOpposite());

        MerchantEntity.SpawnAnimationType spawnAnimationType = MerchantEntity.SpawnAnimationType.MAGIC;
        if (level.canSeeSkyFromBelowWater(entityPos)) {
            spawnAnimationType = MerchantEntity.SpawnAnimationType.FALLING;
        } else if (!level.isEmptyBlock(entityPos.below())) {
            spawnAnimationType = MerchantEntity.SpawnAnimationType.DIGGING;
        }

        if (!level.isClientSide()) {
            MerchantEntity merchant = new MerchantEntity(ModEntities.merchant.value(), level);
            merchant.setMarket(pos, facing);
            merchant.setToFacingAngle();
            merchant.setSpawnAnimation(spawnAnimationType);

            if (level.canSeeSkyFromBelowWater(entityPos)) {
                merchant.setPos(entityPos.getX() + 0.5, entityPos.getY() + 172, entityPos.getZ() + 0.5);
            } else if (!level.isEmptyBlock(entityPos.below())) {
                merchant.setPos(entityPos.getX() + 0.5, entityPos.getY(), entityPos.getZ() + 0.5);
            } else {
                merchant.setPos(entityPos.getX() + 0.5, entityPos.getY(), entityPos.getZ() + 0.5);
            }

            level.addFreshEntity(merchant);
            merchant.finalizeSpawn(((ServerLevel) level), ((ServerLevel) level).getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        }

        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        use(state, level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        use(state, level, pos, player);
        return InteractionResult.SUCCESS;
    }

    private void use(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof MarketBlockEntity market) {
                market.openMenu(player);
            }
        }
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = state.getValue(FACING);
        return state.setValue(FACING, mirror.getRotation(facing).rotate(facing));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            final var TOP_SHAPES = new VoxelShape[]{
                    Block.box(0, 0, 0, 16, 13, 11),
                    Block.box(5, 0, 0, 16, 13, 16),
                    Block.box(0, 0, 5, 16, 13, 16),
                    Block.box(0, 0, 0, 11, 13, 16),
            };
            return TOP_SHAPES[state.getValue(FACING).get2DDataValue()];
        }

        return super.getShape(state, blockGetter, pos, collisionContext);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return Shapes.empty();
        }

        return RENDER_SHAPE;
    }

}
