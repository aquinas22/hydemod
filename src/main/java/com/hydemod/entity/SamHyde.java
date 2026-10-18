package com.hydemod.entity;

import com.hydemod.registry.ModEntities;
import com.hydemod.registry.ModItems;
import com.hydemod.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** A two-phase encounter with readable casts, dodgeable hazards and interruptible healing. */
public class SamHyde extends HydeBoss {
    public static final int ANIM_THROW = 1, ANIM_CLOSE_EYES = 2, ANIM_SUMMON = 3, ANIM_MAGLEV = 4,
            ANIM_COFFEE = 5, ANIM_PAT = 6, ANIM_RESET = 7, ANIM_RAIN = 8, ANIM_INTRO = 9;
    private final List<Integer> attackDeck = new ArrayList<>();
    private int abilityCooldown = 40, pendingMove, windupTicks, recoveryTicks, lastMove;
    private int volleyLeft, volleyDelay, trashRainTicks, healingCooldown, voiceTicks;
    private float castDamage;
    private boolean usedResetClock;
    private Vec3 aim = Vec3.ZERO;
    private @Nullable String currentVoice;

    public SamHyde(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 360.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 8.0).add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected boolean isPerformingMove() {
        return super.isPerformingMove() || pendingMove != 0 || recoveryTicks > 0 || volleyLeft > 0 || trashRainTicks > 0;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true) {
            @Override public boolean canUse() { return !isPerformingMove() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !isPerformingMove() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override public boolean canUse() { return !isPerformingMove() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !isPerformingMove() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, DepthGrovler.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                   EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.TED_MICROPHONE.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        return result;
    }

    @Override
    protected void onIntro(ServerLevel level) {
        playAnim(ANIM_INTRO);
        showTitle(level, Component.translatable("title.hydemod.sam_hyde").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                Component.translatable("title.hydemod.sam_hyde.sub").withStyle(ChatFormatting.RED));
        playBossSound(ModSounds.BOSS_INTRO.get(), 1.8F, 1.0F);
        say("boss.hydemod.sam_hyde.intro");
    }

    @Override
    protected void onPhaseTwo(ServerLevel level) {
        pendingMove = 0;
        volleyLeft = trashRainTicks = 0;
        attackDeck.clear();
        playAnim(ANIM_RESET);
        showTitle(level, Component.translatable("title.hydemod.paradigm_shift").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                Component.translatable("title.hydemod.paradigm_shift.sub").withStyle(ChatFormatting.GOLD));
        playBossSound(ModSounds.BOSS_PHASE.get(), 1.8F, 1.0F);
        stopVoice(level);
        say("boss.hydemod.sam_hyde.phase_two");
        for (int i = 0; i < 4; i++) {
            LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt != null) {
                bolt.snapTo(getX() + random.nextInt(9) - 4, getY(), getZ() + random.nextInt(9) - 4);
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
        }
        addEffect(new MobEffectInstance(MobEffects.SPEED, -1, 0, false, true));
        recoveryTicks = 50; // time to read the transition and reposition
        abilityCooldown = 25;
        getNavigation().stop();
    }

    @Override protected BossEvent.BossBarColor phaseTwoColor() { return BossEvent.BossBarColor.PURPLE; }

    @Override
    protected void bossTick(ServerLevel level) {
        if (healingCooldown > 0) healingCooldown--;
        if (voiceTicks > 0) voiceTicks--;
        if (phaseTwo && tickCount % 6 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.2, getZ(), 3, 0.5, 0.8, 0.5, 0.3);
        }
        LivingEntity target = getTarget();
        if (pendingMove != 0) {
            getNavigation().stop();
            if (target == null || !target.isAlive()) { pendingMove = 0; return; }
            getLookControl().setLookAt(target, 30, 30);
            if (windupTicks % 4 == 0) {
                if (pendingMove == ANIM_MAGLEV || pendingMove == ANIM_RAIN) {
                    warningRing(level, aim.x, aim.y, aim.z, pendingMove == ANIM_RAIN ? 5 : 2.5, ParticleTypes.ELECTRIC_SPARK);
                } else {
                    warningRing(level, getX(), getY(), getZ(), 1.8, ParticleTypes.END_ROD);
                }
            }
            if (--windupTicks <= 0) {
                int move = pendingMove;
                pendingMove = 0;
                executeMove(level, target, move);
                recoveryTicks = move == ANIM_PAT || move == ANIM_RESET ? 35 : 18;
            }
            return;
        }
        if (volleyLeft > 0) {
            if (--volleyDelay <= 0) { throwCube(level); volleyDelay = 6; volleyLeft--; }
            return;
        }
        if (trashRainTicks > 0) { tickTrashRain(level); return; }
        if (recoveryTicks > 0) {
            getNavigation().stop();
            recoveryTicks--;
            if (recoveryTicks % 5 == 0) level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 2.3, getZ(), 2, 0.25, 0.1, 0.25, 0);
            return;
        }
        if (target == null || !target.isAlive() || !hasLineOfSight(target) || distanceToSqr(target) > 40 * 40) return;
        if (!usedResetClock && getHealth() < getMaxHealth() * 0.2F && voiceTicks <= 0) {
            usedResetClock = true;
            beginMove(level, target, ANIM_RESET);
            return;
        }
        if (--abilityCooldown > 0 || voiceTicks > 0) return;
        abilityCooldown = (phaseTwo ? 32 : 48) + random.nextInt(20);
        if (attackDeck.isEmpty()) {
            attackDeck.addAll(List.of(ANIM_THROW, ANIM_CLOSE_EYES, ANIM_SUMMON, ANIM_MAGLEV, ANIM_COFFEE));
            if (healingCooldown == 0 && getHealth() < getMaxHealth() * 0.7F) attackDeck.add(ANIM_PAT);
            if (phaseTwo) attackDeck.add(ANIM_RAIN);
        }
        int index = random.nextInt(attackDeck.size());
        if (attackDeck.size() > 1 && attackDeck.get(index) == lastMove) index = (index + 1) % attackDeck.size();
        lastMove = attackDeck.remove(index);
        beginMove(level, target, lastMove);
    }

