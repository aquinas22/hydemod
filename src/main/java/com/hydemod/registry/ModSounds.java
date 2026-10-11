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

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(HydeMod.id(name)));
    }

    private ModSounds() {}
}
