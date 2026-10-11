package com.hydemod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A snowy old lamp-post: a thin iron post with a glowing lantern head; snowflakes drift around it. */
public class LampPostBlock extends Block {
    public static final MapCodec<LampPostBlock> CODEC = simpleCodec(LampPostBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(box(6.5, 0, 6.5, 9.5, 11, 9.5), box(4, 11, 4, 12, 16, 12));

    public LampPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + random.nextDouble() * 1.6 - 0.3, pos.getY() + 1.4,
                    pos.getZ() + random.nextDouble() * 1.6 - 0.3, 0, -0.03, 0);
        }
    }
}
