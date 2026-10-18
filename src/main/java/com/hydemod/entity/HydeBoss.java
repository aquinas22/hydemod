package com.hydemod.entity;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Shared boss plumbing: boss bar, persistence, chat speeches, title cards, a short invulnerable
 * intro, a phase-two transition at half health, and synced attack animations for the renderer.
 */
public abstract class HydeBoss extends Monster {
    public static final int INTRO_TICKS = 60;
    /** Packed as (sequence << 8) | animationId so repeating the same attack restarts the animation. */
    private static final EntityDataAccessor<Integer> DATA_ANIM = SynchedEntityData.defineId(HydeBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INTRO = SynchedEntityData.defineId(HydeBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PHASE_TWO = SynchedEntityData.defineId(HydeBoss.class, EntityDataSerializers.BOOLEAN);

    protected final ServerBossEvent bossEvent;
    protected boolean phaseTwo;
    private boolean introduced;
    private int animSeq;
    // client-side animation clock
    private int clientAnimStart;

    protected HydeBoss(EntityType<? extends Monster> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), this.getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
        this.bossEvent.setDarkenScreen(true);
        this.bossEvent.setCreateWorldFog(true);
        this.xpReward = 250;
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM, 0);
        builder.define(DATA_INTRO, 0);
        builder.define(DATA_PHASE_TWO, false);
    }

    // ------------------------------------------------------------------ animations

    /** Starts attack animation {@code id} (0 = none) on all clients. */
    protected void playAnim(int id) {
        animSeq = (animSeq + 1) & 0xFFFF;
        entityData.set(DATA_ANIM, (animSeq << 8) | id);
    }

    public int getAnimId() {
        return entityData.get(DATA_ANIM) & 0xFF;
    }

    /** Ticks since the current animation started (client side). */
    public float getAnimTime(float partialTick) {
        return tickCount - clientAnimStart + partialTick;
    }

    public int getIntroTicksLeft() {
        return entityData.get(DATA_INTRO);
    }

    public boolean isPhaseTwoClient() {
        return entityData.get(DATA_PHASE_TWO);
    }

    /** Boss bar colour used once phase two has started. */
    protected abstract BossEvent.BossBarColor phaseTwoColor();

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_ANIM.equals(accessor)) {
            clientAnimStart = tickCount;
        }
    }

    // ------------------------------------------------------------------ speech & titles

    /** Broadcasts "&lt;Name&gt; line" to every player within 48 blocks. */
    protected void say(String key, Object... args) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Component line = Component.translatable("chat.hydemod.speaker", getDisplayName(), Component.translatable(key, args))
                .withStyle(ChatFormatting.YELLOW);
        forNearbyPlayers(serverLevel, p -> p.sendSystemMessage(line));
    }

    protected void sayRandom(String prefix, int count) {
        say(prefix + "." + (1 + random.nextInt(count)));
    }

    protected void showTitle(ServerLevel level, Component title, Component subtitle) {
        forNearbyPlayers(level, p -> {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 15));
            p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            p.connection.send(new ClientboundSetTitleTextPacket(title));
        });
    }

    protected void forNearbyPlayers(ServerLevel level, Consumer<ServerPlayer> action) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 64 * 64) {
                action.accept(player);
            }
        }
    }

    protected void playBossSound(SoundEvent sound, float volume, float pitch) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Short, translated dodge instructions stay visible without filling the chat log. */
    protected void warnPlayers(ServerLevel level, String key) {
        forNearbyPlayers(level, p -> {
            if (isFightable(p)) {
                p.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.GOLD));
            }
        });
    }

    protected boolean isPerformingMove() {
        return getIntroTicksLeft() > 0;
    }

    protected void warningRing(ServerLevel level, double x, double y, double z, double radius,
                               net.minecraft.core.particles.SimpleParticleType particle) {
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2 * i / 24;
            level.sendParticles(particle, x + Math.cos(angle) * radius, y + 0.12,
                    z + Math.sin(angle) * radius, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    // ------------------------------------------------------------------ hooks for subclasses

    /** Called once when the boss first ticks: titles, sounds and a dramatic entrance. */
    protected abstract void onIntro(ServerLevel level);

    /** Called once when health first drops to half. */
    protected abstract void onPhaseTwo(ServerLevel level);

    /** Boss AI that only runs once the intro is over. */
    protected abstract void bossTick(ServerLevel level);

    // ------------------------------------------------------------------ ticking

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (!introduced) {
            introduced = true;
            entityData.set(DATA_INTRO, INTRO_TICKS);
            onIntro(level);
        }
        int intro = entityData.get(DATA_INTRO);
        if (intro > 0) {
            entityData.set(DATA_INTRO, intro - 1);
            getNavigation().stop();
            setTarget(null);
            if (intro % 4 == 0) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + getBbHeight() * 0.5, getZ(), 30,
                        getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.15);
            }
            return;
        }
        if (!phaseTwo && getHealth() <= getMaxHealth() * 0.5F) {
            phaseTwo = true;
            entityData.set(DATA_PHASE_TWO, true);
            bossEvent.setColor(phaseTwoColor());
            onPhaseTwo(level);
            return;
        }
        bossTick(level);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (entityData.get(DATA_INTRO) > 0 && !source.isCreativePlayer()) {
            return false; // still making an entrance
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            bossEvent.setProgress(0.0F);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 2, 0.5, 0.5, 0.5, 0.0);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1.5, getZ(), 120, 1.0, 1.5, 1.0, 0.6);
            level.sendParticles(ParticleTypes.FIREWORK, getX(), getY() + 2, getZ(), 80, 1.5, 1.5, 1.5, 0.25);
            showTitle(level, Component.translatable("title.hydemod.defeated").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    getDisplayName().copy().withStyle(ChatFormatting.YELLOW));
        }
    }

    // ------------------------------------------------------------------ boss bar & misc

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Introduced", introduced);
        output.putBoolean("PhaseTwo", phaseTwo);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        introduced = input.getBooleanOr("Introduced", false);
        phaseTwo = input.getBooleanOr("PhaseTwo", false);
        entityData.set(DATA_PHASE_TWO, phaseTwo);
        if (phaseTwo) {
            bossEvent.setColor(phaseTwoColor());
        }
        if (hasCustomName()) {
            bossEvent.setName(getDisplayName());
        }
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    /** Players in creative/spectator don't count for "anyone nearby?" checks. */
    protected static boolean isFightable(Player p) {
        return !p.isCreative() && !p.isSpectator();
    }
}
