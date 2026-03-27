package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blay09.mods.farmingforblockheads.block.entity.FeedingTroughBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FeedingTroughRenderer implements BlockEntityRenderer<FeedingTroughBlockEntity, FeedingTroughRenderer.FeedingTroughRenderState> {

    public static class FeedingTroughRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public int count;
    }

    private final float[] CONTENT_POSITIONS = new float[]{
            0.2f, 0.01f, 0,
            -0.18f, 0, 0,
            0, -0.01f, -0.18f,
            0, -0.02f, 0.18f,
    };

    private final ItemModelResolver itemModelResolver;

    public FeedingTroughRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public FeedingTroughRenderState createRenderState() {
        return new FeedingTroughRenderState();
    }

    @Override
    public void extractRenderState(FeedingTroughBlockEntity blockEntity, FeedingTroughRenderState renderState, float delta, Vec3 vec, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        final var level = blockEntity.getLevel();
        final var itemStack = blockEntity.getContentStack();
        renderState.count = itemStack.getCount();
        itemModelResolver.updateForTopItem(renderState.item, itemStack, ItemDisplayContext.FIXED, level, null, (int) renderState.blockPos.asLong());
    }

    @Override
    public void submit(FeedingTroughRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        float x = 0;
        float y = 0;
        float z = 0;
        for (int i = 0; i < Math.max(1, Math.min(CONTENT_POSITIONS.length / 3, renderState.count / 12)); i++) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(x + CONTENT_POSITIONS[i * 3], y + CONTENT_POSITIONS[i * 3 + 1], z + CONTENT_POSITIONS[i * 3 + 2]);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            final var scale = 0.5f;
            poseStack.scale(scale, scale, scale);
            renderState.item.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
