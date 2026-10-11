package com.hydemod.block;

import com.hydemod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Remembers which warp pipe this one is linked to. */
public class WarpPipeBlockEntity extends BlockEntity {
    private @Nullable GlobalPos target;

    public WarpPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARP_PIPE.get(), pos, state);
    }

    public @Nullable GlobalPos getTarget() {
        return target;
    }

    public void setTarget(@Nullable GlobalPos target) {
        this.target = target;
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        target = input.read("target", GlobalPos.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("target", GlobalPos.CODEC, target);
    }
}
