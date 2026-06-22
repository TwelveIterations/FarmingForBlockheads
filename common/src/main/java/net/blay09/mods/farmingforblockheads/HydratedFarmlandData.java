package net.blay09.mods.farmingforblockheads;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HydratedFarmlandData extends SavedData {

    private static final Codec<HydratedFarmlandData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("day").forGetter(HydratedFarmlandData::getLastDay),
            Codec.LONG.listOf().fieldOf("positions").forGetter(HydratedFarmlandData::getPositionList)
    ).apply(instance, HydratedFarmlandData::new));

    @SuppressWarnings("DataFlowIssue")
    private static final SavedDataType<HydratedFarmlandData> TYPE = new SavedDataType<>(
            FarmingForBlockheads.id("hydrated_farmland"),
            () -> new HydratedFarmlandData(-1, List.of()),
            CODEC,
            null
    );

    private long lastDay;
    private final Set<Long> positions = new HashSet<>();

    public HydratedFarmlandData(long lastDay, List<Long> positions) {
        this.lastDay = lastDay;
        this.positions.addAll(positions);
    }

    public static HydratedFarmlandData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isHydrated(ServerLevel level, BlockPos pos) {
        dailyReset(level);
        return positions.contains(pos.asLong());
    }

    public boolean hydrate(ServerLevel level, BlockPos pos) {
        dailyReset(level);
        if (positions.add(pos.asLong())) {
            setDirty();
            return true;
        }
        return false;
    }

    public void dailyReset(ServerLevel level) {
        final var currentDay = level.getOverworldClockTime() / 24000L;
        if (lastDay != currentDay) {
            lastDay = currentDay;
            if (!positions.isEmpty()) {
                positions.clear();
            }
            setDirty();
        }
    }

    private long getLastDay() {
        return lastDay;
    }

    private List<Long> getPositionList() {
        return List.copyOf(positions);
    }
}
