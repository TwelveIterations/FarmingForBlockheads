package net.blay09.mods.farmingforblockheads.mixin;

import net.blay09.mods.farmingforblockheads.HydratedFarmlandData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void keepWateredFarmlandMoist(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callbackInfo) {
        if (HydratedFarmlandData.get(level).isHydrated(level, pos)) {
            if (state.getValue(FarmlandBlock.MOISTURE) < FarmlandBlock.MAX_MOISTURE) {
                level.setBlock(pos, state.setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE), 2);
            }
            callbackInfo.cancel();
        }
    }

    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable = true)
    private static void preventWateredFarmlandTurningToDirt(Entity entity, BlockState state, Level level, BlockPos pos, CallbackInfo callbackInfo) {
        if (level instanceof ServerLevel serverLevel && HydratedFarmlandData.get(serverLevel).isHydrated(serverLevel, pos)) {
            callbackInfo.cancel();
        }
    }
}
