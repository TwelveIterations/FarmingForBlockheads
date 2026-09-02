package net.blay09.mods.farmingforblockheads.block;

import net.blay09.mods.balm.world.level.block.CustomFarmBlock;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.HydratedFarmlandData;
import net.blay09.mods.farmingforblockheads.mixin.FarmlandBlockAccessor;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class FertilizedFarmlandBlock extends FarmlandBlock implements CustomFarmBlock {

    public FertilizedFarmlandBlock(Properties properties) {
        super(Blocks.DIRT, properties.sound(SoundType.GRAVEL).strength(0.6f).randomTicks());
    }

    @Override
    public boolean canSustainPlant(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, Block block) {
        return true;
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return state.getValue(MOISTURE) > 0;
    }

    public float getRegressionChance() {
        return (float) FarmingForBlockheadsConfig.getActive().fertilizerRegressionChance;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (!state.is(ModBlockTags.STABLE_FARMLAND)) {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (HydratedFarmlandData.get(level).isHydrated(level, pos)) {
            if (state.getValue(MOISTURE) < MAX_MOISTURE) {
                level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
            }
            return;
        }

        int moisture = state.getValue(MOISTURE);

        final var isNearWater = !FarmingForBlockheadsConfig.getActive().disableVanillaWatering
                && FarmlandBlockAccessor.callIsNearWater(level, pos);
        if (!isNearWater && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!FarmlandBlockAccessor.callShouldMaintainFarmland(level, pos) && state.is(ModBlockTags.STABLE_FARMLAND)) {
                turnToBaseBlock(null, state, level, pos);
            }
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        }
    }

}
