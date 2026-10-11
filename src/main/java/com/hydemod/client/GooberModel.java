package com.hydemod.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/** A squat gooey blob with a little tuft and two stubby feet. */
public class GooberModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart body;
    private final ModelPart tuft;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;

    public GooberModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.tuft = body.getChild("tuft");
        this.leftFoot = root.getChild("left_foot");
        this.rightFoot = root.getChild("right_foot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        body.addOrReplaceChild("tuft", CubeListBuilder.create().texOffs(36, 0).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        root.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 3.0F, 5.0F),
                PartPose.offset(-2.5F, 21.0F, 0.0F));
        root.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -3.0F, 4.0F, 3.0F, 5.0F),
                PartPose.offset(2.5F, 21.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        body.yRot = state.yRot * Mth.DEG_TO_RAD;
        body.zRot = Mth.cos(pos * 0.9F) * 0.12F * speed;
        body.y = 21.0F - Math.abs(Mth.sin(pos * 0.9F)) * 1.2F * speed;
        tuft.zRot = Mth.sin(state.ageInTicks * 0.2F) * 0.25F;
        rightFoot.xRot = Mth.cos(pos * 0.9F) * 1.2F * speed;
        leftFoot.xRot = Mth.cos(pos * 0.9F + Mth.PI) * 1.2F * speed;
    }
}
