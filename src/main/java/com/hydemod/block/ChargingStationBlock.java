package com.hydemod.block;

import com.hydemod.item.LightsaberItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Lightsaber charging station.
 * - Right-click with redstone dust (or redstone blocks) to convert the stack into Redstone Flux.
 * - Right-click with a lightsaber to charge it.
 * - Empty hand shows the stored energy.
 * - Pipe FE into it from any tech mod.
 */
public class ChargingStationBlock extends BaseEntityBlock {
    public static final MapCodec<ChargingStationBlock> CODEC = simpleCodec(ChargingStationBlock::new);

    public ChargingStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargingStationBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ChargingStationBlockEntity station)) {
            return InteractionResult.PASS;
        }
        int perItem = stack.is(Items.REDSTONE) ? ChargingStationBlockEntity.FE_PER_REDSTONE
                : stack.is(Items.REDSTONE_BLOCK) ? ChargingStationBlockEntity.FE_PER_REDSTONE * 9 : 0;
        if (perItem > 0) {
            if (level instanceof ServerLevel serverLevel) {
                int count = stack.getCount(); // feeds in as much of the stack as fits
                int used = 0;
                while (used < count && ChargingStationBlockEntity.CAPACITY - station.stored() >= perItem) {
                    station.addEnergy(perItem);
                    used++;
                }
                if (used == 0) {
                    player.sendOverlayMessage(Component.translatable("message.hydemod.station.full").withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }
                stack.consume(used, player);
                serverLevel.sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 10, 0.3, 0.1, 0.3, 0.0);
                level.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.6F, 1.6F);
                showEnergy(player, station);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof LightsaberItem) {
            if (!level.isClientSide()) {
                int moved = station.chargeSaber(stack);
                if (moved > 0) {
                    level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.7F, 1.8F);
                }
                player.sendOverlayMessage(Component.translatable("message.hydemod.station.charged",
                        String.format("%,d", moved), String.format("%,d", LightsaberItem.getEnergy(stack)), String.format("%,d", station.stored()))
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ChargingStationBlockEntity station) {
            showEnergy(player, station);
        }
        return InteractionResult.SUCCESS;
    }

    private static void showEnergy(Player player, ChargingStationBlockEntity station) {
        player.sendOverlayMessage(Component.translatable("message.hydemod.station.energy",
                String.format("%,d", station.stored()), String.format("%,d", ChargingStationBlockEntity.CAPACITY)).withStyle(ChatFormatting.RED));
    }
}
