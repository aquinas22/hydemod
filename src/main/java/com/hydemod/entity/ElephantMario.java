package com.hydemod.entity;

import com.hydemod.HydeMod;
import com.hydemod.registry.ModItems;
import com.hydemod.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Elephant Mario: a huge elephant plumber who ate one fruit too many.
 *
 * Phase 1:
 *  - Ground Pound: leaps up and slams down; an expanding shockwave ring damages and launches you.
 *  - Trunk Spray: fires blasts of water from his trunk.
 *  - Goober Barrage: flings handfuls of Goobers (chocolate-covered peanuts) at you.
 *  - Snack Break: munches a box of Goobers to heal.
 *  - Trunk Charge: dashes at his target.
 * Phase 2 (half health) - ELEPHANT POWER: grows 40% bigger, faster, stronger; ground pounds come in
 * triple stomps with bigger shockwaves.
 */
public class ElephantMario extends HydeBoss {
    public static final int ANIM_POUND = 1, ANIM_SPRAY = 2, ANIM_THROW = 3, ANIM_SNACK = 4, ANIM_CHARGE = 5, ANIM_ROAR = 6, ANIM_INTRO = 9;
    private static final net.minecraft.resources.Identifier POWER_ID = HydeMod.id("elephant_power");

    private int abilityCooldown = 40;
    private boolean pounding;
    private int poundTicks;
    private int poundsLeft;
    // expanding shockwave ring
    private int ringTicks = -1;
    private double ringX, ringY, ringZ;
    private float ringMaxRadius;

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
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ------------------------------------------------------------------ phases

    @Override
    protected void onIntro(ServerLevel level) {
        playAnim(ANIM_INTRO);
        showTitle(level, Component.translatable("title.hydemod.elephant_mario").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                Component.translatable("title.hydemod.elephant_mario.sub").withStyle(ChatFormatting.AQUA));
        playBossSound(ModSounds.BOSS_INTRO.get(), 3.0F, 1.1F);
        playBossSound(ModSounds.BOSS_TRUMPET.get(), 4.0F, 1.0F);
        say("boss.hydemod.elephant_mario.intro");
    }

