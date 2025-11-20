package net.blay09.mods.farmingforblockheads.registry;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record SimpleHolder<T>(Identifier id, T value) {
    public SimpleHolder(Map.Entry<Identifier, T> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public static <T> SimpleHolder<T> of(Identifier id, @Nullable T marketCategory) {
        if (marketCategory == null) {
            return null;
        }
        return new SimpleHolder<>(id, marketCategory);
    }
}
