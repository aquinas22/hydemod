package com.hydemod.registry;

import com.hydemod.HydeMod;
import com.hydemod.block.ChargingStationBlockEntity;
import com.hydemod.block.WarpPipeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HydeMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WarpPipeBlockEntity>> WARP_PIPE = BLOCK_ENTITIES.register("warp_pipe",
            () -> new BlockEntityType<>(WarpPipeBlockEntity::new, ModBlocks.WARP_PIPE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChargingStationBlockEntity>> CHARGING_STATION = BLOCK_ENTITIES.register("charging_station",
            () -> new BlockEntityType<>(ChargingStationBlockEntity::new, ModBlocks.CHARGING_STATION.get()));

    private ModBlockEntities() {}
}
