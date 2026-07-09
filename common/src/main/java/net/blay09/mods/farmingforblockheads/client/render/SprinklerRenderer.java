package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blay09.mods.farmingforblockheads.block.entity.SprinklerBlockEntity;
import net.blay09.mods.farmingforblockheads.client.ModRenderers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SprinklerRenderer implements BlockEntityRenderer<SprinklerBlockEntity, SprinklerRenderer.SprinklerRenderState> {

    private static final float ROTATION_SPEED = 20f;

    public static class SprinklerRenderState extends BlockEntityRenderState {
        public final List<BlockStateModelPart> rodParts = new ArrayList<>();
        public float rotation;
    }

    public SprinklerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SprinklerRenderState createRenderState() {
        return new SprinklerRenderState();
    }

    @Override
    public void extractRenderState(SprinklerBlockEntity blockEntity, SprinklerRenderState renderState, float delta, Vec3 vec, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        final var level = blockEntity.getLevel();
        renderState.rotation = level != null ? (level.getGameTime() + delta) * ROTATION_SPEED : 0f;
        renderState.rodParts.clear();
        ModRenderers.sprinklerRodModel.asBlockStateModel()
                .collectParts(RandomSource.create(blockEntity.getBlockPos().asLong()), renderState.rodParts);
    }

    @Override
    public void submit(SprinklerRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.wrapDegrees(renderState.rotation)));
        poseStack.translate(-0.5f, 0f, -0.5f);
        submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockItemSheet(), renderState.rodParts, BlockModelRenderState.EMPTY_TINTS, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
