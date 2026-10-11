package com.hydemod.registry;

import com.hydemod.HydeMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, HydeMod.MODID);

    /** Redstone Flux stored in a lightsaber. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY = COMPONENTS.registerComponentType("energy",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    /** Present while a lightsaber blade is lit; drives the item model. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> IGNITED = COMPONENTS.registerComponentType("ignited",
            b -> b.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));
    /** Blade extension 1-4 while igniting/retracting (4 = fully lit); absent when off. Drives the item model. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BLADE = COMPONENTS.registerComponentType("blade",
            b -> b.persistent(Codec.intRange(1, 4)).networkSynchronized(ByteBufCodecs.VAR_INT));
    /** First warp pipe selected with the pipe wrench. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> PIPE_LINK = COMPONENTS.registerComponentType("pipe_link",
            b -> b.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));
    /** Location saved by the Mind Upload BlackBerry. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> UPLOADED_MIND = COMPONENTS.registerComponentType("uploaded_mind",
            b -> b.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));

    private ModComponents() {}
}
