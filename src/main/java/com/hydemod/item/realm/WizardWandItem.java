package com.hydemod.item.realm;

import com.hydemod.item.HydeTooltips;
import com.hydemod.registry.ModComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Wizard's Wand. Sneak + use to pick a spell, use to cast it at whatever you're looking at:
 * Stunning Bolt, Lumos-style Light, Disarm and Levitate.
 */
public class WizardWandItem extends Item {
    private static final String[] SPELLS = {"stun", "light", "disarm", "levitate"};
    private static final int[] COLORS = {0xFF3030, 0xFFFFC0, 0xFF8A20, 0x80C0FF};

    public WizardWandItem(Properties properties) {
        super(properties);
    }

    private static int spell(ItemStack stack) {
        return Math.floorMod(stack.getOrDefault(ModComponents.SPELL.get(), 0), SPELLS.length);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            int next = (spell(stack) + 1) % SPELLS.length;
            stack.set(ModComponents.SPELL.get(), next);
            player.sendOverlayMessage(Component.translatable("spell.hydemod." + SPELLS[next]).withColor(COLORS[next]));
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
            return InteractionResult.SUCCESS;
        }
        int s = spell(stack);
        DustParticleOptions dust = new DustParticleOptions(COLORS[s], 1.0F);
        Vec3 from = RayUtil.handPos(player);
        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.8F, 1.6F);
        if (s == 1) { // light: the wand tip shines, letting you see in the dark, and reveals nearby creatures
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 90, 0));
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(24), e -> e != player)) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 15, 0));
            }
            server.sendParticles(ParticleTypes.END_ROD, from.x, from.y, from.z, 30, 0.3, 0.3, 0.3, 0.1);
        } else {
            LivingEntity target = RayUtil.entityRay(player, 32.0);
            Vec3 to = target != null ? target.position().add(0, target.getBbHeight() * 0.5, 0) : from.add(player.getLookAngle().scale(32));
            RayUtil.trail(server, from, to, dust);
            if (target != null) {
                switch (s) {
                    case 0 -> { // stunning bolt
                        target.hurtServer(server, player.damageSources().indirectMagic(player, player), 6.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 5, 4));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 5, 1));
                        Vec3 push = to.subtract(from).normalize();
                        target.push(push.x * 1.2, 0.4, push.z * 1.2);
                        target.hurtMarked = true;
                    }
                    case 2 -> { // disarm
                        ItemStack held = target.getItemBySlot(EquipmentSlot.MAINHAND);
                        if (!held.isEmpty() && (target instanceof Mob || target instanceof Player)) {
                            target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                            var drop = target.spawnAtLocation(server, held);
                            if (drop != null) {
                                Vec3 toward = from.subtract(to).normalize().scale(0.6);
                                drop.setDeltaMovement(toward.x, 0.4, toward.z);
                            }
                        }
                    }
                    default -> target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 3, 1)); // levitate
                }
                server.sendParticles(dust, to.x, to.y, to.z, 25, 0.3, 0.4, 0.3, 0.0);
            }
        }
        player.getCooldowns().addCooldown(stack, 20);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        int s = spell(stack);
        builder.accept(Component.translatable("tooltip.hydemod.spell", Component.translatable("spell.hydemod." + SPELLS[s])).withColor(COLORS[s]));
        HydeTooltips.add(stack, builder);
    }
}
