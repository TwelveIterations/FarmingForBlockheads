package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.farmingforblockheads.entity.FallingShippingCrateEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class FallingShippingCrateRenderer extends EntityRenderer<FallingShippingCrateEntity, EntityRenderState> {

    public static final ModelLayerLocation MODEL_LAYER = ShippingCrateRenderer.MODEL_LAYER;

    private static final Identifier TEXTURE = id("textures/entity/shipping_balloon.png");

    private final ShippingCrateModel model;

    public FallingShippingCrateRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new ShippingCrateModel(context.bakeLayer(MODEL_LAYER));
        shadowRadius = 0.3f;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, RenderTypes.entityCutout(TEXTURE), renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0, null);
        poseStack.popPose();

        super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
    }
}
