package com.hydemod.registry;

import com.hydemod.HydeMod;
import com.hydemod.entity.ElephantMario;
import com.hydemod.entity.DepthGrovler;
import com.hydemod.entity.SamHyde;
import com.hydemod.entity.ThrownTrashCube;
import com.hydemod.entity.TrunkSpray;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(HydeMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<com.hydemod.entity.NewsReporter>> PETER_OHANRAHAN = ENTITIES.registerEntityType(
            "peter_ohanrahan", com.hydemod.entity.NewsReporter::new, MobCategory.CREATURE, b -> b.sized(.6F, 1.8F).eyeHeight(1.65F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.hydemod.entity.NewsReporter>> CHRIS_MORRIS = ENTITIES.registerEntityType(
            "chris_morris", com.hydemod.entity.NewsReporter::new, MobCategory.CREATURE, b -> b.sized(.6F, 1.8F).eyeHeight(1.65F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<SamHyde>> SAM_HYDE = ENTITIES.registerEntityType("sam_hyde", SamHyde::new, MobCategory.MONSTER,
            b -> b.sized(0.6F, 2.1F).eyeHeight(1.9F).clientTrackingRange(10).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<ElephantMario>> ELEPHANT_MARIO = ENTITIES.registerEntityType("elephant_mario", ElephantMario::new, MobCategory.MONSTER,
            b -> b.sized(1.8F, 2.9F).eyeHeight(2.4F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<DepthGrovler>> DEPTH_GROVLER = ENTITIES.registerEntityType("depth_grovler", DepthGrovler::new, MobCategory.MONSTER,
            b -> b.sized(0.7F, 0.8F).eyeHeight(0.55F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownTrashCube>> THROWN_TRASH_CUBE = ENTITIES.registerEntityType("thrown_trash_cube", ThrownTrashCube::new, MobCategory.MISC,
            b -> b.noLootTable().sized(0.35F, 0.35F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<TrunkSpray>> TRUNK_SPRAY = ENTITIES.registerEntityType("trunk_spray", TrunkSpray::new, MobCategory.MISC,
            b -> b.noLootTable().sized(0.4F, 0.4F).clientTrackingRange(4).updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<com.hydemod.entity.ThrownBomb>> THROWN_BOMB = ENTITIES.registerEntityType("thrown_bomb",
            com.hydemod.entity.ThrownBomb::new, MobCategory.MISC, b -> b.noLootTable().sized(0.4F, 0.4F).clientTrackingRange(6).updateInterval(5));

    private ModEntities() {}
}
