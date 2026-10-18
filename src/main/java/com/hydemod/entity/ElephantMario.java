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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private int pendingMove, windupTicks, recoveryTicks, chargeTicks, healingCooldown, lastMove;
    private float snackDamage;
    private Vec3 chargeDirection = Vec3.ZERO;
    private final Set<UUID> chargeHits = new HashSet<>();
    private final Set<UUID> ringHits = new HashSet<>();
    private boolean ringDamaging;
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
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true) {
            @Override public boolean canUse() { return !isPerformingMove() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !isPerformingMove() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7) {
            @Override public boolean canUse() { return !isPerformingMove() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !isPerformingMove() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected boolean isPerformingMove() {
        return super.isPerformingMove() || pounding || pendingMove != 0 || recoveryTicks > 0 || chargeTicks > 0;
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
        pendingMove = chargeTicks = poundsLeft = 0;
        pounding = false;
        recoveryTicks = 50;
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
        if (healingCooldown > 0) healingCooldown--;
        if (phaseTwo && tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + getBbHeight() + 0.3, getZ(), 1, 0.4, 0.1, 0.4, 0.0);
        }
        if (pounding) {
            getNavigation().stop();
            poundTicks++;
            if (poundTicks > 8 && onGround()) {
                land(level);
            } else if (poundTicks > 100) {
                pounding = false;
                poundsLeft = 0;
                recoveryTicks = 30;
            }
            return;
        }
        LivingEntity target = getTarget();
        if (pendingMove != 0) {
            getNavigation().stop();
            if (target == null || !target.isAlive()) { pendingMove = 0; return; }
            if (windupTicks % 4 == 0) {
                warningRing(level, getX(), getY(), getZ(), 2.2, ParticleTypes.END_ROD);
                if (pendingMove == ANIM_CHARGE) {
                    for (int i = 2; i < 12; i += 2) level.sendParticles(ParticleTypes.CLOUD,
                            getX() + chargeDirection.x * i, getY() + 0.15, getZ() + chargeDirection.z * i,
                            2, 0.2, 0.02, 0.2, 0);
                }
            }
            if (--windupTicks <= 0) {
                int move = pendingMove;
                pendingMove = 0;
                switch (move) {
                    case ANIM_SPRAY -> trunkSpray(level, target, phaseTwo ? 5 : 3);
                    case ANIM_THROW -> gooberBarrage(level, target, phaseTwo ? 7 : 4);
                    case ANIM_SNACK -> snackBreak(level);
                    case ANIM_CHARGE -> {
                        chargeTicks = 16;
                        chargeHits.clear();
                        say("boss.hydemod.elephant_mario.charge");
                        playBossSound(ModSounds.BOSS_TRUMPET.get(), 2.0F, 1.3F);
                    }
                    default -> groundPound(level, target, true);
                }
                recoveryTicks = 25;
            }
            return;
        }
        if (chargeTicks > 0) {
            getNavigation().stop();
            chargeTicks--;
            if (horizontalCollision) {
                chargeTicks = 0;
                recoveryTicks = 50;
                playBossSound(ModSounds.BOSS_STAGGER.get(), 1.5F, 0.8F);
                warnPlayers(level, "warning.hydemod.elephant.staggered");
                return;
            }
            double power = phaseTwo ? 0.95 : 0.75;
            setDeltaMovement(chargeDirection.x * power, getDeltaMovement().y, chargeDirection.z * power);
            hurtMarked = true;
            for (Player victim : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.5), HydeBoss::isFightable)) {
                if (chargeHits.add(victim.getUUID())) {
                    victim.hurtServer(level, damageSources().mobAttack(this), phaseTwo ? 12 : 9);
                    victim.setDeltaMovement(chargeDirection.x, 0.45, chargeDirection.z);
                    victim.hurtMarked = true;
                }
            }
            return;
        }
        if (recoveryTicks > 0) { getNavigation().stop(); recoveryTicks--; return; }
        if (poundsLeft > 0 && target != null && target.isAlive()) {
            groundPound(level, target, false);
            return;
        }
        if (target == null || !target.isAlive() || !hasLineOfSight(target) || --abilityCooldown > 0) {
            return;
        }
        abilityCooldown = (phaseTwo ? 45 : 75) + random.nextInt(35);
        int move = 1 + random.nextInt(5);
        if (move == lastMove) move = move % 5 + 1;
        if (move == ANIM_SNACK && (healingCooldown > 0 || getHealth() > getMaxHealth() * 0.7F)) move = ANIM_SPRAY;
        lastMove = pendingMove = move;
        windupTicks = move == ANIM_SNACK ? 45 : phaseTwo ? 20 : 28;
        snackDamage = 0;
        chargeDirection = target.position().subtract(position()).multiply(1, 0, 1).normalize();
        playAnim(move);
        warnPlayers(level, "warning.hydemod.elephant." + switch (move) {
            case ANIM_POUND -> "pound"; case ANIM_SPRAY -> "spray"; case ANIM_THROW -> "goobers";
            case ANIM_SNACK -> "snack"; default -> "charge";
        });
        playBossSound(ModSounds.BOSS_TELEGRAPH.get(), 1, 0.8F);
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
        Vec3 toward = target.position().subtract(position()).multiply(1, 0, 1);
        if (toward.length() > 8) toward = toward.normalize().scale(8);
        toward = toward.scale(0.06);
        setDeltaMovement(toward.x, phaseTwo ? 1.35 : 1.2, toward.z);
        hurtMarked = true;
        pounding = true;
        poundTicks = 0;
        level.playSound(null, blockPosition(), SoundEvents.GOAT_LONG_JUMP, SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    private void land(ServerLevel level) {
        pounding = false;
        recoveryTicks = 24;
        playBossSound(ModSounds.BOSS_BOOM.get(), 2.0F, phaseTwo ? 0.8F : 1.0F);
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.3, getZ(), 4, 1.5, 0.2, 1.5, 0.0);
        float radius = phaseTwo ? 9.0F : 6.5F;
        startRing(getX(), getY(), getZ(), radius);
        ringDamaging = true;
        for (Player victim : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(1.8, 1, 1.8), HydeBoss::isFightable)) {
            double dist = victim.distanceTo(this);
            if (dist > 1.8) {
                continue;
            }
            float dmg = phaseTwo ? 12 : 9;
            ringHits.add(victim.getUUID());
            victim.hurtServer(level, damageSources().mobAttack(this), dmg);
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.setDeltaMovement(away.x * 1.2, 0.9, away.z * 1.2);
            victim.hurtMarked = true;
        }
    }

    private void startRing(double x, double y, double z, float maxRadius) {
        ringX = x; ringY = y; ringZ = z; ringMaxRadius = maxRadius; ringTicks = 0;
        ringHits.clear();
        ringDamaging = false;
    }

    /** The expanding shockwave reaches players over 18 ticks; jumping clears its low hit area. */
    private void tickRing(ServerLevel level) {
        if (ringTicks < 0) {
            return;
        }
        float r = ringMaxRadius * (ringTicks + 1) / 18.0F;
        float previous = ringMaxRadius * ringTicks / 18.0F;
        if (ringDamaging) {
            var region = new net.minecraft.world.phys.AABB(ringX - r - 1, ringY - 0.5, ringZ - r - 1,
                    ringX + r + 1, ringY + 2, ringZ + r + 1);
            for (Player victim : level.getEntitiesOfClass(Player.class, region, HydeBoss::isFightable)) {
                double distance = Math.hypot(victim.getX() - ringX, victim.getZ() - ringZ);
                if (distance >= previous - 0.4 && distance <= r + 0.4 && victim.getY() < ringY + 0.55
                        && victim.getY() > ringY - 0.5 && ringHits.add(victim.getUUID())) {
                    victim.hurtServer(level, damageSources().mobAttack(this), phaseTwo ? 8 : 6);
                    Vec3 away = victim.position().subtract(new Vec3(ringX, ringY, ringZ)).multiply(1, 0, 1).normalize();
                    victim.setDeltaMovement(away.x * 0.6, 0.45, away.z * 0.6);
                    victim.hurtMarked = true;
                }
            }
        }
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
        if (++ringTicks >= 18) {
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
        playAnim(ANIM_SNACK);
        say("boss.hydemod.elephant_mario.snack");
        heal(16.0F);
        healingCooldown = 400;
        playSound(SoundEvents.GENERIC_EAT.value(), 2.0F, 0.6F);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(new ItemStack(ModItems.GOOBERS.get()))),
                getX(), getEyeY() - 0.5, getZ(), 25, 0.4, 0.3, 0.4, 0.1);
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
        if (hurt && isAlive() && pendingMove == ANIM_SNACK && source.getEntity() instanceof LivingEntity) {
            snackDamage += damage;
            if (snackDamage >= 10) {
                pendingMove = 0;
                recoveryTicks = 45;
                healingCooldown = 400;
                playAnim(0);
                warnPlayers(level, "warning.hydemod.elephant.interrupted");
                playBossSound(ModSounds.BOSS_STAGGER.get(), 1, 1.2F);
            }
        }
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

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("HealingCooldown", healingCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        healingCooldown = input.getIntOr("HealingCooldown", 0);
    }
}
