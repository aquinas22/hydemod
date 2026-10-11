package com.hydemod.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** "Can we reset the clock so I have another minute?" Resets the world clock to morning. */
public class ResetClockItem extends Item {
    public ResetClockItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            var clock = serverLevel.dimensionTypeRegistration().value().defaultClock();
            if (clock.isEmpty() || !serverLevel.getServer().clockManager().moveToTimeMarker(clock.get(), ClockTimeMarkers.DAY)) {
                player.sendSystemMessage(Component.translatable("message.hydemod.reset_clock.fail").withStyle(ChatFormatting.GRAY));
                return InteractionResult.FAIL;
            }
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("message.hydemod.reset_clock", player.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            ItemStack stack = player.getItemInHand(hand);
            player.getCooldowns().addCooldown(stack, 20 * 60);
            stack.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HydeTooltips.add(stack, builder);
    }
}
