package net.blay09.mods.farmingforblockheads.item;

import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.blay09.mods.farmingforblockheads.block.FertilizedFarmlandBlock;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class FertilizerItem extends Item {

    public enum FertilizerType {
        HEALTHY(Component.translatable("tooltip.farmingforblockheads.red_fertilizer").withStyle(ChatFormatting.DARK_RED)),
        RICH(Component.translatable("tooltip.farmingforblockheads.green_fertilizer").withStyle(ChatFormatting.GREEN)),
        STABLE(Component.translatable("tooltip.farmingforblockheads.yellow_fertilizer").withStyle(ChatFormatting.YELLOW));

        private final Component tooltip;

        FertilizerType(Component tooltip) {
            this.tooltip = tooltip;
        }

        public Component getTooltip() {
            return tooltip;
        }

        public boolean canFertilize(BlockState state) {
            Block sourceBlock = state.getBlock();
            return sourceBlock == Blocks.FARMLAND || sourceBlock instanceof FertilizedFarmlandBlock;
        }

        public BlockState applyFertilizer(BlockState state) {
            if (!canFertilize(state)) {
                return state;
            }

            int moisture = state.getValue(BlockStateProperties.MOISTURE);
            final var traits = new HashSet<FertilizerType>();
            traits.add(this);
            if (state.is(ModBlockTags.HEALTHY_FARMLAND)) {
                traits.add(HEALTHY);
            }

            if (state.is(ModBlockTags.RICH_FARMLAND)) {
                traits.add(RICH);
            }

            if (state.is(ModBlockTags.STABLE_FARMLAND)) {
                traits.add(STABLE);
            }

            final var targetBlock = getBlockForTraits(traits);
            if (targetBlock == null) {
                return state;
            }

            BlockState newState = targetBlock.defaultBlockState();
            return newState.setValue(FertilizedFarmlandBlock.MOISTURE, moisture);
        }

        @Nullable
        private static DeferredBlock getBlockForTraits(Set<FertilizerType> traits) {
            boolean hasStableTrait = traits.contains(STABLE);
            boolean hasHealthyTrait = traits.contains(HEALTHY);
            boolean hasRichTrait = traits.contains(RICH);
            if (hasStableTrait && !hasRichTrait && !hasHealthyTrait) {
                return ModBlocks.fertilizedFarmlandStable;
            }

            if (!hasStableTrait && hasRichTrait && !hasHealthyTrait) {
                return ModBlocks.fertilizedFarmlandRich;
            }

            if (!hasStableTrait && !hasRichTrait && hasHealthyTrait) {
                return ModBlocks.fertilizedFarmlandHealthy;
            }

            if (hasStableTrait && hasRichTrait && !hasHealthyTrait) {
                return ModBlocks.fertilizedFarmlandRichStable;
            }

            //noinspection ConstantConditions
            if (hasStableTrait && !hasRichTrait && hasHealthyTrait) {
                return ModBlocks.fertilizedFarmlandHealthyStable;
            }

            return null;
        }
    }

    private final FertilizerType fertilizerType;

    public FertilizerItem(Item.Properties properties, FertilizerType fertilizerType) {
        super(properties);
        this.fertilizerType = fertilizerType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack heldItem = context.getItemInHand();
        if (tryApplyFertilizerAt(level, pos) || tryApplyFertilizerAt(level, pos.below())) {
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    private boolean tryApplyFertilizerAt(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState newState = fertilizerType.applyFertilizer(state);
        if (newState != state) {
            world.setBlockAndUpdate(pos, newState);
            return true;
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(fertilizerType.getTooltip());
    }

}
