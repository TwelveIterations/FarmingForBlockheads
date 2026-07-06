package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blay09.mods.farmingforblockheads.block.entity.ShippingBinBlockEntity;
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

public class ShippingBinRenderer implements BlockEntityRenderer<ShippingBinBlockEntity, ShippingBinRenderer.ShippingBinRenderState> {

    private static final float[][] CONTENT_OFFSETS = new float[][]{
            {-0.24f, -0.22f, -0.22f},
            {0.05f, -0.20f, -0.24f},
            {0.24f, -0.23f, -0.05f},
            {-0.08f, -0.18f, 0.02f},
            {-0.24f, 0.02f, 0.19f},
            {0.19f, 0.00f, 0.22f},
            {-0.05f, 0.04f, 0.24f},
            {0.24f, 0.08f, 0.03f},
            {-0.19f, 0.24f, -0.05f},
            {0.03f, 0.28f, -0.19f},
            {0.21f, 0.24f, 0.10f},
            {-0.08f, 0.30f, 0.18f},
    };
    private static final float[] CONTENT_ROTATIONS = new float[]{
            -18f,
            12f,
            34f,
            -5f,
            21f,
            -30f,
            8f,
            -12f,
            28f,
            -36f,
            16f,
            -8f,
    };

    public static class ShippingBinRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState[] items = new ItemStackRenderState[ShippingBinBlockEntity.DISPLAYED_ITEM_SLOTS];
        public int count;

        public ShippingBinRenderState() {
            for (int i = 0; i < items.length; i++) {
                items[i] = new ItemStackRenderState();
            }
        }
    }

    private final ItemModelResolver itemModelResolver;

    public ShippingBinRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ShippingBinRenderState createRenderState() {
        return new ShippingBinRenderState();
    }

    @Override
    public void extractRenderState(ShippingBinBlockEntity blockEntity, ShippingBinRenderState renderState, float delta, Vec3 vec, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        renderState.count = ShippingBinBlockEntity.getDisplayedItemCount(blockEntity.getShipmentValue(), blockEntity.getShipmentCapacity());

        final var level = blockEntity.getLevel();
        for (int i = 0; i < renderState.count; i++) {
            final var seed = renderState.blockPos.asLong() + i;
            itemModelResolver.updateForTopItem(renderState.items[i], blockEntity.getDisplayedItems().get(i), ItemDisplayContext.FIXED, level, null, (int) seed);
        }
    }

    @Override
    public void submit(ShippingBinRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        for (int i = 0; i < renderState.count; i++) {
            final var offset = CONTENT_OFFSETS[i];
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(offset[0], offset[1], offset[2]);
            poseStack.mulPose(Axis.YP.rotationDegrees(CONTENT_ROTATIONS[i]));
            poseStack.mulPose(Axis.XP.rotationDegrees(i % 2 == 0 ? 62f : 72f));
            final var scale = 0.36f;
            poseStack.scale(scale, scale, scale);
            renderState.items[i].submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

}
