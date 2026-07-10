package net.blay09.mods.farmingforblockheads.block;

import net.blay09.mods.balm.world.entity.ai.village.poi.BalmPoiTypeRegistrar;
import net.blay09.mods.farmingforblockheads.block.entity.SprinklerBlockEntity;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.Set;
import java.util.stream.Stream;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModPoiTypes {

    public static final ResourceKey<PoiType> SPRINKLER = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, id("sprinkler"));

    public static Holder<PoiType> sprinkler;

    public static void initialize(BalmPoiTypeRegistrar poiTypes) {
        sprinkler = poiTypes.register("sprinkler", () -> new PoiType(Set.copyOf(ModBlocks.sprinkler.asBlock().getStateDefinition().getPossibleStates()), 0, 1));
    }

    public static Stream<BlockPos> findNearbySprinklers(ServerLevel level, BlockPos pos) {
        return level.getPoiManager()
                .getInSquare(it -> it.is(SPRINKLER), pos, SprinklerBlockEntity.RANGE, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(sprinklerPos -> sprinklerPos.getY() > pos.getY() && sprinklerPos.getY() <= pos.getY() + 1);
    }

    public static Stream<BlockPos> findNearbyWaterSprinklers(ServerLevel level, BlockPos pos) {
        return findNearbySprinklers(level, pos)
                .filter(it -> !isNonWaterSprinkler(level, it));
    }

    public static boolean hasNearbyWaterSprinkler(ServerLevel level, BlockPos pos) {
        return findNearbyWaterSprinklers(level, pos).findAny().isPresent();
    }

    public static boolean hasNearbySprinklerWithHead(ServerLevel level, BlockPos pos) {
        return findNearbyWaterSprinklers(level, pos)
                .anyMatch(sprinklerPos -> level.getBlockEntity(sprinklerPos) instanceof SprinklerBlockEntity sprinklerBlockEntity && sprinklerBlockEntity.hasHead());
    }

    private static boolean isNonWaterSprinkler(ServerLevel level, BlockPos sprinklerPos) {
        final var stateBelow = level.getBlockState(sprinklerPos.below());
        return stateBelow.is(ModBlockTags.LAVA_SPRINKLER_BASE)
                || stateBelow.is(ModBlockTags.HONEY_SPRINKLER_BASE)
                || stateBelow.is(ModBlockTags.SLIME_SPRINKLER_BASE)
                || stateBelow.is(ModBlockTags.SNOW_SPRINKLER_BASE)
                || stateBelow.is(ModBlockTags.SULFUR_SPRINKLER_BASE);
    }
}
