package net.blay09.mods.farmingforblockheads.mixin;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.HydratedFarmlandData;
import net.blay09.mods.farmingforblockheads.block.ModPoiTypes;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {

    @WrapOperation(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FarmlandBlock;isNearWater(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean isNearWater(LevelReader level, BlockPos pos, Operation<Boolean> original) {
        return !FarmingForBlockheadsConfig.getActive().disableVanillaWatering
                && original.call(level, pos);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void keepWateredFarmlandMoist(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callbackInfo) {
        if (HydratedFarmlandData.get(level).isHydrated(level, pos)) {
            if (state.getValue(FarmlandBlock.MOISTURE) > 0 && state.getValue(FarmlandBlock.MOISTURE) < FarmlandBlock.MAX_MOISTURE) {
                level.setBlock(pos, state.setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE), 2);
            }
            callbackInfo.cancel();
        }
    }

    @WrapOperation(
            method = "fallOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FarmlandBlock;turnToBaseBlock(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void preventFarmlandTrampling(FarmlandBlock farmland, Entity sourceEntity, BlockState state, Level level, BlockPos pos, Operation<Void> original) {
        if (!(level instanceof ServerLevel serverLevel) || !ModPoiTypes.hasNearbySprinklerWithHead(serverLevel, pos)) {
            original.call(farmland, sourceEntity, state, level, pos);
        }
    }

    @WrapOperation(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FarmlandBlock;turnToBaseBlock(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void preventFarmlandRegression(FarmlandBlock farmland, @Nullable Entity sourceEntity, BlockState state, Level level, BlockPos pos, Operation<Void> original) {
        if (!(level instanceof ServerLevel serverLevel) || !ModPoiTypes.hasNearbyWaterSprinkler(serverLevel, pos)) {
            original.call(farmland, sourceEntity, state, level, pos);
        }
    }
}