    @Override
    protected void onPhaseTwo(ServerLevel level) {
        playAnim(ANIM_ROAR);
        showTitle(level, Component.translatable("title.hydemod.elephant_power").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                Component.translatable("title.hydemod.elephant_power.sub").withStyle(ChatFormatting.GOLD));
        playBossSound(ModSounds.BOSS_PHASE.get(), 3.0F, 1.0F);
        playBossSound(ModSounds.BOSS_TRUMPET.get(), 5.0F, 0.75F);
        say("boss.hydemod.elephant_mario.phase_two");
        addModifier(Attributes.SCALE, 0.4);
        addModifier(Attributes.MOVEMENT_SPEED, 0.06);
        addModifier(Attributes.ATTACK_DAMAGE, 6.0);
        addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 4, 2));
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.5, getZ(), 8, 1.5, 1.0, 1.5, 0.0);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 3, getZ(), 12, 1.0, 0.5, 1.0, 0.0);
        startRing(getX(), getY(), getZ(), 10.0F);
        abilityCooldown = 40;
    }

    private void addModifier(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double amount) {
        AttributeInstance inst = getAttribute(attribute);
        if (inst != null && !inst.hasModifier(POWER_ID)) {
            inst.addPermanentModifier(new AttributeModifier(POWER_ID, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    protected BossEvent.BossBarColor phaseTwoColor() {
        return BossEvent.BossBarColor.PINK;
    }

    @Override
    protected void bossTick(ServerLevel level) {
        tickRing(level);
        if (phaseTwo && tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + getBbHeight() + 0.3, getZ(), 1, 0.4, 0.1, 0.4, 0.0);
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
        if (poundsLeft > 0 && target != null) {
            groundPound(level, target, false);
            return;
        }
        if (target == null || !target.isAlive() || --abilityCooldown > 0) {
            return;
        }
        abilityCooldown = (phaseTwo ? 45 : 75) + random.nextInt(35);
        switch (random.nextInt(6)) {
            case 0, 1 -> groundPound(level, target, true);
            case 2 -> trunkSpray(level, target, phaseTwo ? 5 : 3);
            case 3 -> gooberBarrage(level, target, phaseTwo ? 9 : 5);
            case 4 -> snackBreak(level);
            default -> trunkCharge(target);
        }
    }

    // ------------------------------------------------------------------ abilities

    private void groundPound(ServerLevel level, LivingEntity target, boolean first) {
        if (first) {
            say("boss.hydemod.elephant_mario.pound");
            poundsLeft = phaseTwo ? 2 : 0; // phase two: triple stomp
        } else {
            poundsLeft--;
        }
        playAnim(ANIM_POUND);
        Vec3 toward = target.position().subtract(position()).multiply(0.08, 0, 0.08);
        setDeltaMovement(toward.x, phaseTwo ? 1.35 : 1.2, toward.z);
        hurtMarked = true;
        pounding = true;
        poundTicks = 0;
        level.playSound(null, blockPosition(), SoundEvents.GOAT_LONG_JUMP, SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    private void land(ServerLevel level) {
        pounding = false;
        playBossSound(ModSounds.BOSS_BOOM.get(), 4.0F, phaseTwo ? 0.8F : 1.0F);
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.3, getZ(), 4, 1.5, 0.2, 1.5, 0.0);
        float radius = phaseTwo ? 9.0F : 6.5F;
        startRing(getX(), getY(), getZ(), radius);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 2.5, radius),
                e -> e != this && net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e))) {
            double dist = victim.distanceTo(this);
            if (dist > radius) {
                continue;
            }
            float dmg = (float) Math.max(4.0, (phaseTwo ? 20.0 : 16.0) - dist * 2.0);
            victim.hurtServer(level, damageSources().mobAttack(this), dmg);
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.setDeltaMovement(away.x * 1.2, 0.9, away.z * 1.2);
            victim.hurtMarked = true;
        }
    }

    private void startRing(double x, double y, double z, float maxRadius) {
        ringX = x; ringY = y; ringZ = z; ringMaxRadius = maxRadius; ringTicks = 0;
    }

    /** Draws a shockwave ring that expands outward over 10 ticks, kicking up the ground's own block particles. */
    private void tickRing(ServerLevel level) {
        if (ringTicks < 0) {
            return;
        }
        float r = ringMaxRadius * (ringTicks + 1) / 10.0F;
        BlockState ground = level.getBlockState(BlockPos.containing(ringX, ringY - 0.5, ringZ));
        int points = (int) (r * 10);
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            double x = ringX + Math.cos(a) * r, z = ringZ + Math.sin(a) * r;
            level.sendParticles(ParticleTypes.CLOUD, x, ringY + 0.15, z, 1, 0.05, 0.02, 0.05, 0.01);
            if (!ground.isAir() && i % 2 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), x, ringY + 0.2, z, 2, 0.1, 0.1, 0.1, 0.15);
            }
        }
        if (++ringTicks >= 10) {
            ringTicks = -1;
        }
    }

    private void trunkSpray(ServerLevel level, LivingEntity target, int count) {
        playAnim(ANIM_SPRAY);
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
        playBossSound(ModSounds.BOSS_TRUMPET.get(), 2.5F, 1.25F);
    }

    private void gooberBarrage(ServerLevel level, LivingEntity target, int count) {
        playAnim(ANIM_THROW);
        say("boss.hydemod.elephant_mario.goobers");
        for (int i = 0; i < count; i++) {
            ThrownTrashCube candy = new ThrownTrashCube(level, this, new ItemStack(ModItems.GOOBERS.get())).withDamage(4.0F);
            candy.setPos(getX(), getEyeY() - 0.4, getZ());
            double dx = target.getX() - getX();
            double dy = target.getY(0.4) - candy.getY();
            double dz = target.getZ() - getZ();
            candy.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.15, dz, 1.2F, 12.0F);
            level.addFreshEntity(candy);
        }
        level.playSound(null, blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.5F, 0.8F);
    }

    private void snackBreak(ServerLevel level) {
        if (getHealth() > getMaxHealth() * 0.85F) {
            trunkCharge(getTarget());
            return;
        }
        playAnim(ANIM_SNACK);
        say("boss.hydemod.elephant_mario.snack");
        heal(phaseTwo ? 20.0F : 30.0F);
        playSound(SoundEvents.GENERIC_EAT.value(), 2.0F, 0.6F);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(new ItemStack(ModItems.GOOBERS.get()))),
                getX(), getEyeY() - 0.5, getZ(), 25, 0.4, 0.3, 0.4, 0.1);
    }

    private void trunkCharge(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        playAnim(ANIM_CHARGE);
        say("boss.hydemod.elephant_mario.charge");
        Vec3 dir = target.position().subtract(position()).normalize();
        float power = phaseTwo ? 2.2F : 1.8F;
        setDeltaMovement(dir.x * power, 0.3, dir.z * power);
        hurtMarked = true;
        playBossSound(ModSounds.BOSS_TRUMPET.get(), 3.0F, 1.4F);
    }

    // ------------------------------------------------------------------ combat

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() == this) {
            return false; // own goobers / spray / pounds
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
        playBossSound(ModSounds.BOSS_TRUMPET.get(), 4.0F, 0.55F);
        super.die(source);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.BOSS_TRUMPET.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
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
        return 1.0F + Mth.nextFloat(random, -0.1F, 0.1F);
    }
}
