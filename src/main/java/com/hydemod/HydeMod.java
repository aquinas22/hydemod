package com.hydemod;

import com.hydemod.registry.ModBlockEntities;
import com.hydemod.registry.ModBlocks;
import com.hydemod.registry.ModComponents;
import com.hydemod.registry.ModEntities;
import com.hydemod.registry.ModItems;
import com.hydemod.registry.ModSounds;
import com.hydemod.registry.ModTabs;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(HydeMod.MODID)
public class HydeMod {
    public static final String MODID = "hydemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HydeMod(IEventBus modBus, ModContainer container) {
        ModComponents.COMPONENTS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModTabs.TABS.register(modBus);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
