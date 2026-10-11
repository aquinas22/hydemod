package com.hydemod.client;

import com.hydemod.entity.ElephantMario;
import com.hydemod.entity.HydeBoss;
import com.hydemod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

/**
 * Plays a looping battle theme while a HydeMod boss is alive nearby, silencing the vanilla
 * soundtrack, and fades out when the fight ends.
 */
public final class BossMusic {
    private static @Nullable Track current;

    private static final class Track extends AbstractTickableSoundInstance {
        private final SoundEvent event;
        private boolean fading;
        private int age;

        Track(SoundEvent event) {
            super(event, SoundSource.MUSIC, RandomSource.create());
            this.event = event;
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = 0.05F;
        }

        @Override
        public void tick() {
            age++;
            if (fading) {
                volume -= 0.03F;
                if (volume <= 0.0F) {
                    stop();
                }
            } else if (volume < 0.85F) {
                volume = Math.min(0.85F, volume + 0.05F);
            }
        }
    }

    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            current = null; // disconnect stops every sound without marking it stopped
            return;
        }
        if (current != null && (current.isStopped() || current.age > 20 && !mc.getSoundManager().isActive(current))) {
            current = null; // sound engine dropped it (F3+T, device change, ...): start fresh
        }
        HydeBoss boss = null;
        if (mc.level != null && mc.player != null) {
            double best = 64 * 64;
            for (HydeBoss b : mc.level.getEntitiesOfClass(HydeBoss.class, mc.player.getBoundingBox().inflate(64.0), HydeBoss::isAlive)) {
                double d = b.distanceToSqr(mc.player);
                if (d < best) {
                    best = d;
                    boss = b;
                }
            }
        }
        SoundEvent wanted = boss == null ? null
                : (boss instanceof ElephantMario ? ModSounds.MUSIC_ELEPHANT_MARIO.get() : ModSounds.MUSIC_SAM_HYDE.get());
        if (current != null && (wanted == null || current.event != wanted)) {
            current.fading = true;
            if (current.isStopped()) {
                current = null;
            }
        }
        if (wanted != null) {
            mc.getMusicManager().stopPlaying(); // the boss theme takes over
            if (current == null || current.isStopped() || current.fading && current.event != wanted) {
                current = new Track(wanted);
                mc.getSoundManager().play(current);
            } else if (current.fading && current.event == wanted) {
                current.fading = false;
            }
        }
    }

    private BossMusic() {}
}
