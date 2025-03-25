package net.blay09.mods.farmingforblockheads.client;

import net.blay09.mods.balm.api.client.rendering.BalmRenderers;
import net.blay09.mods.farmingforblockheads.block.entity.ModBlockEntities;
import net.blay09.mods.farmingforblockheads.client.render.ChickenNestRenderer;
import net.blay09.mods.farmingforblockheads.client.render.FeedingTroughRenderer;
import net.blay09.mods.farmingforblockheads.client.render.MerchantRenderer;
import net.blay09.mods.farmingforblockheads.entity.ModEntities;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ModRenderers {
    public static void initialize(BalmRenderers renderers) {
        renderers.registerEntityRenderer(id("merchant"), ModEntities.merchant::get, MerchantRenderer::new);

        renderers.registerBlockEntityRenderer(id("chicken_nest"), ModBlockEntities.chickenNest::get, ChickenNestRenderer::new);
        renderers.registerBlockEntityRenderer(id("feeding_trough"), ModBlockEntities.feedingTrough::get, FeedingTroughRenderer::new);
    }
}
