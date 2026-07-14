package net.blay09.mods.farmingforblockheads.fabric.datagen;

import net.blay09.mods.farmingforblockheads.tag.ModEntityTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {
    private static final ResourceKey<EntityType<?>> ARMADILLO = key("armadillo");
    private static final ResourceKey<EntityType<?>> CAT = key("cat");
    private static final ResourceKey<EntityType<?>> CHICKEN = key("chicken");
    private static final ResourceKey<EntityType<?>> FROG = key("frog");
    private static final ResourceKey<EntityType<?>> RABBIT = key("rabbit");

    public ModEntityTypeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        builder(ModEntityTypeTags.RABBIT_TRAP_CAPTURABLE).add(
                ARMADILLO,
                CAT,
                CHICKEN,
                FROG,
                RABBIT
        );
        builder(ModEntityTypeTags.RABBIT_TRAP_SPAWNABLE).add(
                ARMADILLO,
                CAT,
                CHICKEN,
                FROG,
                RABBIT
        );
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace(name));
    }
}
