package net.blay09.mods.farmingforblockheads.loot;

import net.blay09.mods.balm.world.level.storage.loot.BalmLootModifier;
import net.blay09.mods.balm.world.level.storage.loot.BalmLootTables;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.FarmlandHandler;
import net.blay09.mods.farmingforblockheads.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ModLootModifiers {
    public static void initialize(BalmLootTables lootTables) {
        lootTables.registerLootModifier(Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "rich_farmland"), new BalmLootModifier() {
            @Override
            public void apply(LootContext context, List<ItemStack> loot, @Nullable ResourceKey<LootTable> lootTableId) {
                Level level = context.getLevel();
                Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
                BlockState plant = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
                if (origin == null || plant == null || !(plant.getBlock() instanceof BonemealableBlock)) {
                    return;
                }

                BlockPos pos = BlockPos.containing(origin);
                // Other mods might trigger loot tables during world gen, which results in a deadlock when reading the
                // block state below as the world in the context is the server world, not the world gen region
                ChunkPos chunkPos = ChunkPos.containing(pos);
                if (!level.getChunkSource().hasChunk(chunkPos.x(), chunkPos.z())) {
                    return;
                }

                if (plant.getBlock() instanceof CropBlock crop && !crop.isMaxAge(plant)) {
                    return;
                }

                BlockPos posBelow = pos.below();
                BlockState farmland = level.getBlockState(posBelow);
                if (farmland.is(ModBlockTags.RICH_FARMLAND)) {
                    final var bonusCropChance = (float) FarmingForBlockheadsConfig.getActive().fertilizerBonusCropChance;
                    if (Math.random() <= bonusCropChance) {
                        loot.stream().filter(p -> !isProbablySeed(p)).findAny().ifPresent(c -> {
                            loot.add(c.copy());
                            level.levelEvent(2005, pos, 0);
                            FarmlandHandler.rollRegression(level, posBelow, farmland);
                        });
                    }
                }
            }
        });
    }

    private static boolean isProbablySeed(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath().contains("seed");
    }
}
