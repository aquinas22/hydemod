package com.hydemod.item;

import java.util.function.Consumer;
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
import net.minecraft.world.phys.Vec3;

/** Uses maglev technology to get resources (you) to the Moon. Launches you skyward with a soft landing. */
public class MaglevLauncherItem extends Item {
    public MaglevLauncherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * 1.2, 2.6, look.z * 1.2);
        player.hurtMarked = true;
        player.resetFallDistance();
        if (level instanceof ServerLevel serverLevel) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 12, 0));
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY(), player.getZ(), 40, 0.4, 0.2, 0.4, 0.3);
            level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 0.7F);
            ItemStack stack = player.getItemInHand(hand);
            player.getCooldowns().addCooldown(stack, 20 * 5);
            stack.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HydeTooltips.add(stack, builder);
    }
}
