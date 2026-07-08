package net.blay09.mods.farmingforblockheads.client.render;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;

public class ShippingCrateModel extends Model<Unit> {

    private static final String BOX = "box";

    public final ModelPart box;

    public ShippingCrateModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        box = root.getChild(BOX);
    }

    public static LayerDefinition createBodyLayer() {
        final MeshDefinition meshDefinition = new MeshDefinition();
        final var root = meshDefinition.getRoot();
        root.addOrReplaceChild(BOX, CubeListBuilder.create()
                .texOffs(0, 57)
                .addBox(-6.5f, 3.25f, -6.5f, 13f, 13f, 13f), PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 128, 128);
    }
}
