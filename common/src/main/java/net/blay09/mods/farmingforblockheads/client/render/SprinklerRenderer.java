package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.farmingforblockheads.block.entity.SprinklerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SprinklerRenderer implements BlockEntityRenderer<SprinklerBlockEntity, SprinklerRenderer.SprinklerRenderState> {

    public static class SprinklerRenderState extends BlockEntityRenderState {
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
    }

    @Override
    public void submit(SprinklerRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
    }
}
