package net.blay09.mods.farmingforblockheads.client;

import net.blay09.mods.balm.client.model.geom.BalmModelLayerRegistrar;
import net.blay09.mods.balm.client.renderer.blockentity.BalmBlockEntityRendererRegistrar;
import net.blay09.mods.balm.client.renderer.entity.BalmEntityRendererRegistrar;
import net.blay09.mods.farmingforblockheads.block.entity.ModBlockEntities;
import net.blay09.mods.farmingforblockheads.client.render.*;
import net.blay09.mods.farmingforblockheads.entity.ModEntities;

public class ModRenderers {
    public static void initialize(BalmEntityRendererRegistrar renderers) {
        renderers.register(ModEntities.merchant, MerchantRenderer::new);
        renderers.register(ModEntities.shippingBalloon, ShippingBalloonRenderer::new);
        renderers.register(ModEntities.shippingCrate, ShippingCrateRenderer::new);
        renderers.register(ModEntities.courier, CourierRenderer::new);
        renderers.register(ModEntities.fallingShippingCrate, FallingShippingCrateRenderer::new);
    }

    public static void initialize(BalmBlockEntityRendererRegistrar renderers) {
        renderers.register(ModBlockEntities.chickenNest, ChickenNestRenderer::new);
        renderers.register(ModBlockEntities.feedingTrough, FeedingTroughRenderer::new);
        renderers.register(ModBlockEntities.shippingBin, ShippingBinRenderer::new);
    }

    public static void initialize(BalmModelLayerRegistrar modelLayers) {
        modelLayers.register(ShippingBalloonRenderer.MODEL_LAYER.model(), ShippingBalloonRenderer.MODEL_LAYER.layer(), ShippingBalloonModel::createBodyLayer);
        modelLayers.register(ShippingCrateRenderer.MODEL_LAYER.model(), ShippingCrateRenderer.MODEL_LAYER.layer(), ShippingCrateModel::createBodyLayer);
    }
}
