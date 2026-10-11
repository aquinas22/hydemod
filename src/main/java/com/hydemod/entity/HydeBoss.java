package com.hydemod.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;

/** Shared boss plumbing: boss bar, persistence and chat speeches. */
public abstract class HydeBoss extends Monster {
    protected final ServerBossEvent bossEvent;

    protected HydeBoss(EntityType<? extends Monster> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), this.getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
        this.bossEvent.setDarkenScreen(false);
        this.xpReward = 150;
        this.setPersistenceRequired();
    }

    /** Broadcasts "&lt;Name&gt; line" to every player within 48 blocks. */
    protected void say(String key, Object... args) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Component line = Component.translatable("chat.hydemod.speaker", getDisplayName(), Component.translatable(key, args))
                .withStyle(ChatFormatting.YELLOW);
        for (Player player : serverLevel.players()) {
            if (player.distanceToSqr(this) < 48 * 48) {
                player.sendSystemMessage(line);
            }
        }
    }

    protected void sayRandom(String prefix, int count) {
        say(prefix + "." + (1 + random.nextInt(count)));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        bossEvent.setProgress(getHealth() / getMaxHealth());
    }

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
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
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
}
