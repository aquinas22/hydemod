package com.hydemod.entity;

import com.hydemod.registry.ModEntities;
import com.hydemod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.EquipmentSlot;
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
import org.jspecify.annotations.Nullable;

/**
 * Sam Hyde, passionate childlike innovator, delivering "2070 Paradigm Shift" whether you like it or not.
 *
 * Abilities (one every few seconds while he has a target):
 *  - Trash Economy: hurls a volley of trash cubes.
 *  - "Close your eyes": blinds everyone nearby.
 *  - Depth Grovlers: calls up underground toilers from the tiered cities.
 *  - Maglev to the Moon: launches his target into the sky.
 *  - Bulletproof Coffee: gets all jagged up (speed + strength).
 *  - Pat on the back: heals himself.
 *  - Once, under 25% health: "Can we reset the clock?" - heals a big chunk.
 */
public class SamHyde extends HydeBoss {
    private int abilityCooldown = 60;
    private boolean usedResetClock;
    private boolean introduced;

    public SamHyde(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, DepthGrovler.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.TED_MICROPHONE.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        return result;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!introduced) {
            introduced = true;
            say("boss.hydemod.sam_hyde.intro");
        }
        if (!usedResetClock && getHealth() < getMaxHealth() * 0.25F) {
            usedResetClock = true;
            say("boss.hydemod.sam_hyde.reset_clock");
            heal(getMaxHealth() * 0.3F);
            addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 10, 1));
            level.playSound(null, blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 3.0F, 0.8F);
            abilityCooldown = 60;
            return;
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (--abilityCooldown > 0) {
            return;
        }
        boolean enraged = getHealth() < getMaxHealth() * 0.5F;
        abilityCooldown = (enraged ? 60 : 90) + random.nextInt(40);
        switch (random.nextInt(7)) {
            case 0, 1 -> trashVolley(level, target, enraged ? 5 : 3);
            case 2 -> closeYourEyes(level);
            case 3 -> summonGrovlers(level);
            case 4 -> maglev(level, target);
            case 5 -> bulletproofCoffee();
            default -> patOnTheBack(level);
        }
    }

    private void trashVolley(ServerLevel level, LivingEntity target, int count) {
        say("boss.hydemod.sam_hyde.trash");
        for (int i = 0; i < count; i++) {
            ThrownTrashCube cube = new ThrownTrashCube(level, this, new ItemStack(ModItems.TRASH_CUBE.get())).withDamage(7.0F);
            double dx = target.getX() - getX();
            double dy = target.getY(0.33) - cube.getY();
            double dz = target.getZ() - getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            cube.shoot(dx, dy + dist * 0.2, dz, 1.3F, 6.0F + i * 2.0F);
            level.addFreshEntity(cube);
        }
        level.playSound(null, blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.0F, 0.5F);
    }

    private void closeYourEyes(ServerLevel level) {
        say("boss.hydemod.sam_hyde.close_eyes");
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(20.0))) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 0));
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 7, 0));
        }
        level.playSound(null, blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.2F);
    }

    private void summonGrovlers(ServerLevel level) {
        int existing = level.getEntitiesOfClass(DepthGrovler.class, getBoundingBox().inflate(24.0)).size();
        if (existing >= 8) {
            bulletproofCoffee();
            return;
        }
        say("boss.hydemod.sam_hyde.grovlers");
        for (int i = 0; i < 4; i++) {
            BlockPos pos = blockPosition().offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
            DepthGrovler growler = ModEntities.DEPTH_GROVLER.get().spawn(level, pos, EntitySpawnReason.MOB_SUMMONED);
            if (growler != null) {
                growler.setTarget(getTarget());
                level.sendParticles(ParticleTypes.LARGE_SMOKE, growler.getX(), growler.getY() + 1, growler.getZ(), 15, 0.3, 0.6, 0.3, 0.02);
            }
        }
    }

    private void maglev(ServerLevel level, LivingEntity target) {
        say("boss.hydemod.sam_hyde.maglev");
        target.setDeltaMovement(target.getDeltaMovement().x, 1.6, target.getDeltaMovement().z);
        target.hurtMarked = true;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(), target.getZ(), 40, 0.4, 0.2, 0.4, 0.4);
        level.playSound(null, target.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    private void bulletproofCoffee() {
        say("boss.hydemod.sam_hyde.coffee");
        addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 8, 1));
        addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 8, 0));
        playSound(SoundEvents.GENERIC_DRINK.value(), 1.0F, 1.0F);
    }

    private void patOnTheBack(ServerLevel level) {
        say("boss.hydemod.sam_hyde.pat");
        heal(25.0F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.5, getZ(), 20, 0.5, 0.6, 0.5, 0.0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof DepthGrovler) {
            return false; // his own toilers can't hurt him
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && random.nextInt(12) == 0 && isAlive()) {
            sayRandom("boss.hydemod.sam_hyde.hurt", 6);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        say("boss.hydemod.sam_hyde.death");
        super.die(source);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 600 == 300 && getTarget() != null) {
            sayRandom("boss.hydemod.sam_hyde.taunt", 10);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.7F;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("UsedResetClock", usedResetClock);
        output.putBoolean("Introduced", introduced);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        usedResetClock = input.getBooleanOr("UsedResetClock", false);
        introduced = input.getBooleanOr("Introduced", false);
    }
}
