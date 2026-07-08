package net.blay09.mods.farmingforblockheads.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.farmingforblockheads.ShippingBinSalesData;
import net.blay09.mods.farmingforblockheads.block.entity.ShippingBinBlockEntity;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.util.ShogiDuration;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public final class ShippingBinSalesEffects {

    private ShippingBinSalesEffects() {
    }

    public static void register(ShogiScope scope) {
        scope.registerEffect(TotalSold.IDENTIFIER, TotalSold.MAP_CODEC);
        scope.registerEffect(RecentlySold.IDENTIFIER, RecentlySold.mapCodec(scope), List.of("timespan"));
    }

    private static @Nullable SalesContext getSalesContext(ShogiContext context) {
        if (!(context.blockEntity() instanceof ShippingBinBlockEntity blockEntity)) {
            return null;
        }

        if (!(blockEntity.getLevel() instanceof ServerLevel level)) {
            return null;
        }

        final var recipe = context.getVariable("recipe")
                .filter(Identifier.class::isInstance)
                .map(Identifier.class::cast)
                .orElse(null);
        if (recipe == null) {
            return null;
        }

        return new SalesContext(level, level.getChunk(blockEntity.getBlockPos()).getPos(), recipe);
    }

    private record SalesContext(ServerLevel level, ChunkPos chunkPos, Identifier recipe) {
    }

    public record TotalSold() implements ShogiEffect<Long> {
        public static final Identifier IDENTIFIER = id("total_sold");
        public static final MapCodec<TotalSold> MAP_CODEC = MapCodec.unit(new TotalSold());

        @Override
        public Identifier identifier() {
            return IDENTIFIER;
        }

        @Override
        public Either<? extends Long, ?> apply(ShogiContext context) {
            final var salesContext = getSalesContext(context);
            if (salesContext == null) {
                return Either.right(ShogiDeferred.INSTANCE);
            }

            return Either.left(ShippingBinSalesData.get(salesContext.level()).getSoldAllTime(salesContext.chunkPos(), salesContext.recipe()));
        }
    }

    public record RecentlySold(ShogiEffect<?> timespan) implements ShogiEffect<Long> {
        public static final Identifier IDENTIFIER = id("recently_sold");

        public static MapCodec<RecentlySold> mapCodec(ShogiScope scope) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EffectArgumentCodecs.effectOrConstant(scope).fieldOf("timespan").forGetter(RecentlySold::timespan)
            ).apply(instance, RecentlySold::new));
        }

        @Override
        public Identifier identifier() {
            return IDENTIFIER;
        }

        @Override
        public Either<? extends Long, ?> apply(ShogiContext context) {
            final var salesContext = getSalesContext(context);
            if (salesContext == null) {
                return Either.right(ShogiDeferred.INSTANCE);
            }

            final var timespanTicks = timespan.apply(context)
                    .mapLeft(Coercion.DURATION)
                    .mapLeft(ShogiDuration::toTicks)
                    .orThrow();
            return Either.left(ShippingBinSalesData.get(salesContext.level()).getSoldInLast(salesContext.level(), salesContext.chunkPos(), salesContext.recipe(), timespanTicks));
        }
    }
}
