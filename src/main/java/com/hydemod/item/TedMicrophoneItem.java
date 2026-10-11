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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The microphone that lets you speak to the unwashed masses. Right-click to deliver a
 * 2070 prediction to everyone nearby and blast surrounding mobs back with sheer charisma.
 */
public class TedMicrophoneItem extends Item {
    public static final int PREDICTIONS = 14;

    public TedMicrophoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            int line = 1 + level.getRandom().nextInt(PREDICTIONS);
            Component speech = Component.translatable("chat.hydemod.speaker", player.getDisplayName(),
                    Component.translatable("prediction.hydemod." + line)).withStyle(ChatFormatting.GOLD);
            for (Player other : serverLevel.players()) {
                if (other.distanceToSqr(player) < 48 * 48) {
                    other.sendSystemMessage(speech);
                }
            }
            for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0), e -> e != player && e.isAlive())) {
                Vec3 push = mob.position().subtract(player.position()).normalize();
                mob.push(push.x * 1.4, 0.4, push.z * 1.4);
                mob.hurtMarked = true;
                mob.hurtServer(serverLevel, player.damageSources().sonicBoom(player), 3.0F);
            }
            serverLevel.sendParticles(ParticleTypes.NOTE, player.getX(), player.getEyeY() + 0.4, player.getZ(), 8, 1.2, 0.4, 1.2, 1.0);
            level.playSound(null, player.blockPosition(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(), SoundSource.PLAYERS, 1.0F, 1.3F);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 60);
            player.getItemInHand(hand).hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HydeTooltips.add(stack, builder);
    }
}
