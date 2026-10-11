package com.hydemod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * Depth Grovler: one of the displaced lower-class hordes who, by 2070, live underground in tiered
 * cities endlessly toiling for nuggets of neoplas. A squat, grumpy little blob. Weak in a fight,
 * but the proper way to deal with one is to jump on its head - stomping squashes it instantly
 * and bounces you up. Sam Hyde calls them up from below to fight for him.
 */
public class DepthGrovler extends Monster {
    public DepthGrovler(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    /** Lives in the tiered cities below: spawns in the dark under Y 40. */
    public static boolean checkGrovlerSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return pos.getY() < 40 && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, SamHyde.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level && isAlive()) {
            checkStomp(level);
        }
    }

    /** Any player falling onto our head squashes us. */
    private void checkStomp(ServerLevel level) {
        double headY = getY() + getBbHeight();
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.15, 0.6, 0.15))) {
            boolean falling = player.getDeltaMovement().y < -0.08 || player.fallDistance > 0.4;
            if (falling && !player.onGround() && player.getY() >= headY - 0.45 && !player.isSpectator()) {
                squash(level, player);
                return;
            }
        }
    }

    private void squash(ServerLevel level, Player stomper) {
        level.playSound(null, blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.2F, 0.7F);
        level.sendParticles(ParticleTypes.ITEM_SLIME, getX(), getY() + 0.3, getZ(), 20, 0.3, 0.1, 0.3, 0.1);
        stomper.setDeltaMovement(stomper.getDeltaMovement().x, 0.65, stomper.getDeltaMovement().z);
        stomper.hurtMarked = true;
        stomper.resetFallDistance();
        hurtServer(level, damageSources().playerAttack(stomper), Float.MAX_VALUE);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        // A player standing on our head is stomping us, not getting bitten.
        if (target.getY() >= getY() + getBbHeight() - 0.3) {
            return false;
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof SamHyde) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.SLIME_SQUISH_SMALL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT_SMALL;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH_SMALL;
    }

    @Override
    public float getVoicePitch() {
        return 0.6F + random.nextFloat() * 0.2F;
    }
}
