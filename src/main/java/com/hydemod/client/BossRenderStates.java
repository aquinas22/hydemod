package com.hydemod.client;

import com.hydemod.entity.HydeBoss;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Render states carrying each boss's current attack animation to its model. */
public final class BossRenderStates {
    public static class SamHydeState extends HumanoidRenderState {
        public int anim;
        public float animTime;
        public int intro;
        public boolean phaseTwo;
    }

    public static class ElephantState extends LivingEntityRenderState {
        public int anim;
        public float animTime;
        public int intro;
        public boolean phaseTwo;
    }

    static void copy(HydeBoss boss, float partialTick, SamHydeState s) {
        s.anim = boss.getAnimId();
        s.animTime = boss.getAnimTime(partialTick);
        s.intro = boss.getIntroTicksLeft();
        s.phaseTwo = boss.isPhaseTwoClient();
    }

    static void copy(HydeBoss boss, float partialTick, ElephantState s) {
        s.anim = boss.getAnimId();
        s.animTime = boss.getAnimTime(partialTick);
        s.intro = boss.getIntroTicksLeft();
        s.phaseTwo = boss.isPhaseTwoClient();
    }

    /** 0 -> 1 -> 0 envelope over {@code length} ticks (fast rise, hold, ease out). */
    static float envelope(float t, float length) {
        if (t < 0 || t > length) {
            return 0.0F;
        }
        float u = t / length;
        float rise = Math.min(1.0F, u * 5.0F);
        float fall = Math.min(1.0F, (1.0F - u) * 3.0F);
        return rise * fall;
    }

    private BossRenderStates() {}
}
