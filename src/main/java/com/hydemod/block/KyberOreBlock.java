package com.hydemod.block;

import com.hydemod.item.KyberColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Kyber crystal ore: its crystals glow and shed sparkles of their colour from any exposed face. */
public class KyberOreBlock extends DropExperienceBlock {
    private final KyberColor color;

    public KyberOreBlock(IntProvider xp, KyberColor color, Properties properties) {
        super(xp, properties);
        this.color = color;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (Direction dir : Direction.values()) {
            if (random.nextInt(3) != 0) {
                continue;
            }
            BlockPos side = pos.relative(dir);
            if (level.getBlockState(side).isSolidRender()) {
                continue;
            }
            double x = pos.getX() + 0.5 + dir.getStepX() * 0.55 + (dir.getStepX() == 0 ? random.nextDouble() - 0.5 : 0);
            double y = pos.getY() + 0.5 + dir.getStepY() * 0.55 + (dir.getStepY() == 0 ? random.nextDouble() - 0.5 : 0);
            double z = pos.getZ() + 0.5 + dir.getStepZ() * 0.55 + (dir.getStepZ() == 0 ? random.nextDouble() - 0.5 : 0);
            level.addParticle(new DustParticleOptions(color.rgb(), 0.7F + random.nextFloat() * 0.5F), x, y, z, 0, 0.02, 0);
            if (random.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, dir.getStepX() * 0.01, 0.015, dir.getStepZ() * 0.01);
            }
        }
    }
}
