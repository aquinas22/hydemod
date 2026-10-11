package com.hydemod.entity;

import com.hydemod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Elephant Mario: a huge elephant plumber who ate one fruit too many.
 *
 * Abilities:
 *  - Ground Pound: leaps up and slams down, shockwave damages and launches everything nearby.
 *  - Trunk Spray: fires blasts of water from his trunk.
 *  - Goober Call: summons a squad of Goobers (stomp on them!).
 *  - Trunk Charge: dashes at his target.
 * Below half health he gets faster and angrier.
 */
public class ElephantMario extends HydeBoss {
    private int abilityCooldown = 60;
    private boolean pounding;
    private int poundTicks;
    private boolean introduced;

    public ElephantMario(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.STEP_HEIGHT, 1.5)
                .add(Attributes.SAFE_FALL_DISTANCE, 64.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, Goober.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!introduced) {
            introduced = true;
            say("boss.hydemod.elephant_mario.intro");
        }
        if (pounding) {
            poundTicks++;
            if (poundTicks > 8 && onGround()) {
                land(level);
            } else if (poundTicks > 100) {
                pounding = false;
            }
            return;
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || --abilityCooldown > 0) {
            return;
        }
        boolean enraged = getHealth() < getMaxHealth() * 0.5F;
        abilityCooldown = (enraged ? 50 : 80) + random.nextInt(40);
        switch (random.nextInt(5)) {
            case 0, 1 -> groundPound(level, target);
            case 2 -> trunkSpray(level, target, enraged ? 5 : 3);
            case 3 -> callGoobers(level);
            default -> trunkCharge(target);
        }
    }

    private void groundPound(ServerLevel level, LivingEntity target) {
        say("boss.hydemod.elephant_mario.pound");
        Vec3 toward = target.position().subtract(position()).multiply(0.08, 0, 0.08);
        setDeltaMovement(toward.x, 1.25, toward.z);
        hurtMarked = true;
        pounding = true;
        poundTicks = 0;
        level.playSound(null, blockPosition(), SoundEvents.GOAT_LONG_JUMP, SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    private void land(ServerLevel level) {
        pounding = false;
        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.7F);
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.3, getZ(), 6, 2.0, 0.2, 2.0, 0.0);
        level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.2, getZ(), 60, 3.0, 0.1, 3.0, 0.1);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6.0, 2.0, 6.0), e -> e != this && !(e instanceof Goober))) {
            double dist = victim.distanceTo(this);
            float dmg = (float) Math.max(4.0, 16.0 - dist * 2.0);
            victim.hurtServer(level, damageSources().mobAttack(this), dmg);
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.setDeltaMovement(away.x * 1.2, 0.9, away.z * 1.2);
            victim.hurtMarked = true;
        }
    }

    private void trunkSpray(ServerLevel level, LivingEntity target, int count) {
        say("boss.hydemod.elephant_mario.spray");
        for (int i = 0; i < count; i++) {
            TrunkSpray spray = new TrunkSpray(level, this);
            spray.setPos(getX(), getEyeY() - 0.6, getZ());
            double dx = target.getX() - getX();
            double dy = target.getY(0.5) - spray.getY();
            double dz = target.getZ() - getZ();
            spray.shoot(dx, dy, dz, 1.4F, 4.0F + i * 3.0F);
            level.addFreshEntity(spray);
        }
        level.playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0F, 1.8F);
    }

    private void callGoobers(ServerLevel level) {
        if (level.getEntitiesOfClass(Goober.class, getBoundingBox().inflate(24.0)).size() >= 8) {
            trunkCharge(getTarget());
            return;
        }
        say("boss.hydemod.elephant_mario.goobers");
        for (int i = 0; i < 4; i++) {
            BlockPos pos = blockPosition().offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            Goober goober = ModEntities.GOOBER.get().spawn(level, pos, EntitySpawnReason.MOB_SUMMONED);
            if (goober != null) {
                goober.setTarget(getTarget());
                level.sendParticles(ParticleTypes.POOF, goober.getX(), goober.getY() + 0.4, goober.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
            }
        }
    }

    private void trunkCharge(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        say("boss.hydemod.elephant_mario.charge");
        Vec3 dir = target.position().subtract(position()).normalize();
        setDeltaMovement(dir.x * 1.8, 0.3, dir.z * 1.8);
        hurtMarked = true;
        playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 1.3F);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof Goober || source.getDirectEntity() instanceof TrunkSpray) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && random.nextInt(10) == 0 && isAlive()) {
            sayRandom("boss.hydemod.elephant_mario.hurt", 5);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        say("boss.hydemod.elephant_mario.death");
        super.die(source);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 1.3F;
    }
}
