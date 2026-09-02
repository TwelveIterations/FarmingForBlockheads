package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.farmingforblockheads.entity.ShippingBalloonEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

public class ShippingBalloonRenderer extends EntityRenderer<ShippingBalloonEntity, ShippingBalloonRenderState> {

    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(id("shipping_balloon"), "main");

    private static final Identifier TEXTURE = id("textures/entity/shipping_balloon.png");
    private static final float INFLATION_DURATION_TICKS = 20f;
    private static final float INITIAL_SCALE = 0.12f;

    private final ShippingBalloonModel model;

    public ShippingBalloonRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new ShippingBalloonModel(context.bakeLayer(MODEL_LAYER));
        shadowRadius = 0.3f;
    }

    @Override
    public ShippingBalloonRenderState createRenderState() {
        return new ShippingBalloonRenderState();
    }

    @Override
    public void submit(ShippingBalloonRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        final float scale = getInflationScale(renderState.ageInTicks);
        poseStack.scale(scale, scale, scale);
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, RenderTypes.entityCutout(TEXTURE), renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    private static float getInflationScale(float ageInTicks) {
        final float progress = Mth.clamp(ageInTicks / INFLATION_DURATION_TICKS, 0f, 1f);
        final float easedProgress = progress * progress * progress * (progress * (progress * 6f - 15f) + 10f);
        return Mth.lerp(easedProgress, INITIAL_SCALE, 1f);
    }
}