    private void beginMove(ServerLevel level, LivingEntity target, int move) {
        pendingMove = move;
        castDamage = 0;
        aim = target.position(); // lock the warning location; sidestepping is meaningful
        windupTicks = switch (move) {
            case ANIM_RESET -> 60;
            case ANIM_PAT -> 45;
            case ANIM_MAGLEV, ANIM_RAIN -> phaseTwo ? 26 : 34;
            default -> phaseTwo ? 22 : 30;
        };
        playAnim(move);
        String action = switch (move) {
            case ANIM_THROW -> "trash"; case ANIM_CLOSE_EYES -> "close_eyes";
            case ANIM_SUMMON -> "grovlers"; case ANIM_MAGLEV -> "maglev";
            case ANIM_COFFEE -> "coffee"; case ANIM_PAT -> "pat";
            case ANIM_RESET -> "reset_clock"; default -> "trash_rain";
        };
        say("boss.hydemod.sam_hyde." + action);
        warnPlayers(level, "warning.hydemod.sam." + action);
        playBossSound(ModSounds.BOSS_TELEGRAPH.get(), 0.8F, move == ANIM_RESET ? 0.75F : 1.1F);
        getNavigation().stop();
    }

    private void executeMove(ServerLevel level, LivingEntity target, int move) {
        switch (move) {
            case ANIM_THROW -> { volleyLeft = phaseTwo ? 5 : 3; volleyDelay = 1; }
            case ANIM_CLOSE_EYES -> {
                for (Player player : fighters(level, 14)) {
                    Vec3 toSam = getEyePosition().subtract(player.getEyePosition()).normalize();
                    if (hasLineOfSight(player) && player.getLookAngle().dot(toSam) > 0.35) {
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0));
                    }
                }
                playBossSound(ModSounds.BOSS_TELEGRAPH.get(), 1.0F, 0.7F);
            }
            case ANIM_SUMMON -> summonGrovlers(level, target);
            case ANIM_MAGLEV -> {
                for (Player player : fighters(level, 24)) {
                    if (horizontalDistance(player.position(), aim) <= 2.5 && Math.abs(player.getY() - aim.y) < 4) {
                        player.setDeltaMovement(player.getDeltaMovement().x, 1.15, player.getDeltaMovement().z);
                        player.hurtMarked = true;
                        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 65, 0));
                    }
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, aim.x, aim.y, aim.z, 45, 1.5, 0.2, 1.5, 0.3);
                level.playSound(null, BlockPos.containing(aim), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 1, 0.7F);
            }
            case ANIM_COFFEE -> {
                addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
                addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0));
                playSound(SoundEvents.GENERIC_DRINK.value(), 1, 1);
            }
            case ANIM_PAT, ANIM_RESET -> {
                heal(move == ANIM_RESET ? 45 : 16);
                healingCooldown = 400;
                playBossSound(ModSounds.BOSS_APPLAUSE.get(), 1.2F, 1.0F);
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1, getZ(), 45, 0.5, 1, 0.5, 0.3);
            }
            case ANIM_RAIN -> trashRainTicks = 65;
            default -> { }
        }
    }

    private void throwCube(ServerLevel level) {
        ThrownTrashCube cube = new ThrownTrashCube(level, this, new ItemStack(ModItems.TRASH_CUBE.get())).withDamage(phaseTwo ? 6 : 5);
        double dx = aim.x - getX(), dz = aim.z - getZ();
        cube.shoot(dx, aim.y + 0.6 - cube.getY() + Math.sqrt(dx * dx + dz * dz) * 0.16, dz, 1.2F, 3.0F);
        level.addFreshEntity(cube);
        playBossSound(SoundEvents.SNOWBALL_THROW, 0.8F, 0.6F + volleyLeft * 0.1F);
    }

    private void tickTrashRain(ServerLevel level) {
        trashRainTicks--;
        if (trashRainTicks % 8 != 0) return;
        warningRing(level, aim.x, aim.y, aim.z, 5, ParticleTypes.ELECTRIC_SPARK);
        for (int i = 0; i < 3; i++) {
            double angle = random.nextDouble() * Math.PI * 2, radius = random.nextDouble() * 5;
            ThrownTrashCube cube = new ThrownTrashCube(level, this, new ItemStack(ModItems.TRASH_CUBE.get())).withDamage(5);
            cube.setPos(aim.x + Math.cos(angle) * radius, aim.y + 10, aim.z + Math.sin(angle) * radius);
            cube.setDeltaMovement(0, -0.5, 0);
            level.addFreshEntity(cube);
        }
    }

    private void summonGrovlers(ServerLevel level, LivingEntity target) {
        int existing = level.getEntitiesOfClass(DepthGrovler.class, getBoundingBox().inflate(32)).size();
        int count = Math.min(phaseTwo ? 3 : 2, 5 - existing);
        for (int i = 0; i < count; i++) {
            BlockPos pos = blockPosition().offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                    || !level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) continue;
            DepthGrovler grovler = ModEntities.DEPTH_GROVLER.get().spawn(level, pos, EntitySpawnReason.MOB_SUMMONED);
            if (grovler != null) {
                grovler.setTarget(target);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, grovler.getX(), grovler.getY() + 0.5, grovler.getZ(), 12, 0.3, 0.3, 0.3, 0.02);
            }
        }
        playBossSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0F, 0.8F);
    }

    private List<Player> fighters(ServerLevel level, double radius) {
        return level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius), HydeBoss::isFightable);
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) { return Math.hypot(a.x - b.x, a.z - b.z); }

    @Override
    protected void say(String key, Object... args) {
        super.say(key, args);
        SoundEvent voice = ModSounds.samVoice(key);
        if (voice != null && voiceTicks <= 0 && !level().isClientSide()) {
            playBossSound(voice, 1.6F, 1.0F);
            currentVoice = key.replace("boss.hydemod.sam_hyde.", "");
            voiceTicks = ModSounds.samVoiceTicks(key);
        }
    }

    private void stopVoice(ServerLevel level) {
        if (currentVoice != null && voiceTicks > 0) {
            var packet = new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                    com.hydemod.HydeMod.id("sam.voice." + currentVoice), SoundSource.HOSTILE);
            forNearbyPlayers(level, p -> p.connection.send(packet));
        }
        currentVoice = null;
        voiceTicks = 0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof DepthGrovler || source.getEntity() == this) return false;
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && isAlive()) {
            if ((pendingMove == ANIM_PAT || pendingMove == ANIM_RESET) && source.getEntity() instanceof LivingEntity) {
                castDamage += damage;
                if (castDamage >= 12) {
                    pendingMove = 0;
                    recoveryTicks = 45;
                    healingCooldown = 400;
                    playAnim(0);
                    warnPlayers(level, "warning.hydemod.sam.interrupted");
                    playBossSound(ModSounds.BOSS_STAGGER.get(), 1.2F, 1);
                    level.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.4, getZ(), 24, 0.5, 0.6, 0.5, 0.2);
                }
            } else if (random.nextInt(16) == 0 && voiceTicks <= 0) sayRandom("boss.hydemod.sam_hyde.hurt", 6);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel level) stopVoice(level);
        say("boss.hydemod.sam_hyde.death");
        super.die(source);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive() && tickCount % 480 == 300 && getTarget() != null && !isPerformingMove() && voiceTicks <= 0) {
            sayRandom("boss.hydemod.sam_hyde.taunt", 10);
        }
    }

    @Override protected @Nullable SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.PLAYER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PLAYER_DEATH; }
    @Override public float getVoicePitch() { return 1.0F; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("UsedResetClock", usedResetClock);
        output.putInt("HealingCooldown", healingCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        usedResetClock = input.getBooleanOr("UsedResetClock", false);
        healingCooldown = input.getIntOr("HealingCooldown", 0);
    }
}
