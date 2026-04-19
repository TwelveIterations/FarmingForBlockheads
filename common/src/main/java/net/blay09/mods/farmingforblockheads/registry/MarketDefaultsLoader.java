package net.blay09.mods.farmingforblockheads.registry;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.api.MarketDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MarketDefaultsLoader implements PreparableReloadListener {

    private static final FileToIdConverter MARKET_DEFAULTS = new FileToIdConverter("farmingforblockheads", "defaults.json");

    private final HolderLookup.Provider registries;

    public MarketDefaultsLoader(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    private void loadDefaults(ResourceManager resourceManager) {
        final var defaultsByGroup = new HashMap<String, MarketDefault>();
        for (final var entry : MARKET_DEFAULTS.listMatchingResources(resourceManager).entrySet()) {
            try (final var reader = entry.getValue().openAsReader()) {
                MarketDefaultsRegistry.loadAdditionally(defaultsByGroup, registries, reader);
            } catch (Exception e) {
                FarmingForBlockheads.logger.error("Error loading Farming for Blockheads market defaults file at {}", entry.getKey(), e);
            }
        }

        final var configFile = new File(Balm.config().getConfigDir(), "farmingforblockheads/defaults.json");
        if (configFile.exists()) {
            try (final var reader = Files.newBufferedReader(configFile.toPath())) {
                MarketDefaultsRegistry.loadAdditionally(defaultsByGroup, registries, reader);
            } catch (Exception e) {
                FarmingForBlockheads.logger.error("Error loading Farming for Blockheads market defaults file at {}", configFile, e);
            }
        }

        MarketDefaultsRegistry.INSTANCE.replace(defaultsByGroup);
    }

    @Override
    public CompletableFuture<Void> reload(SharedState sharedState, Executor preparationExecutor, PreparationBarrier preparationBarrier, Executor reloadExecutor) {
        return CompletableFuture.runAsync(() -> loadDefaults(sharedState.resourceManager()), preparationExecutor)
                .thenCompose(preparationBarrier::wait);
    }
}
