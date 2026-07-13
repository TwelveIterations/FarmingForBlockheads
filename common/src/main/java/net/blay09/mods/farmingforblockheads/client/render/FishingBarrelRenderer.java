package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.blay09.mods.farmingforblockheads.block.entity.FishingBarrelBlockEntity;
import net.blay09.mods.farmingforblockheads.mixin.ConditionalItemModelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class FishingBarrelRenderer implements BlockEntityRenderer<FishingBarrelBlockEntity, FishingBarrelRenderer.FishingBarrelRenderState> {
    private static final Identifier BOBBER_TEXTURE = Identifier.withDefaultNamespace("textures/entity/fishing/fishing_hook.png");
    private static final int MAX_RENDERED_CATCH_ITEMS = 4;
    private static final float CATCH_ITEM_LAUNCH_PROGRESS = 0.1347f;
    private static final float CATCH_ITEM_LAUNCH_ELAPSED = FishingBarrelBlockEntity.CATCH_BITE_TICKS + FishingBarrelBlockEntity.CATCH_RETRIEVE_TICKS * CATCH_ITEM_LAUNCH_PROGRESS;
    private static final float CATCH_ITEM_FLIGHT_TICKS = 10f;
    private static final float CATCH_ITEM_FLIGHT_END_ELAPSED = CATCH_ITEM_LAUNCH_ELAPSED + CATCH_ITEM_FLIGHT_TICKS;
    private static final float CATCH_ITEM_ARC_HEIGHT = 0.62f;
    private static final float[][] CONTENT_OFFSETS = new float[][]{
            {-0.24f, -0.22f, -0.22f},
            {0.05f, -0.20f, -0.24f},
            {0.24f, -0.23f, -0.05f},
            {-0.08f, -0.18f, 0.02f},
            {-0.24f, 0.02f, 0.19f},
            {0.19f, 0.00f, 0.22f},
            {-0.05f, 0.04f, 0.24f},
            {0.24f, 0.08f, 0.03f},
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
    };

    public static class FishingBarrelRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState rod = new ItemStackRenderState();
        public final ItemStackRenderState[] catchItems = new ItemStackRenderState[MAX_RENDERED_CATCH_ITEMS];
        public final ItemStackRenderState[] contentItems = new ItemStackRenderState[FishingBarrelBlockEntity.STORAGE_SLOTS];
        public Direction facing = Direction.NORTH;
        public boolean hasRod;
        public boolean hasWater;
        public int catchItemCount;
        public int contentItemCount;
        public int catchAnimationTicks;
        public float partialTicks;
        public long gameTime;

        public FishingBarrelRenderState() {
            for (int i = 0; i < catchItems.length; i++) {
                catchItems[i] = new ItemStackRenderState();
            }
            for (int i = 0; i < contentItems.length; i++) {
                contentItems[i] = new ItemStackRenderState();
            }
        }
    }

    private final ItemModelResolver itemModelResolver;

    public FishingBarrelRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public FishingBarrelRenderState createRenderState() {
        return new FishingBarrelRenderState();
    }

    @Override
    public void extractRenderState(FishingBarrelBlockEntity blockEntity, FishingBarrelRenderState renderState, float delta, Vec3 vec, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = blockEntity.getFacing();
        renderState.hasRod = blockEntity.hasFishingRod();
        renderState.catchAnimationTicks = blockEntity.getCatchAnimationTicks();
        renderState.partialTicks = delta;

        final var level = blockEntity.getLevel();
        if (level != null) {
            final var fishingPos = renderState.blockPos.relative(renderState.facing).below();
            renderState.hasWater = level.getFluidState(fishingPos).is(FluidTags.WATER);
            renderState.gameTime = level.getGameTime();
        } else {
            renderState.hasWater = false;
            renderState.gameTime = 0;
        }

        if (renderState.hasRod) {
            final var rodStack = blockEntity.getContainer().getItem(FishingBarrelBlockEntity.ROD_SLOT);
            final var castModel = getCastFishingRodModel(rodStack);
            if (castModel != null) {
                renderState.rod.clear();
                castModel.update(renderState.rod,
                        rodStack,
                        itemModelResolver,
                        ItemDisplayContext.NONE,
                        level instanceof ClientLevel clientLevel ? clientLevel : null,
                        null,
                        (int) renderState.blockPos.asLong());
            } else {
                itemModelResolver.updateForTopItem(renderState.rod, rodStack, ItemDisplayContext.NONE, level, null, (int) renderState.blockPos.asLong());
            }
        } else {
            renderState.rod.clear();
        }

        final List<ItemStack> pendingCatchItems = blockEntity.getPendingCatchItems();
        renderState.catchItemCount = Math.min(MAX_RENDERED_CATCH_ITEMS, pendingCatchItems.size());
        for (int i = 0; i < renderState.catchItemCount; i++) {
            itemModelResolver.updateForTopItem(renderState.catchItems[i], pendingCatchItems.get(i), ItemDisplayContext.FIXED, level, null, (int) renderState.blockPos.asLong() + i + 31);
        }
        for (int i = renderState.catchItemCount; i < renderState.catchItems.length; i++) {
            renderState.catchItems[i].clear();
        }

        renderState.contentItemCount = 0;
        final var container = blockEntity.getContainer();
        for (int slot = 0; slot < FishingBarrelBlockEntity.STORAGE_SLOTS; slot++) {
            final var itemStack = container.getItem(FishingBarrelBlockEntity.ROD_SLOT + 1 + slot);
            if (itemStack.isEmpty()) {
                continue;
            }

            final var index = renderState.contentItemCount++;
            itemModelResolver.updateForTopItem(renderState.contentItems[index],
                    itemStack,
                    ItemDisplayContext.FIXED,
                    level,
                    null,
                    (int) renderState.blockPos.asLong() + index + 47);
        }
        for (int i = renderState.contentItemCount; i < renderState.contentItems.length; i++) {
            renderState.contentItems[i].clear();
        }
    }

    @Override
    public void submit(FishingBarrelRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        submitContentItems(renderState, poseStack, submitNodeCollector);

        if (!renderState.hasRod || !renderState.hasWater) {
            return;
        }

        final Vec3 rodBase = getRodBase(renderState.facing);
        final Vec3 rodTip = getRodTip(renderState);
        final Vec3 bobber = getBobberPosition(renderState);
        final Vec3 bobberLineAttachment = bobber.add(0, 0.15, 0);

        submitLine(poseStack, submitNodeCollector, rodTip, bobberLineAttachment, 0xff1c1712);
        submitRodItem(renderState, poseStack, submitNodeCollector, rodBase);
        submitBobber(renderState, poseStack, submitNodeCollector, cameraRenderState, bobber);
        submitCatchItems(renderState, poseStack, submitNodeCollector, bobber);
    }

    private static void submitContentItems(FishingBarrelRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        for (int i = 0; i < renderState.contentItemCount; i++) {
            final var offset = CONTENT_OFFSETS[i];
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(offset[0], offset[1], offset[2]);
            poseStack.mulPose(Axis.YP.rotationDegrees(CONTENT_ROTATIONS[i]));
            poseStack.mulPose(Axis.XP.rotationDegrees(i % 2 == 0 ? 62f : 72f));
            final var scale = 0.36f;
            poseStack.scale(scale, scale, scale);
            renderState.contentItems[i].submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private static @Nullable ItemModel getCastFishingRodModel(ItemStack rodStack) {
        final var itemModelId = rodStack.get(DataComponents.ITEM_MODEL);
        if (itemModelId == null) {
            return null;
        }

        final var itemModel = Minecraft.getInstance().getModelManager().getItemModel(itemModelId);
        if (itemModel instanceof ConditionalItemModel conditionalItemModel) {
            final var accessor = (ConditionalItemModelAccessor) conditionalItemModel;
            if (accessor.getProperty() instanceof FishingRodCast) {
                return accessor.getOnTrue();
            }
        }

        return null;
    }

    private static Vec3 getRodBase(Direction facing) {
        final var forward = 1 / 16f * 9;
        return new Vec3(0.5 + facing.getStepX() * forward, 1 + 3 / 16f, 0.5 + facing.getStepZ() * forward);
    }

    private static Vec3 getRodTip(FishingBarrelRenderState renderState) {
        final double forward = 1 / 32f * 31f;
        final float elapsed = getCatchAnimationElapsed(renderState);
        final double pullBack = elapsed > 0f ? getCatchPullIntensity(elapsed) * 0.11 : 0;
        return new Vec3(0.5 + renderState.facing.getStepX() * (forward - pullBack), 1.5, 0.5 + renderState.facing.getStepZ() * (forward - pullBack));
    }

    private static Vec3 getBobberPosition(FishingBarrelRenderState state) {
        final float elapsed = getCatchAnimationElapsed(state);
        return getBobberPosition(state, elapsed);
    }

    private static Vec3 getBobberPosition(FishingBarrelRenderState state, float elapsed) {
        final float forward = 1 / 32f * 31f;
        final float retrieve = getCatchRetrieveProgress(elapsed);
        final float elasticRetrieve = getCatchElasticRetrieveProgress(elapsed);
        final float lower = getCatchLowerProgress(elapsed);
        final float retrieved = elasticRetrieve * (1f - lower);
        final float lift = retrieved * 0.7f;
        final float pullBack = retrieved * 0.2f;
        final Vec3 water = new Vec3(0.5 + state.facing.getStepX() * (forward - pullBack), -0.1f + lift, 0.5 + state.facing.getStepZ() * (forward - pullBack));
        final float wave = Mth.sin((state.gameTime + state.partialTicks) * 0.22f) * 0.025f;
        return water.add(0, wave * (1f - retrieve) * (1f - lower), 0);
    }

    private static void submitLine(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 from, Vec3 to, int color) {
        final float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
        final Vec3 delta = to.subtract(from);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            lineVertex(buffer, pose, from, delta, 0f, 1f, color, width);
            lineVertex(buffer, pose, from, delta, 1f, 0f, color, width);
        });
    }

    private static void lineVertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 from, Vec3 delta, float progress, float nextProgress, int color, float width) {
        final Vec3 point = from.add(delta.scale(progress));
        Vec3 normal = delta.scale(nextProgress - progress).normalize();
        if (normal.lengthSqr() <= 0.0001) {
            normal = new Vec3(0, 1, 0);
        }

        buffer.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(color)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z)
                .setLineWidth(width);
    }

    private static void submitBobber(FishingBarrelRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, Vec3 bobber) {
        poseStack.pushPose();
        poseStack.translate(bobber.x, bobber.y, bobber.z);
        poseStack.scale(0.35f, 0.35f, 0.35f);
        poseStack.mulPose(cameraRenderState.orientation);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(BOBBER_TEXTURE), (pose, buffer) -> {
            bobberVertex(buffer, pose, renderState.lightCoords, -0.5f, -0.5f, 0, 1);
            bobberVertex(buffer, pose, renderState.lightCoords, 0.5f, -0.5f, 1, 1);
            bobberVertex(buffer, pose, renderState.lightCoords, 0.5f, 0.5f, 1, 0);
            bobberVertex(buffer, pose, renderState.lightCoords, -0.5f, 0.5f, 0, 0);
        });
        poseStack.popPose();
    }

    private static void bobberVertex(VertexConsumer buffer, PoseStack.Pose pose, int lightCoords, float x, float y, int u, int v) {
        buffer.addVertex(pose, x, y, 0)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0, 1, 0);
    }

    private static void submitRodItem(FishingBarrelRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 rodBase) {
        poseStack.pushPose();
        poseStack.translate(rodBase.x, rodBase.y, rodBase.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(270 - renderState.facing.toYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(getRodCatchRotation(renderState)));
        final float scale = 1f;
        final float forwardScale = getRodCatchForwardScale(renderState);
        poseStack.scale(forwardScale, scale, scale);
        renderState.rod.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static float getRodCatchRotation(FishingBarrelRenderState renderState) {
        final float elapsed = getCatchAnimationElapsed(renderState);
        if (elapsed <= 0f) {
            return 0f;
        }

        final float pullIntensity = getCatchPullIntensity(elapsed);
        final float pull = pullIntensity * 9f;
        final float wiggleFade = 1f - getCatchRetrieveProgress(elapsed);
        final float wiggle = Mth.sin(elapsed * 1.4f) * pullIntensity * wiggleFade * 5f;
        return pull + wiggle;
    }

    private static float getRodCatchForwardScale(FishingBarrelRenderState renderState) {
        final float elapsed = getCatchAnimationElapsed(renderState);
        if (elapsed <= 0f) {
            return 1f;
        }

        final float pullIntensity = Mth.clamp(getCatchPullIntensity(elapsed), 0f, 1f);
        final float pulseFade = 1f - getCatchRetrieveProgress(elapsed);
        final float pulse = Mth.abs(Mth.sin(elapsed * 1.4f)) * pulseFade * 0.035f;
        return 1f - pullIntensity * (0.075f + pulse);
    }

    private static float getCatchAnimationElapsed(FishingBarrelRenderState renderState) {
        if (renderState.catchAnimationTicks <= 0) {
            return 0f;
        }

        final float remaining = Math.max(0f, renderState.catchAnimationTicks - renderState.partialTicks);
        return FishingBarrelBlockEntity.CATCH_ANIMATION_TICKS - remaining;
    }

    private static float getCatchPullIntensity(float elapsed) {
        final float retrieved = getCatchElasticRetrieveProgress(elapsed) * (1f - getCatchLowerProgress(elapsed));
        if (retrieved > 0f) {
            return retrieved;
        }

        if (elapsed > FishingBarrelBlockEntity.CATCH_BITE_TICKS + FishingBarrelBlockEntity.CATCH_RETRIEVE_TICKS) {
            return 0f;
        }

        final float bite = Mth.clamp(elapsed / FishingBarrelBlockEntity.CATCH_BITE_TICKS, 0f, 1f);
        return bite * 0.18f;
    }

    private static float getCatchRetrieveProgress(float elapsed) {
        final float retrieveElapsed = elapsed - FishingBarrelBlockEntity.CATCH_BITE_TICKS;
        final float progress = Mth.clamp(retrieveElapsed / FishingBarrelBlockEntity.CATCH_RETRIEVE_TICKS, 0f, 1f);
        return progress * progress * (3f - 2f * progress);
    }

    private static float getCatchElasticRetrieveProgress(float elapsed) {
        final float retrieveElapsed = elapsed - FishingBarrelBlockEntity.CATCH_BITE_TICKS;
        final float progress = Mth.clamp(retrieveElapsed / FishingBarrelBlockEntity.CATCH_RETRIEVE_TICKS, 0f, 1f);
        if (progress <= 0f || progress >= 1f) {
            return progress;
        }

        return (float) (Math.pow(2, -10f * progress) * Mth.sin((progress * 10f - 0.75f) * (2f * Mth.PI / 3f)) + 1f);
    }

    private static float getCatchLowerProgress(float elapsed) {
        final float lowerElapsed = elapsed - FishingBarrelBlockEntity.CATCH_BITE_TICKS - FishingBarrelBlockEntity.CATCH_RETRIEVE_TICKS;
        final float lowerTicks = FishingBarrelBlockEntity.CATCH_ANIMATION_TICKS - FishingBarrelBlockEntity.CATCH_BITE_TICKS - FishingBarrelBlockEntity.CATCH_RETRIEVE_TICKS;
        if (lowerTicks <= 0f) {
            return 1f;
        }

        final float progress = Mth.clamp(lowerElapsed / lowerTicks, 0f, 1f);
        return progress * progress * (3f - 2f * progress);
    }

    private static void submitCatchItems(FishingBarrelRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 bobber) {
        if (renderState.catchItemCount <= 0 || getCatchAnimationElapsed(renderState) >= CATCH_ITEM_FLIGHT_END_ELAPSED) {
            return;
        }

        for (int i = 0; i < renderState.catchItemCount; i++) {
            final Vec3 itemPos = getCatchItemPosition(renderState, bobber, i);

            poseStack.pushPose();
            poseStack.translate(itemPos.x, itemPos.y, itemPos.z);
            poseStack.mulPose(Axis.XP.rotationDegrees(45f));
            final float scale = 0.28f;
            poseStack.scale(scale, scale, scale);
            renderState.catchItems[i].submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private static Vec3 getCatchItemPosition(FishingBarrelRenderState renderState, Vec3 bobber, int index) {
        final Vec3 bobberOffset = new Vec3(0, -0.15 + index * 0.025, 0);
        final float elapsed = getCatchAnimationElapsed(renderState);
        if (elapsed < CATCH_ITEM_LAUNCH_ELAPSED) {
            return bobber.add(bobberOffset);
        }

        final float flightProgress = Mth.clamp((elapsed - CATCH_ITEM_LAUNCH_ELAPSED) / CATCH_ITEM_FLIGHT_TICKS, 0f, 1f);
        final Direction side = renderState.facing.getClockWise();
        final double sideSpread = (index - (renderState.catchItemCount - 1) * 0.5) * 0.055;
        final Vec3 launchPos = getBobberPosition(renderState, CATCH_ITEM_LAUNCH_ELAPSED).add(bobberOffset);
        final Vec3 targetPos = new Vec3(
                0.5 + side.getStepX() * sideSpread,
                0.58 + index * 0.015,
                0.5 + side.getStepZ() * sideSpread);

        final Vec3 linearPos = launchPos.lerp(targetPos, flightProgress);
        final double arc = Mth.sin(flightProgress * Mth.PI) * CATCH_ITEM_ARC_HEIGHT;
        return linearPos.add(0, arc, 0);
    }
}
