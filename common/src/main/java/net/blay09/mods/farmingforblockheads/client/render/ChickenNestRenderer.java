package net.blay09.mods.farmingforblockheads.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blay09.mods.farmingforblockheads.block.ChickenNestBlock;
import net.blay09.mods.farmingforblockheads.block.entity.ChickenNestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChickenNestRenderer implements BlockEntityRenderer<ChickenNestBlockEntity> {

    private final ItemStack EGG_STACK = new ItemStack(Items.EGG);
    private final float[] EGG_POSITIONS = new float[]{
            0.2f, 0, 0,
            -0.2f, 0, 0,
            0, 0, -0.1f,
            0, 0, 0.1f,
    };

    public ChickenNestRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static float getFacingAngle(Direction facing) {
        return switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> 90;
            default -> -90;
        };
    }

    @Override
    public void render(ChickenNestBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight, int combinedOverlay, Vec3 cameraPos) {
        if (!blockEntity.hasLevel()) {
            return;
        }

        Level level = blockEntity.getLevel();poseStack.translate(0.5, 0, 0.5);

        BlockState state = blockEntity.getBlockState();
        float angle = 0f;
        if (state.hasProperty(ChickenNestBlock.FACING)) {
            angle = getFacingAngle(state.getValue(ChickenNestBlock.FACING));
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (int i = 0; i < Math.min(EGG_POSITIONS.length / 3, blockEntity.getEggCount()); i++) {
            poseStack.pushPose();
            poseStack.translate(EGG_POSITIONS[i * 3], 0.1f + EGG_POSITIONS[i * 3 + 1], -0.1f + EGG_POSITIONS[i * 3 + 2]);
            poseStack.mulPose(Axis.XP.rotationDegrees(45f));
            itemRenderer.renderStatic(EGG_STACK, ItemDisplayContext.GROUND, combinedLight, OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);
            poseStack.popPose();
        }
    }
}
