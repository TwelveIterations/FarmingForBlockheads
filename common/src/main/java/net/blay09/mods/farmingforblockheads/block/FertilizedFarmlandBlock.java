package net.blay09.mods.farmingforblockheads.block;

import com.mojang.serialization.MapCodec;
import net.blay09.mods.balm.api.block.CustomFarmBlock;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.item.FertilizerItem;
import net.blay09.mods.farmingforblockheads.mixin.FarmBlockAccessor;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.blay09.mods.farmingforblockheads.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class FertilizedFarmlandBlock extends FarmBlock implements CustomFarmBlock {

    public static final MapCodec<FarmBlock> CODEC = simpleCodec(FertilizedFarmlandBlock::new);

    public FertilizedFarmlandBlock(Properties properties) {
        super(properties.sound(SoundType.GRAVEL).strength(0.6f).randomTicks());
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
        int moisture = state.getValue(MOISTURE);

        if (!FarmBlockAccessor.callIsNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!FarmBlockAccessor.callShouldMaintainFarmland(level, pos) && state.is(ModBlockTags.STABLE_FARMLAND)) {
                turnToDirt(null, state, level, pos);
            }
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        }
    }

    @Override
    public MapCodec<FarmBlock> codec() {
        return CODEC;
    }
}
