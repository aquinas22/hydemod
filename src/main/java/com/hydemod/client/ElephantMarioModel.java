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

/** A big upright elephant in a knit cap and work overalls, with flappy ears and a swinging trunk. */
public class ElephantMarioModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart trunk;
    private final ModelPart trunkTip;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ElephantMarioModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.leftEar = head.getChild("left_ear");
        this.rightEar = head.getChild("right_ear");
        this.trunk = head.getChild("trunk");
        this.trunkTip = trunk.getChild("trunk_tip");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -14.0F, -5.0F, 16.0F, 14.0F, 10.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 24).addBox(-6.0F, -11.0F, -6.0F, 12.0F, 11.0F, 11.0F)
                        .texOffs(0, 48).addBox(-6.5F, -13.0F, -6.5F, 13.0F, 3.0F, 12.0F)
                        .texOffs(0, 64).addBox(-6.5F, -11.0F, -10.0F, 13.0F, 1.0F, 4.0F)
                        .texOffs(116, 0).addBox(-5.0F, -3.0F, -8.0F, 1.0F, 1.0F, 3.0F)
                        .texOffs(116, 0).addBox(4.0F, -3.0F, -8.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(56, 16).addBox(0.0F, -5.0F, 0.0F, 1.0F, 10.0F, 8.0F),
                PartPose.offsetAndRotation(5.5F, -6.0F, -2.0F, 0.0F, -0.6F, 0.0F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(56, 16).mirror().addBox(-1.0F, -5.0F, 0.0F, 1.0F, 10.0F, 8.0F),
                PartPose.offsetAndRotation(-5.5F, -6.0F, -2.0F, 0.0F, 0.6F, 0.0F));
        PartDefinition trunk = head.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(100, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, -7.0F, -0.15F, 0.0F, 0.0F));
        trunk.addOrReplaceChild("trunk_tip", CubeListBuilder.create().texOffs(100, 10).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.35F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(80, 0).addBox(-4.0F, -1.0F, -2.5F, 5.0F, 13.0F, 5.0F),
                PartPose.offset(-9.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(80, 0).mirror().addBox(-1.0F, -1.0F, -2.5F, 5.0F, 13.0F, 5.0F),
                PartPose.offset(9.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 10.0F, 6.0F),
                PartPose.offset(-4.0F, 14.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 10.0F, 6.0F),
                PartPose.offset(4.0F, 14.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        float t = state.ageInTicks;
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.5F;
        rightLeg.xRot = Mth.cos(pos * 0.5F) * 1.0F * speed;
        leftLeg.xRot = Mth.cos(pos * 0.5F + Mth.PI) * 1.0F * speed;
        rightArm.xRot = Mth.cos(pos * 0.5F + Mth.PI) * 0.8F * speed;
        leftArm.xRot = Mth.cos(pos * 0.5F) * 0.8F * speed;
        rightArm.zRot = 0.1F + Mth.sin(t * 0.07F) * 0.04F;
        leftArm.zRot = -0.1F - Mth.sin(t * 0.07F) * 0.04F;
        float flap = Mth.sin(t * 0.15F) * 0.15F + speed * 0.3F;
        leftEar.yRot = -0.6F - flap;
        rightEar.yRot = 0.6F + flap;
        trunk.xRot = -0.15F + Mth.sin(t * 0.1F) * 0.12F;
        trunk.zRot = Mth.cos(t * 0.08F) * 0.1F + Mth.sin(pos * 0.5F) * 0.2F * speed;
        trunkTip.xRot = -0.35F + Mth.sin(t * 0.1F + 1.0F) * 0.2F;
    }
}
