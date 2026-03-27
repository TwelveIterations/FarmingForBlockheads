package net.blay09.mods.farmingforblockheads.registry;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public record SimpleHolder<T>(Identifier id, T value) {
    public SimpleHolder(Map.Entry<Identifier, T> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public static <T> @Nullable SimpleHolder<T> of(Identifier id, @Nullable T marketCategory) {
        return marketCategory != null ? new SimpleHolder<>(id, marketCategory) : null;
    }
}
