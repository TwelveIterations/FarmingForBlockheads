package net.blay09.mods.farmingforblockheads.client.render;

import net.blay09.mods.farmingforblockheads.FarmingForBlockheads;
import net.blay09.mods.farmingforblockheads.entity.CourierEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class CourierRenderer extends MobRenderer<CourierEntity, VillagerRenderState, CourierModel> {

    private static final Identifier MERCHANT_TEXTURE = Identifier.fromNamespaceAndPath(FarmingForBlockheads.MOD_ID, "textures/entity/merchant.png");
    private static final float LOOK_DOWN_X_ROT = 35f;

    public CourierRenderer(EntityRendererProvider.Context context) {
        super(context, new CourierModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
        addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return MERCHANT_TEXTURE;
    }

    @Override
    public void extractRenderState(CourierEntity entity, VillagerRenderState state, float delta) {
        super.extractRenderState(entity, state, delta);
        if (state instanceof MerchantRenderState merchantRenderState) {
            merchantRenderState.reaching = entity.isReaching();
            merchantRenderState.courierAnimationTicks = entity.getAnimationTicks(delta);
            final int lookAtPlayerDelayTicks = CourierEntity.LOOK_FORWARD_TICKS + CourierEntity.LOOK_DOWN_TICKS;
            if (merchantRenderState.courierAnimationTicks >= CourierEntity.LOOK_FORWARD_TICKS && merchantRenderState.courierAnimationTicks < lookAtPlayerDelayTicks) {
                merchantRenderState.xRot = LOOK_DOWN_X_ROT;
            } else if (merchantRenderState.courierAnimationTicks >= lookAtPlayerDelayTicks) {
                lookAtClosestPlayer(entity, merchantRenderState);
            }
        }
    }

    private static void lookAtClosestPlayer(CourierEntity entity, MerchantRenderState state) {
        final Player player = entity.level().getNearestPlayer(entity, 16);
        if (player == null) {
            return;
        }

        final double deltaX = player.getX() - entity.getX();
        final double deltaY = player.getEyeY() - entity.getEyeY();
        final double deltaZ = player.getZ() - entity.getZ();
        final double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        final float targetYRot = (float) (Mth.atan2(deltaZ, deltaX) * Mth.RAD_TO_DEG) - 90f;
        state.yRot = Mth.wrapDegrees(targetYRot - entity.getYRot());
        state.xRot = (float) -(Mth.atan2(deltaY, horizontalDistance) * Mth.RAD_TO_DEG);
    }

    @Override
    public MerchantRenderState createRenderState() {
        return new MerchantRenderState();
    }
}
