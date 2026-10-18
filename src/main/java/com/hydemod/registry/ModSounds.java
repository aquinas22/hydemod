package com.hydemod.registry;

import com.hydemod.HydeMod;
import java.util.Map;
import java.util.HashMap;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, HydeMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SABER_IGNITE = register("lightsaber.ignite");
    public static final DeferredHolder<SoundEvent, SoundEvent> SABER_RETRACT = register("lightsaber.retract");
    public static final DeferredHolder<SoundEvent, SoundEvent> SABER_HUM = register("lightsaber.hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> SABER_SWING = register("lightsaber.swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> SABER_HIT = register("lightsaber.hit");

    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_TRUMPET = register("boss.trumpet");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_BOOM = register("boss.boom");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_APPLAUSE = register("boss.applause");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_INTRO = register("boss.intro");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_PHASE = register("boss.phase");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_SAM_HYDE = register("music.sam_hyde");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_ELEPHANT_MARIO = register("music.elephant_mario");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_SAM_PHASE_TWO = register("music.sam_hyde_phase_two");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_ELEPHANT_PHASE_TWO = register("music.elephant_mario_phase_two");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_TELEGRAPH = register("boss.telegraph");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_STAGGER = register("boss.stagger");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE_ENTER = register("pipe.enter");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE_EXIT = register("pipe.exit");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE_LINK = register("pipe.link");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE_FAIL = register("pipe.fail");
    private static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> SAM_VOICES = registerSamVoices();

    private static Map<String, DeferredHolder<SoundEvent, SoundEvent>> registerSamVoices() {
        Map<String, DeferredHolder<SoundEvent, SoundEvent>> voices = new HashMap<>();
        SamVoiceLines.TICKS.keySet().stream().sorted().forEach(key -> voices.put(key, register("sam.voice." + key)));
        return Map.copyOf(voices);
    }

    public static @Nullable SoundEvent samVoice(String translationKey) {
        var voice = SAM_VOICES.get(translationKey.replace("boss.hydemod.sam_hyde.", ""));
        return voice == null ? null : voice.get();
    }

    public static int samVoiceTicks(String translationKey) {
        return SamVoiceLines.TICKS.getOrDefault(translationKey.replace("boss.hydemod.sam_hyde.", ""), 100);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(HydeMod.id(name)));
    }

    private ModSounds() {}
}
