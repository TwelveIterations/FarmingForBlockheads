package net.blay09.mods.farmingforblockheads.client;

import net.blay09.mods.balm.client.model.geom.BalmModelLayerRegistrar;
import net.blay09.mods.balm.client.renderer.block.model.BalmBlockStateModelRegistrar;
import net.blay09.mods.balm.client.renderer.block.model.DeferredBlockStateModel;
import net.blay09.mods.balm.client.renderer.blockentity.BalmBlockEntityRendererRegistrar;
import net.blay09.mods.balm.client.renderer.entity.BalmEntityRendererRegistrar;
import net.blay09.mods.farmingforblockheads.block.entity.ModBlockEntities;
import net.blay09.mods.farmingforblockheads.client.render.*;
import net.blay09.mods.farmingforblockheads.entity.ModEntities;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModRenderers {
    public static DeferredBlockStateModel sprinklerRodModel;

    public static void initialize(BalmEntityRendererRegistrar renderers) {
        renderers.register(ModEntities.merchant, MerchantRenderer::new);
        renderers.register(ModEntities.shippingBalloon, ShippingBalloonRenderer::new);
        renderers.register(ModEntities.shippingCrate, ShippingCrateRenderer::new);
        renderers.register(ModEntities.courier, CourierRenderer::new);
        renderers.register(ModEntities.fallingShippingCrate, FallingShippingCrateRenderer::new);
    }

    public static void initialize(BalmBlockEntityRendererRegistrar renderers) {
        renderers.register(ModBlockEntities.chickenNest, ChickenNestRenderer::new);
        renderers.register(ModBlockEntities.rabbitTrap, RabbitTrapRenderer::new);
        renderers.register(ModBlockEntities.feedingTrough, FeedingTroughRenderer::new);
        renderers.register(ModBlockEntities.shippingBin, ShippingBinRenderer::new);
        renderers.register(ModBlockEntities.sprinkler, SprinklerRenderer::new);
        renderers.register(ModBlockEntities.fishingBarrel, FishingBarrelRenderer::new);
    }

    public static void initialize(BalmModelLayerRegistrar modelLayers) {
        modelLayers.register(ShippingBalloonRenderer.MODEL_LAYER.model(), ShippingBalloonRenderer.MODEL_LAYER.layer(), ShippingBalloonModel::createBodyLayer);
        modelLayers.register(ShippingCrateRenderer.MODEL_LAYER.model(), ShippingCrateRenderer.MODEL_LAYER.layer(), ShippingCrateModel::createBodyLayer);
    }

    public static void initialize(BalmBlockStateModelRegistrar models) {
        sprinklerRodModel = models.register(id("block/sprinkler_rod"));
    }
}
