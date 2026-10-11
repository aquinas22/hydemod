package com.hydemod.registry;

import com.hydemod.HydeMod;
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

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(HydeMod.id(name)));
    }

    private ModSounds() {}
}
