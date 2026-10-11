package com.hydemod.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Guys, pat yourselves on the back. Heals you and every player near you, for saving the world. */
public class PatOnTheBackItem extends Item {
    public PatOnTheBackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            for (Player other : level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(8.0))) {
                other.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1));
                other.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 30, 0));
                other.sendSystemMessage(Component.translatable("message.hydemod.pat_on_the_back").withStyle(ChatFormatting.GREEN));
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, other.getX(), other.getY() + 1.2, other.getZ(), 12, 0.4, 0.5, 0.4, 0.0);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1.0F, 1.5F);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 30);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HydeTooltips.add(stack, builder);
    }
}
