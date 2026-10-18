package com.hydemod.registry;

import com.hydemod.HydeMod;
import com.hydemod.block.ChargingStationBlock;
import com.hydemod.block.KyberOreBlock;
import com.hydemod.item.KyberColor;
import com.hydemod.block.WarpPipeBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HydeMod.MODID);

    // Kyber crystal ores: green (overworld stone), blue (deep overworld), red (nether), purple (end).
    public static final DeferredBlock<KyberOreBlock> GREEN_KYBER_ORE = BLOCKS.registerBlock("green_kyber_ore",
            p -> new KyberOreBlock(UniformInt.of(3, 6), KyberColor.GREEN, p),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(3.0F, 3.0F).lightLevel(s -> 7));
    public static final DeferredBlock<KyberOreBlock> BLUE_KYBER_ORE = BLOCKS.registerBlock("deepslate_blue_kyber_ore",
            p -> new KyberOreBlock(UniformInt.of(4, 8), KyberColor.BLUE, p),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE).lightLevel(s -> 7));
    public static final DeferredBlock<KyberOreBlock> RED_KYBER_ORE = BLOCKS.registerBlock("nether_red_kyber_ore",
            p -> new KyberOreBlock(UniformInt.of(5, 10), KyberColor.RED, p),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.NETHER_ORE).lightLevel(s -> 9));
    public static final DeferredBlock<KyberOreBlock> PURPLE_KYBER_ORE = BLOCKS.registerBlock("end_purple_kyber_ore",
            p -> new KyberOreBlock(UniformInt.of(7, 14), KyberColor.PURPLE, p),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(6.0F, 9.0F).lightLevel(s -> 11));

    public static final DeferredBlock<Block> NEOPLAS_BLOCK = BLOCKS.registerSimpleBlock("neoplas_block",
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> TRASH_CUBE_BLOCK = BLOCKS.registerSimpleBlock("trash_cube_block",
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.0F).sound(SoundType.WET_GRASS));

    public static final DeferredBlock<WarpPipeBlock> WARP_PIPE = BLOCKS.registerBlock("warp_pipe", WarpPipeBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(3.0F, 6.0F).sound(SoundType.METAL)
                    .lightLevel(s -> s.getValue(WarpPipeBlock.LINKED) ? 7 : 0).noOcclusion());
    public static final DeferredBlock<ChargingStationBlock> CHARGING_STATION = BLOCKS.registerBlock("charging_station", ChargingStationBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F, 6.0F).sound(SoundType.METAL).lightLevel(s -> 4));

    /** A snowy old lamp-post that never goes out. */
    public static final DeferredBlock<com.hydemod.block.LampPostBlock> LAMP_POST = BLOCKS.registerBlock("lamp_post", com.hydemod.block.LampPostBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion());

    private ModBlocks() {}
}
