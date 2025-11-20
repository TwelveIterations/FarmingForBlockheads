package net.blay09.mods.farmingforblockheads.registry;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MarketDefaultsLoader implements ResourceManagerReloadListener {

    private static final FileToIdConverter MARKET_DEFAULTS = new FileToIdConverter("farmingforblockheads", "defaults.json");

    private final HolderLookup.Provider registries;

    public MarketDefaultsLoader(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        final var registry = MarketDefaultsRegistry.INSTANCE;
        registry.clear();

        for (final var entry : MARKET_DEFAULTS.listMatchingResources(resourceManager).entrySet()) {
            try (final var reader = entry.getValue().openAsReader()) {
                registry.loadAdditionally(registries, reader);
            } catch (Exception e) {
                FarmingForBlockheads.logger.error("Error loading Farming for Blockheads market defaults file at {}", entry.getKey(), e);
            }
        }

        final var configFile = new File(Balm.config().getConfigDir(), "farmingforblockheads/defaults.json");
        if (configFile.exists()) {
            try (final var reader = Files.newBufferedReader(configFile.toPath())) {
                registry.loadAdditionally(registries, reader);
            } catch (Exception e) {
                FarmingForBlockheads.logger.error("Error loading Farming for Blockheads market defaults file at {}", configFile, e);
            }
        }
    }
}
