package net.blay09.mods.farmingforblockheads.client.render;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;

public class ShippingBalloonModel extends Model<Unit> {

    private static final String BONE = "bone";
    private static final String BOX = "box";
    private static final String ROPE = "rope";
    private static final String ROPE2 = "rope2";
    private static final String ROPE3 = "rope3";
    private static final String ROPE4 = "rope4";
    private static final String BALLOON = "balloon";

    public final ModelPart bone;
    public final ModelPart box;
    public final ModelPart rope;
    public final ModelPart rope2;
    public final ModelPart rope3;
    public final ModelPart rope4;
    public final ModelPart balloon;

    public ShippingBalloonModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        bone = root.getChild(BONE);
        box = bone.getChild(BOX);
        rope = bone.getChild(ROPE);
        rope2 = bone.getChild(ROPE2);
        rope3 = bone.getChild(ROPE3);
        rope4 = bone.getChild(ROPE4);
        balloon = bone.getChild(BALLOON);
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition meshDefinition = new MeshDefinition();
        final PartDefinition root = meshDefinition.getRoot();

        final PartDefinition bone = root.addOrReplaceChild(BONE, CubeListBuilder.create(), PartPose.ZERO);
        bone.addOrReplaceChild(BOX, CubeListBuilder.create()
                .texOffs(0, 57)
                .addBox(-6.5f, 3.25f, -6.5f, 13f, 13f, 13f), PartPose.ZERO);
        bone.addOrReplaceChild(ROPE, createRope(4.5f, -6.5f), PartPose.ZERO);
        bone.addOrReplaceChild(ROPE2, createRope(4.5f, 4.5f), PartPose.ZERO);
        bone.addOrReplaceChild(ROPE3, createRope(-6.5f, 4.5f), PartPose.ZERO);
        bone.addOrReplaceChild(ROPE4, createRope(-6.5f, -6.5f), PartPose.ZERO);
        bone.addOrReplaceChild(BALLOON, CubeListBuilder.create()
                .texOffs(53, 0)
                .addBox(-7.5f, 26.25f, -7.5f, 15f, 2f, 15f)
                .texOffs(21, 21)
                .addBox(-10.5f, 28.25f, -10.5f, 21f, 15f, 21f), PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    private static CubeListBuilder createRope(float x, float z) {
        return CubeListBuilder.create()
                .texOffs(2, 12)
                .addBox(x, 16.25f, z, 2f, 1f, 2f)
                .texOffs(2, 12)
                .addBox(x, 19.25f, z, 2f, 1f, 2f)
                .texOffs(2, 12)
                .addBox(x, 23.25f, z, 2f, 1f, 2f)
                .texOffs(28, 0)
                .addBox(x + 0.5f, 17.25f, z + 0.5f, 1f, 9f, 1f);
    }
}
