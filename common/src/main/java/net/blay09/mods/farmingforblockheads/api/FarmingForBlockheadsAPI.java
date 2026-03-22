package net.blay09.mods.farmingforblockheads.api;

import net.minecraft.resources.Identifier;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

public class FarmingForBlockheadsAPI {

    private static final InternalMethods internalMethods = loadInternalMethods();

    private static InternalMethods loadInternalMethods() {
        try {
            return (InternalMethods) Class.forName("net.blay09.mods.farmingforblockheads.InternalMethodsImpl").getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException("Failed to load Farming for Blockheads API", e);
        }
    }

    public static Optional<MarketCategory> getMarketCategory(Identifier registryName) {
        return internalMethods.getMarketCategory(registryName);
    }

    public static Map<Identifier, MarketCategory> getMarketCategories() {
        return internalMethods.getMarketCategories();
    }
}
