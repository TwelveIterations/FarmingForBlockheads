package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.entity.MerchantEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class MerchantRenderer extends MobRenderer<MerchantEntity, VillagerRenderState, VillagerModel> {

    private static final ResourceLocation MERCHANT_TEXTURE = ResourceLocation.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "textures/entity/merchant.png");
    private static final Map<ResourceLocation, ResourceLocation> verifiedTextures = new HashMap<>();

    public MerchantRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
    }

    @Override
    public ResourceLocation getTextureLocation(VillagerRenderState state) {
        ResourceLocation textureLocation = state instanceof MerchantRenderState merchantRenderState ? merchantRenderState.textureLocation : null;
        if (textureLocation == null) {
            return MERCHANT_TEXTURE;
        }

        return verifiedTextures.computeIfAbsent(textureLocation, it -> {
            if (Minecraft.getInstance().getResourceManager().getResource(it).isPresent()) {
                return it;
            }
            return MERCHANT_TEXTURE;
        });
    }

    @Override
    public void submit(VillagerRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        final var diggingAnimation = renderState instanceof MerchantRenderState merchantRenderState ? merchantRenderState.diggingAnimation : 0;
        if (diggingAnimation > 0) {
            poseStack.translate(0.0, -diggingAnimation * 0.05, 0.0);
        }
        super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
        poseStack.popPose();
    }

    @Override
    public void extractRenderState(MerchantEntity entity, VillagerRenderState state, float delta) {
        super.extractRenderState(entity, state, delta);
        if (state instanceof MerchantRenderState merchantRenderState) {
            merchantRenderState.textureLocation = entity.getTextureLocation();
            merchantRenderState.diggingAnimation = entity.getDiggingAnimation();
        }
    }

    @Override
    protected boolean shouldShowName(MerchantEntity entity, double distance) {
        return entity.getDiggingAnimation() <= 0 && super.shouldShowName(entity, distance);
    }

    @Override
    public MerchantRenderState createRenderState() {
        return new MerchantRenderState();
    }

}
