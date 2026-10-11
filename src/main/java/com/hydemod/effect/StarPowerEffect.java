package com.hydemod.effect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

/** Invincibility Star: anything hostile you touch gets knocked flying, and you sparkle in rainbow colours. */
public class StarPowerEffect extends MobEffect {
    private static final int[] RAINBOW = {0xFF4040, 0xFFA030, 0xFFF040, 0x50FF60, 0x40C0FF, 0xB060FF};

    public StarPowerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFE040);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplification) {
        int color = RAINBOW[(int) (level.getGameTime() / 2 % RAINBOW.length)];
        level.sendParticles(new DustParticleOptions(color, 1.2F), entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                3, entity.getBbWidth() * 0.6, entity.getBbHeight() * 0.4, entity.getBbWidth() * 0.6, 0.0);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(0.4),
                e -> e != entity && e instanceof Enemy && e.isAlive())) {
            if (victim.hurtServer(level, entity.damageSources().mobAttack(entity), 12.0F)) {
                var away = victim.position().subtract(entity.position()).normalize();
                victim.push(away.x * 1.4, 0.6, away.z * 1.4);
                victim.hurtMarked = true;
            }
        }
        return true;
    }
}
