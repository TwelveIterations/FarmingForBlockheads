package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blay09.mods.farmingforblockheads.block.ChickenNestBlock;
import net.blay09.mods.farmingforblockheads.block.entity.ChickenNestBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChickenNestRenderer implements BlockEntityRenderer<ChickenNestBlockEntity, ChickenNestRenderer.ChickenNestRenderState> {

    public static class ChickenNestRenderState extends BlockEntityRenderState {
        public Direction facing;
        public ItemStackRenderState item;
        public int count;
    }

    private final ItemStack EGG_STACK = new ItemStack(Items.EGG);
    private final float[] EGG_POSITIONS = new float[]{
            0.2f, 0, 0,
            -0.2f, 0, 0,
            0, 0, -0.1f,
            0, 0, 0.1f,
    };

    private final ItemModelResolver itemModelResolver;

    public ChickenNestRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ChickenNestRenderState createRenderState() {
        return new ChickenNestRenderState();
    }

    @Override
    public void extractRenderState(ChickenNestBlockEntity blockEntity, ChickenNestRenderState renderState, float delta, Vec3 vec, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = renderState.blockState.getValue(ChickenNestBlock.FACING);
        renderState.count = blockEntity.getEggCount();
        itemModelResolver.updateForTopItem(renderState.item, EGG_STACK, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) renderState.blockPos.asLong());
    }

    @Override
    public void submit(ChickenNestRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderState.facing.toYRot()));
        for (int i = 0; i < Math.min(EGG_POSITIONS.length / 3, renderState.count); i++) {
            poseStack.pushPose();
            poseStack.translate(EGG_POSITIONS[i * 3], 0.1f + EGG_POSITIONS[i * 3 + 1], -0.1f + EGG_POSITIONS[i * 3 + 2]);
            poseStack.mulPose(Axis.XP.rotationDegrees(45f));
            renderState.item.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
