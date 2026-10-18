package com.hydemod.item;

import com.hydemod.registry.ModItems;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Newsroom equipment: peace, field measurements, broadcast effects and archive playback. */
public final class NewsPropItem extends Item {
    public enum Kind { TWIG, INFOMOMETER, MICROPHONE, FACT_BOMB, ARCHIVE, REPORT }
    private final Kind kind;
    public NewsPropItem(Properties properties, Kind kind) { super(properties); this.kind = kind; }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        switch (kind) {
            case TWIG -> {
                int affected = 0;
                for (Monster mob : server.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(10), Monster::isAlive)) {
                    if (mob.distanceToSqr(player) > 100) continue;
                    mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0), player);
                    mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 0), player);
                    affected++;
                }
                boolean hot = server.dimension() == Level.NETHER;
                server.sendParticles(hot ? ParticleTypes.SMOKE : ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getY() + 1, player.getZ(), 30, 2, .6, 2, .03);
                player.sendOverlayMessage(Component.translatable(hot ? "message.hydemod.news.melting" : "message.hydemod.news.peace", affected));
                stack.hurtAndBreak(hot ? 2 : 1, player, hand.asEquipmentSlot());
                player.getCooldowns().addCooldown(stack, 300);
            }
            case INFOMOMETER -> {
                int count = (int) server.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(32),
                        mob -> mob.isAlive() && mob.distanceToSqr(player) <= 32 * 32).size();
                player.sendSystemMessage(Component.translatable("message.hydemod.news.reading", player.blockPosition().getX(),
                        player.blockPosition().getY(), player.blockPosition().getZ(), server.dimension().identifier().toString(), count));
                player.sendSystemMessage(Component.translatable(server.isThundering() ? "message.hydemod.news.thunder" :
                        server.isRaining() ? "message.hydemod.news.rain" : "message.hydemod.news.clear"));
                player.getCooldowns().addCooldown(stack, 40);
            }
            case MICROPHONE -> {
                var headline = Component.translatable("message.hydemod.news.live", player.getDisplayName(),
                        player.blockPosition().getX(), player.blockPosition().getZ());
                for (var listener : server.players()) if (listener.distanceToSqr(player) <= 32 * 32) listener.sendSystemMessage(headline);
                server.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, .7F, .7F);
                player.getCooldowns().addCooldown(stack, 200);
            }
            case FACT_BOMB -> {
                for (Monster mob : server.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(16), Monster::isAlive))
                    if (mob.distanceToSqr(player) <= 16 * 16) mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0), player);
                server.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, -1),
                        player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 32, 2, 1, 2, .1);
                server.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, .6F, 1.2F);
                stack.consume(1, player);
                player.getCooldowns().addCooldown(new ItemStack(ModItems.FACT_BOMB.get()), 60);
                player.sendOverlayMessage(Component.translatable("message.hydemod.news.fact_bomb"));
            }
            case ARCHIVE -> {
                player.sendSystemMessage(Component.translatable("message.hydemod.news.archive"));
                server.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, .5F, .8F);
                player.getCooldowns().addCooldown(stack, 100);
            }
            case REPORT -> {
                player.sendSystemMessage(Component.translatable("message.hydemod.news.unverified"));
                player.getCooldowns().addCooldown(stack, 40);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> builder, TooltipFlag flag) { HydeTooltips.add(stack, builder); }
}
