package com.hydemod.client;

import com.hydemod.entity.SamHyde;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/** Player-skin humanoid with Sam Hyde's attack poses layered over the normal walk cycle. */
public class SamHydeModel extends HumanoidModel<BossRenderStates.SamHydeState> {
    public SamHydeModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
    }

    @Override
    public void setupAnim(BossRenderStates.SamHydeState s) {
        super.setupAnim(s);
        float t = s.animTime;
        float age = s.ageInTicks;
        if (s.intro > 0) {
            // Entrance: arms spread wide to soak up the applause, slight bow, then a wave.
            float u = 1.0F - s.intro / 60.0F;
            rightArm.zRot = Mth.lerp(Math.min(1, u * 3), 0.0F, 2.3F) + Mth.sin(age * 0.6F) * 0.25F * Math.max(0, u - 0.5F) * 2;
            leftArm.zRot = -Mth.lerp(Math.min(1, u * 3), 0.0F, 1.2F);
            head.xRot = Mth.sin(u * Mth.PI) * 0.5F;
            body.xRot = Mth.sin(u * Mth.PI) * 0.25F;
            return;
        }
        float e;
        switch (s.anim) {
            case SamHyde.ANIM_THROW -> { // two-handed overhead heave
                e = BossRenderStates.envelope(t, 14);
                float swing = t < 6 ? -2.8F : Mth.lerp(Math.min(1, (t - 6) / 4), -2.8F, -0.6F);
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, swing);
                leftArm.xRot = Mth.lerp(e, leftArm.xRot, swing);
                body.xRot = e * (t < 6 ? -0.2F : 0.3F);
            }
            case SamHyde.ANIM_CLOSE_EYES -> { // hypnotist: arms out, fingers wiggling, head tilted back
                e = BossRenderStates.envelope(t, 40);
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, -1.5F);
                leftArm.xRot = Mth.lerp(e, leftArm.xRot, -1.5F);
                rightArm.yRot = e * (-0.4F + Mth.sin(age * 0.5F) * 0.15F);
                leftArm.yRot = e * (0.4F - Mth.sin(age * 0.5F) * 0.15F);
                head.xRot = Mth.lerp(e, head.xRot, -0.3F);
            }
            case SamHyde.ANIM_SUMMON, SamHyde.ANIM_RESET, SamHyde.ANIM_RAIN -> { // both arms thrust skyward, shaking
                e = BossRenderStates.envelope(t, 30);
                float shake = Mth.sin(age * 1.7F) * 0.12F;
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, -3.0F + shake);
                leftArm.xRot = Mth.lerp(e, leftArm.xRot, -3.0F - shake);
                rightArm.zRot = e * 0.35F;
                leftArm.zRot = -e * 0.35F;
                head.xRot = Mth.lerp(e, head.xRot, -0.5F);
            }
            case SamHyde.ANIM_MAGLEV -> { // dramatic point to the Moon
                e = BossRenderStates.envelope(t, 25);
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, -2.6F);
                rightArm.zRot = -e * 0.3F;
                leftArm.zRot = -e * 0.6F;
                head.xRot = Mth.lerp(e, head.xRot, -0.6F);
            }
            case SamHyde.ANIM_COFFEE -> { // chugging the coffee
                e = BossRenderStates.envelope(t, 30);
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, -2.0F + Mth.sin(age * 1.2F) * 0.08F);
                rightArm.yRot = e * 0.5F;
                head.xRot = Mth.lerp(e, head.xRot, -0.55F);
            }
            case SamHyde.ANIM_PAT -> { // patting himself on the back
                e = BossRenderStates.envelope(t, 30);
                float pat = Mth.sin(age * 1.4F) * 0.3F;
                rightArm.xRot = Mth.lerp(e, rightArm.xRot, -2.4F + pat);
                rightArm.zRot = e * 0.9F;
                leftArm.xRot = Mth.lerp(e, leftArm.xRot, 0.4F);
                head.xRot = Mth.lerp(e, head.xRot, 0.2F);
            }
            default -> { }
        }
        if (s.phaseTwo) {
            // agitated: head bobs like he's hyped on bulletproof coffee
            head.zRot += Mth.sin(age * 0.9F) * 0.05F;
        }
    }
}
