package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blay09.mods.farmingforblockheads.block.ModBlocks;
import net.blay09.mods.farmingforblockheads.block.RabbitTrapBlock;
import net.blay09.mods.farmingforblockheads.block.entity.RabbitTrapBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RabbitTrapRenderer implements BlockEntityRenderer<RabbitTrapBlockEntity, RabbitTrapRenderer.RabbitTrapRenderState> {

    private static final int FALL_TICKS = 5;
    private static final int WIGGLE_INTERVAL_TICKS = 120;
    private static final int WIGGLE_TICKS = 14;
    private static final float WIGGLE_DEGREES = 4f;

    public static class RabbitTrapRenderState extends BlockEntityRenderState {
        public final BlockModelRenderState crate = new BlockModelRenderState();
        public @Nullable BlockStateModel crateBreakingModel;
        public long crateBreakingSeed;
        public final ItemStackRenderState stick = new ItemStackRenderState();
        public final ItemStackRenderState carrot = new ItemStackRenderState();
        public Direction facing = Direction.NORTH;
        public boolean triggered;
        public float fallProgress;
        public float wiggle;
    }

    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;
    private @Nullable ItemStack stickStack;
    private @Nullable ItemStack carrotStack;

    public RabbitTrapRenderer(BlockEntityRendererProvider.Context context) {
        blockModelResolver = context.blockModelResolver();
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RabbitTrapRenderState createRenderState() {
        return new RabbitTrapRenderState();
    }

    @Override
    public void extractRenderState(RabbitTrapBlockEntity blockEntity, RabbitTrapRenderState renderState, float delta, Vec3 vec, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState().getValue(RabbitTrapBlock.FACING);
        renderState.triggered = blockEntity.getBlockState().getValue(RabbitTrapBlock.TRIGGERED);

        long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        long caughtGameTime = blockEntity.getCaughtGameTime();
        if (caughtGameTime >= 0) {
            renderState.fallProgress = Mth.clamp((gameTime - caughtGameTime + delta) / FALL_TICKS, 0f, 1f);
        } else if (!renderState.triggered && blockEntity.getSetupGameTime() >= 0) {
            renderState.fallProgress = 1f - Mth.clamp((gameTime - blockEntity.getSetupGameTime() + delta) / FALL_TICKS, 0f, 1f);
        } else {
            renderState.fallProgress = renderState.triggered ? 1f : 0f;
        }

        if (renderState.triggered && renderState.fallProgress >= 1f) {
            renderState.wiggle = getWiggle(blockEntity.getBlockPos().asLong(), gameTime, delta);
        } else {
            renderState.wiggle = 0f;
        }

        final var crateBlockState = ModBlocks.rabbitTrap.asBlock().defaultBlockState();
        blockModelResolver.update(renderState.crate, crateBlockState, BlockDisplayContext.create());
        if (renderState.breakProgress != null) {
            renderState.crateBreakingModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(crateBlockState);
            renderState.crateBreakingSeed = crateBlockState.getSeed(renderState.blockPos);
        } else {
            renderState.crateBreakingModel = null;
        }

        if (stickStack == null) {
            stickStack = new ItemStack(Items.STICK);
        }
        itemModelResolver.updateForTopItem(renderState.stick, stickStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) renderState.blockPos.asLong());
        if (carrotStack == null) {
            carrotStack = new ItemStack(Items.CARROT);
        }
        itemModelResolver.updateForTopItem(renderState.carrot, carrotStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) renderState.blockPos.asLong());
    }

    @Override
    public void submit(RabbitTrapRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-renderState.facing.toYRot()));
        poseStack.translate(-0.5f, 0f, -0.5f);

        renderCarrot(renderState, poseStack, submitNodeCollector);
        renderStick(renderState, poseStack, submitNodeCollector);
        renderCrate(renderState, poseStack, submitNodeCollector);

        poseStack.popPose();
    }

    private void renderCarrot(RabbitTrapRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.01f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(35f));
        poseStack.scale(0.3f, 0.3f, 0.3f);
        renderState.carrot.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private void renderStick(RabbitTrapRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        final float fallProgress = easeOut(renderState.fallProgress);
        poseStack.pushPose();
        poseStack.translate(0.5f, Mth.lerp(fallProgress, 0.225f, 0.045f), Mth.lerp(fallProgress, 0.2f, 0.36f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(fallProgress, 0f, 72f)));
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        renderState.stick.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private void renderCrate(RabbitTrapRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        final float fallProgress = easeOut(renderState.fallProgress);
        poseStack.pushPose();
        poseStack.translate(0.5f, Mth.lerp(fallProgress, 0.28f, 0.02f), 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(fallProgress, 45f, 0f)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderState.wiggle));
        poseStack.translate(-0.5f, 0.0f, -0.5f);
        renderState.crate.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        if (renderState.breakProgress != null && renderState.crateBreakingModel != null) {
            submitNodeCollector.submitBreakingBlockModel(poseStack, renderState.crateBreakingModel, renderState.crateBreakingSeed, renderState.breakProgress.progress());
        }
        poseStack.popPose();
    }

    private static float getWiggle(long seed, long gameTime, float delta) {
        final int offset = Math.floorMod(seed, WIGGLE_INTERVAL_TICKS);
        final float elapsed = Math.floorMod(gameTime + offset, WIGGLE_INTERVAL_TICKS) + delta;
        if (elapsed >= WIGGLE_TICKS) {
            return 0f;
        }

        final float fade = 1f - elapsed / WIGGLE_TICKS;
        return Mth.sin(elapsed * 1.7f) * fade * WIGGLE_DEGREES;
    }

    private static float easeOut(float value) {
        return 1f - (1f - value) * (1f - value);
    }
}
