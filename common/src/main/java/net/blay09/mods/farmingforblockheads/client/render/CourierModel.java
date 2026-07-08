package net.blay09.mods.farmingforblockheads.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;

public class CourierModel extends VillagerModel {

    private final ModelPart arms;

    public CourierModel(ModelPart root) {
        super(root);
        arms = root.getChild("arms");
    }

    @Override
    public void setupAnim(VillagerRenderState state) {
        super.setupAnim(state);
        if (state instanceof MerchantRenderState merchantRenderState && merchantRenderState.reaching) {
            arms.xRot = -1.35f;
            arms.y = 2.0f;
            arms.z = -4.0f;
        }
    }
}
