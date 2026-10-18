package com.hydemod.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import com.hydemod.entity.ElephantMario;
import net.minecraft.util.Mth;

/** Elephant Mario's red cap, red sleeves, blue overalls, gloves and articulated trunk. */
public class ElephantMarioModel extends EntityModel<BossRenderStates.ElephantState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart trunk;
    private final ModelPart trunkMid;
    private final ModelPart trunkTip;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ElephantMarioModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftEar = head.getChild("left_ear");
        this.rightEar = head.getChild("right_ear");
        this.trunk = head.getChild("trunk");
        this.trunkMid = trunk.getChild("trunk_mid");
        this.trunkTip = trunkMid.getChild("trunk_tip");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    /** A chunky cartoon elephant: round belly, big head, floppy ears, long curling trunk, cap and overalls. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9.0F, -16.0F, -6.0F, 18.0F, 16.0F, 12.0F)
                        .texOffs(60, 0).addBox(-7.0F, -13.0F, -8.0F, 14.0F, 10.0F, 2.0F),   // round belly
                PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 28).addBox(-7.0F, -12.0F, -7.0F, 14.0F, 12.0F, 12.0F)
                        .texOffs(0, 52).addBox(-7.5F, -17.0F, -7.5F, 15.0F, 6.0F, 13.0F)   // cap crown
                        .texOffs(56, 52).addBox(-7.5F, -12.0F, -12.0F, 15.0F, 1.0F, 5.0F), // cap brim
                PartPose.offset(0.0F, -1.0F, -2.0F));
        head.addOrReplaceChild("cap_badge", CubeListBuilder.create().texOffs(104, 66)
                        .addBox(-2.5F, -2.5F, -0.5F, 5.0F, 5.0F, 1.0F),
                PartPose.offset(0.0F, -14.5F, -7.8F));
        head.addOrReplaceChild("mustache", CubeListBuilder.create().texOffs(64, 72)
                        .addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -3.7F, -7.3F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(92, 0).addBox(0.0F, -6.0F, 0.0F, 1.0F, 12.0F, 10.0F),
                PartPose.offsetAndRotation(6.5F, -7.0F, -3.0F, 0.0F, 0.95F, 0.0F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(92, 0).mirror().addBox(-1.0F, -6.0F, 0.0F, 1.0F, 12.0F, 10.0F),
                PartPose.offsetAndRotation(-6.5F, -7.0F, -3.0F, 0.0F, -0.95F, 0.0F));
        PartDefinition trunk = head.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(60, 12).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, -8.0F, -0.2F, 0.0F, 0.0F));
        PartDefinition mid = trunk.addOrReplaceChild("trunk_mid", CubeListBuilder.create().texOffs(80, 80).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.3F, 0.0F, 0.0F));
        PartDefinition tip = mid.addOrReplaceChild("trunk_tip", CubeListBuilder.create().texOffs(96, 80).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.5F, 0.0F, 0.0F));
        tip.addOrReplaceChild("trunk_nose", CubeListBuilder.create().texOffs(108, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 4.5F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 28).addBox(-5.0F, -1.0F, -3.0F, 6.0F, 13.0F, 6.0F),
                PartPose.offset(-9.0F, 1.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 28).mirror().addBox(-1.0F, -1.0F, -3.0F, 6.0F, 13.0F, 6.0F),
                PartPose.offset(9.0F, 1.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(84, 28).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F),
                PartPose.offset(-4.5F, 15.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(84, 28).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F),
                PartPose.offset(4.5F, 15.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(BossRenderStates.ElephantState state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        float t = state.ageInTicks;
        // Bouncy cartoon waddle: the whole upper body hops with each step and sways side to side.
        float bob = Math.abs(Mth.sin(pos * 0.5F)) * 1.6F * speed;
        float sway = Mth.sin(pos * 0.5F) * 0.12F * speed;
        body.y -= bob; head.y -= bob; leftArm.y -= bob; rightArm.y -= bob;
        body.zRot = sway; head.zRot = sway * 0.6F;
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.5F + Mth.sin(t * 0.08F) * 0.03F;
        rightLeg.xRot = Mth.cos(pos * 0.5F) * 1.0F * speed;
        leftLeg.xRot = Mth.cos(pos * 0.5F + Mth.PI) * 1.0F * speed;
        rightArm.xRot = Mth.cos(pos * 0.5F + Mth.PI) * 0.9F * speed;
        leftArm.xRot = Mth.cos(pos * 0.5F) * 0.9F * speed;
        rightArm.zRot = 0.18F + Mth.sin(t * 0.07F) * 0.05F;
        leftArm.zRot = -0.18F - Mth.sin(t * 0.07F) * 0.05F;
        // Floppy ears and a trunk that curls up at the end and swings with every step.
        float flap = Mth.sin(t * 0.15F) * 0.18F + speed * 0.35F;
        leftEar.yRot = 0.95F + flap;
        rightEar.yRot = -0.95F - flap;
        leftEar.zRot = -Mth.sin(t * 0.11F) * 0.06F;
        rightEar.zRot = Mth.sin(t * 0.11F) * 0.06F;
        float curl = Mth.sin(t * 0.09F) * 0.15F;
        trunk.xRot = -0.2F + curl * 0.5F;
        trunk.zRot = Mth.cos(t * 0.07F) * 0.08F + Mth.sin(pos * 0.5F) * 0.22F * speed;
        trunkMid.xRot = -0.3F + curl;
        trunkTip.xRot = -0.6F + curl * 1.5F;
        applyAttack(state, t);
    }

    private void applyAttack(BossRenderStates.ElephantState s, float age) {
        float at = s.animTime;
        float e;
        if (s.intro > 0) {
            // Entrance: rears up, arms high, trunk raised in a trumpet blast, ears flared.
            float u = 1.0F - s.intro / 60.0F;
            float k = Mth.sin(Math.min(1, u * 1.3F) * Mth.PI);
            rightArm.xRot = -2.8F * k; leftArm.xRot = -2.8F * k;
            trunk.xRot = -2.4F * k; trunkMid.xRot = -0.6F * k; trunkTip.xRot = -0.9F * k;
            leftEar.yRot = 1.3F * k + 0.95F * (1 - k); rightEar.yRot = -1.3F * k - 0.95F * (1 - k);
            head.xRot = -0.4F * k;
            return;
        }
        switch (s.anim) {
            case ElephantMario.ANIM_POUND -> { // arms flung up on the leap, slammed down on landing
                e = BossRenderStates.envelope(at, 30);
                float up = at < 14 ? 1 : Math.max(0, 1 - (at - 14) / 4);
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, -3.0F * up + 0.4F * (1 - up));
                leftArm.xRot = Mth.lerp(e, leftArm.xRot, -3.0F * up + 0.4F * (1 - up));
                rightArm.zRot = e * 0.4F; leftArm.zRot = -e * 0.4F;
                rightLeg.xRot = Mth.lerp(e * up, rightLeg.xRot, -0.6F);
                leftLeg.xRot = Mth.lerp(e * up, leftLeg.xRot, -0.6F);
                trunk.xRot = Mth.lerp(e, trunk.xRot, -1.2F * up);
                leftEar.yRot += e * 0.7F; rightEar.yRot -= e * 0.7F;
            }
            case ElephantMario.ANIM_SPRAY -> { // trunk levelled straight at you like a hose, recoiling
                e = BossRenderStates.envelope(at, 25);
                float recoil = Mth.sin(age * 2.2F) * 0.08F;
                trunk.xRot = Mth.lerp(e, trunk.xRot, -1.55F + recoil);
                trunkTip.xRot = Mth.lerp(e, trunkTip.xRot, 0.0F);
                head.xRot = Mth.lerp(e, head.xRot, -0.15F);
                leftEar.yRot += e * 0.5F; rightEar.yRot -= e * 0.5F;
            }
            case ElephantMario.ANIM_THROW -> { // big overhand candy toss with the right arm
                e = BossRenderStates.envelope(at, 16);
                float wind = at < 7 ? -3.0F : Mth.lerp(Math.min(1, (at - 7) / 3), -3.0F, -0.3F);
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, wind);
                body.yRot = e * (at < 7 ? 0.3F : -0.25F);
                head.yRot += e * (at < 7 ? 0.2F : -0.2F);
            }
            case ElephantMario.ANIM_SNACK -> { // hand to mouth, trunk curled, happy ear flaps
                e = BossRenderStates.envelope(at, 55);
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, -2.1F + Mth.sin(age * 1.3F) * 0.15F);
                rightArm.yRot = e * 0.5F;
                trunk.xRot = Mth.lerp(e, trunk.xRot, -2.0F);
                trunkTip.xRot = Mth.lerp(e, trunkTip.xRot, -1.2F);
                leftEar.yRot += e * Mth.sin(age * 0.8F) * 0.4F; rightEar.yRot -= e * Mth.sin(age * 0.8F) * 0.4F;
            }
            case ElephantMario.ANIM_CHARGE -> { // head down, trunk forward, arms back
                e = BossRenderStates.envelope(at, 45);
                head.xRot = Mth.lerp(e, head.xRot, 0.6F);
                trunk.xRot = Mth.lerp(e, trunk.xRot, -1.0F);
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, 0.9F); leftArm.xRot = Mth.lerp(e, leftArm.xRot, 0.9F);
                leftEar.yRot += e * 1.0F; rightEar.yRot -= e * 1.0F;
            }
            case ElephantMario.ANIM_ROAR -> { // phase two: rears up and roars, shaking
                e = BossRenderStates.envelope(at, 50);
                float shake = Mth.sin(age * 2.5F) * 0.1F;
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, -2.5F + shake); leftArm.xRot = Mth.lerp(e, leftArm.xRot, -2.5F - shake);
                rightArm.zRot = e * 0.6F; leftArm.zRot = -e * 0.6F;
                trunk.xRot = Mth.lerp(e, trunk.xRot, -2.6F); trunkTip.xRot = Mth.lerp(e, trunkTip.xRot, -1.0F);
                head.xRot = Mth.lerp(e, head.xRot, -0.5F);
                leftEar.yRot = Mth.lerp(e, leftEar.yRot, 1.4F); rightEar.yRot = Mth.lerp(e, rightEar.yRot, -1.4F);
            }
            default -> { }
        }
        if (s.phaseTwo) {
            float huff = Mth.sin(age * 0.5F) * 0.05F;
            leftEar.yRot += 0.2F + huff; rightEar.yRot -= 0.2F + huff;
        }
    }
}
