package com.hydemod.registry;

import com.hydemod.HydeMod;
import com.hydemod.effect.StarPowerEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, HydeMod.MODID);

    /** Power Mushroom: you grow huge, with extra health and reach. */
    public static final DeferredHolder<MobEffect, MobEffect> SUPER_SIZE = EFFECTS.register("super_size", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xE04030) {}
            .addAttributeModifier(Attributes.SCALE, HydeMod.id("super_size_scale"), 0.6, AttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(Attributes.MAX_HEALTH, HydeMod.id("super_size_health"), 10.0, AttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(Attributes.ENTITY_INTERACTION_RANGE, HydeMod.id("super_size_reach"), 1.5, AttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, HydeMod.id("super_size_block_reach"), 1.5, AttributeModifier.Operation.ADD_VALUE));
    /** Invincibility Star: touching hostile mobs knocks them flying. */
    public static final DeferredHolder<MobEffect, MobEffect> STAR_POWER = EFFECTS.register("star_power", StarPowerEffect::new);

    private ModEffects() {}
}
