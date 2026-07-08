package net.blay09.mods.farmingforblockheads;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

public class ShippingBinSalesData extends SavedData {

    private static final Codec<SaleRecord> SALE_RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("chunk").forGetter(SaleRecord::chunk),
            Identifier.CODEC.fieldOf("recipe").forGetter(SaleRecord::recipe),
            Codec.LONG.fieldOf("time").forGetter(SaleRecord::time),
            Codec.LONG.fieldOf("amount").forGetter(SaleRecord::amount)
    ).apply(instance, SaleRecord::new));

    private static final Codec<AllTimeRecord> ALL_TIME_RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("chunk").forGetter(AllTimeRecord::chunk),
            Identifier.CODEC.fieldOf("recipe").forGetter(AllTimeRecord::recipe),
            Codec.LONG.fieldOf("amount").forGetter(AllTimeRecord::amount)
    ).apply(instance, AllTimeRecord::new));

    private static final Codec<ShippingBinSalesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SALE_RECORD_CODEC.listOf().fieldOf("recentSales").forGetter(ShippingBinSalesData::recentSales),
            ALL_TIME_RECORD_CODEC.listOf().fieldOf("allTimeSales").forGetter(ShippingBinSalesData::allTimeSales)
    ).apply(instance, ShippingBinSalesData::new));

    @SuppressWarnings("DataFlowIssue")
    private static final SavedDataType<ShippingBinSalesData> TYPE = new SavedDataType<>(
            FarmingForBlockheads.id("shipping_bin_sales"),
            ShippingBinSalesData::new,
            CODEC,
            null
    );

    private final List<SaleRecord> recentSales = new ArrayList<>();
    private final List<AllTimeRecord> allTimeSales = new ArrayList<>();

    public ShippingBinSalesData() {
    }

    public ShippingBinSalesData(List<SaleRecord> recentSales, List<AllTimeRecord> allTimeSales) {
        this.recentSales.addAll(recentSales);
        this.allTimeSales.addAll(allTimeSales);
    }

    public static ShippingBinSalesData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void recordSale(ServerLevel level, ChunkPos chunkPos, Identifier recipeId, long amount) {
        final var chunk = chunkPos.pack();
        final var time = level.getGameTime();
        recentSales.add(new SaleRecord(chunk, recipeId, time, amount));

        for (int i = 0; i < allTimeSales.size(); i++) {
            final var record = allTimeSales.get(i);
            if (record.chunk == chunk && record.recipe.equals(recipeId)) {
                allTimeSales.set(i, new AllTimeRecord(chunk, recipeId, record.amount + amount));
                prune(level);
                setDirty();
                return;
            }
        }

        allTimeSales.add(new AllTimeRecord(chunk, recipeId, amount));
        prune(level);
        setDirty();
    }

    public long getSoldInLast(ServerLevel level, ChunkPos chunkPos, Identifier recipeId, long timespan) {
        final var minTime = level.getGameTime() - timespan;
        final var chunk = chunkPos.pack();
        return recentSales.stream()
                .filter(it -> it.time >= minTime && it.chunk == chunk && it.recipe.equals(recipeId))
                .mapToLong(SaleRecord::amount)
                .sum();
    }

    public long getSoldAllTime(ChunkPos chunkPos, Identifier recipeId) {
        final var chunk = chunkPos.pack();
        return allTimeSales.stream()
                .filter(it -> it.chunk == chunk && it.recipe.equals(recipeId))
                .mapToLong(AllTimeRecord::amount)
                .sum();
    }

    public void prune(ServerLevel level) {
        final var minTime = level.getGameTime() - 1728000L;
        if (recentSales.removeIf(it -> it.time < minTime)) {
            setDirty();
        }
    }

    private List<SaleRecord> recentSales() {
        return recentSales;
    }

    private List<AllTimeRecord> allTimeSales() {
        return allTimeSales;
    }

    public record SaleRecord(long chunk, Identifier recipe, long time, long amount) {
    }

    public record AllTimeRecord(long chunk, Identifier recipe, long amount) {
    }
}
