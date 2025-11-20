package net.blay09.mods.farmingforblockheads;

import net.blay09.mods.balm.platform.event.EventHandling;
import net.blay09.mods.farmingforblockheads.block.FertilizedFarmlandBlock;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FarmlandHandler {

    public static EventHandling onGrowEvent(LevelAccessor levelAccessor, BlockPos pos, BlockState state) {
        BlockState plant = levelAccessor.getBlockState(pos);
        if (plant.getBlock() instanceof BonemealableBlock growable && levelAccessor instanceof Level level) {
            BlockState farmland = levelAccessor.getBlockState(pos.below());
            if (farmland.is(ModBlockTags.HEALTHY_FARMLAND)) {
                final var doubleGrowthChance = (float) FarmingForBlockheadsConfig.getActive().fertilizerBonusGrowthChance;
                if (Math.random() <= doubleGrowthChance) {
                    if (growable.isValidBonemealTarget(levelAccessor, pos, plant)) {
                        growable.performBonemeal(((ServerLevel) levelAccessor), levelAccessor.getRandom(), pos, plant);
                        levelAccessor.levelEvent(2005, pos, 0);
                        rollRegression(level, pos.below(), farmland);
                    }
                }
            }
        }

        return EventHandling.RESUME;
    }

    public static void rollRegression(Level level, BlockPos pos, BlockState farmland) {
        if (farmland.getBlock() instanceof FertilizedFarmlandBlock) {
            if (Math.random() <= ((FertilizedFarmlandBlock) farmland.getBlock()).getRegressionChance()) {
                level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, farmland.getValue(FarmBlock.MOISTURE)));
            }
        }
    }
}
