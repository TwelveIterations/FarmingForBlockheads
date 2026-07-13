package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.tag.ModEntityTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityTypeIds;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {
    public ModEntityTypeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        builder(ModEntityTypeTags.RABBIT_TRAP_CAPTURABLE).add(
                EntityTypeIds.ARMADILLO,
                EntityTypeIds.CAT,
                EntityTypeIds.CHICKEN,
                EntityTypeIds.FROG,
                EntityTypeIds.RABBIT
        );
        builder(ModEntityTypeTags.RABBIT_TRAP_SPAWNABLE).add(
                EntityTypeIds.ARMADILLO,
                EntityTypeIds.CAT,
                EntityTypeIds.CHICKEN,
                EntityTypeIds.FROG,
                EntityTypeIds.RABBIT
        );
    }
}
