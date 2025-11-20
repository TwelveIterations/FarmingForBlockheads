package net.blay09.mods.farmingforblockheads.client;

import net.blay09.mods.balm.client.renderer.blockentity.BalmBlockEntityRendererRegistrar;
import net.blay09.mods.balm.client.renderer.entity.BalmEntityRendererRegistrar;
import net.blay09.mods.farmingforblockheads.block.entity.ModBlockEntities;
import net.blay09.mods.farmingforblockheads.client.render.ChickenNestRenderer;
import net.blay09.mods.farmingforblockheads.client.render.FeedingTroughRenderer;
import net.blay09.mods.farmingforblockheads.client.render.MerchantRenderer;
import net.blay09.mods.farmingforblockheads.entity.ModEntities;

public class ModRenderers {
    public static void initialize(BalmEntityRendererRegistrar renderers) {
        renderers.register(ModEntities.merchant, MerchantRenderer::new);
    }

    public static void initialize(BalmBlockEntityRendererRegistrar renderers) {
        renderers.register(ModBlockEntities.chickenNest, ChickenNestRenderer::new);
        renderers.register(ModBlockEntities.feedingTrough, FeedingTroughRenderer::new);
    }
}
