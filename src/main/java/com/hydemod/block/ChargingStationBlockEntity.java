package com.hydemod.block;

import com.hydemod.item.LightsaberItem;
import com.hydemod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * Stores Redstone Flux. Accepts FE from any mod's cables/generators (exposed as an energy
 * capability), or from redstone dust/blocks fed in by hand.
 */
public class ChargingStationBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1_000_000;
    public static final int FE_PER_REDSTONE = 2_500;

    private final SimpleEnergyHandler energy = new SimpleEnergyHandler(CAPACITY, 20_000, 20_000) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

    public ChargingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHARGING_STATION.get(), pos, state);
    }

    public SimpleEnergyHandler getEnergy() {
        return energy;
    }

    public int stored() {
        return energy.getAmountAsInt();
    }

    /** Adds raw energy (from burning redstone); returns the amount accepted. */
    public int addEnergy(int amount) {
        int accepted = Math.min(amount, CAPACITY - stored());
        energy.set(stored() + accepted);
        return accepted;
    }

    /** Moves as much energy as possible into a lightsaber; returns the amount moved. */
    public int chargeSaber(ItemStack saber) {
        int room = LightsaberItem.CAPACITY - LightsaberItem.getEnergy(saber);
        int moved = Math.min(room, stored());
        if (moved > 0) {
            energy.set(stored() - moved);
            LightsaberItem.setEnergy(saber, LightsaberItem.getEnergy(saber) + moved);
        }
        return moved;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy.deserialize(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energy.serialize(output);
    }
}
