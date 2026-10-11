package com.hydemod.item.realm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Homage items for classic RPGs: original designs, simple fun mechanics. */
public final class RetroItems {

    /** Slugger Bat: a trusty baseball bat. Every hit is a home run. Knockback handled by the attribute. */
    public static class SluggerBat extends UtilityItems.Base {
        public SluggerBat(Properties p) {
            super(p);
        }

        @Override
        public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker.level() instanceof ServerLevel level && attacker.getRandom().nextInt(4) == 0) {
                // SMAAAASH! critical home run
                Vec3 d = target.position().subtract(attacker.position()).normalize();
                target.push(d.x * 2.2, 0.8, d.z * 2.2);
                target.hurtMarked = true;
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(), 30, 0.3, 0.4, 0.3, 0.6);
                level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 0.6F);
                if (attacker instanceof Player player) {
                    player.sendOverlayMessage(Component.translatable("message.hydemod.smash"));
                }
            }
        }
    }

    /** Reflector Badge: carried anywhere in your inventory, half of projectile damage bounces back at the shooter (see CommonEvents). */
    public static class ReflectorBadge extends UtilityItems.Base {
        public ReflectorBadge(Properties p) {
            super(p);
        }
    }

    /** Healing Butterfly: release it to heal fully and shake off bad effects. */
    public static class HealingButterfly extends UtilityItems.Base {
        public HealingButterfly(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                player.heal(player.getMaxHealth());
                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.WITHER);
                player.removeEffect(MobEffects.WEAKNESS);
                player.removeEffect(MobEffects.SLOWNESS);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1));
                for (int i = 0; i < 30; i++) {
                    double a = i * 0.6, r = 0.6 + i * 0.03;
                    server.sendParticles(new DustParticleOptions(i % 2 == 0 ? 0xFFB0F0 : 0xB0E0FF, 1.0F),
                            player.getX() + Math.cos(a) * r, player.getY() + 0.3 + i * 0.07, player.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
                }
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 1.6F);
                player.getItemInHand(hand).consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Spin Hammer: slam the ground for a shockwave that flips nearby monsters into the air. */
    public static class SpinHammer extends UtilityItems.Base {
        public SpinHammer(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                BlockState ground = level.getBlockState(player.blockPosition().below());
                for (int i = 0; i < 36; i++) {
                    double a = i * Math.PI * 2 / 36;
                    for (double r = 1; r <= 5; r += 1.3) {
                        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground.isAir() ? net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState() : ground),
                                player.getX() + Math.cos(a) * r, player.getY() + 0.1, player.getZ() + Math.sin(a) * r, 1, 0.05, 0.05, 0.05, 0.1);
                    }
                }
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5, 1.5, 5),
                        e -> e != player && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e))) {
                    e.hurtServer(server, player.damageSources().playerAttack(player), 6.0F);
                    e.push(0, 0.9, 0);
                    e.hurtMarked = true;
                }
                level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.3F);
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 30);
                player.getItemInHand(hand).hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Flip Charm: flip through the world's dimensions to slip through up to 3 blocks of wall. */
    public static class FlipCharm extends UtilityItems.Base {
        public FlipCharm(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (!(level instanceof ServerLevel server)) {
                return InteractionResult.SUCCESS;
            }
            Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
            for (int dist = 2; dist <= 4; dist++) {
                BlockPos feet = BlockPos.containing(player.position().add(look.scale(dist)));
                if (level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                        && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                        && !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty()) {
                    server.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 40, 0.3, 0.8, 0.3, 0.05);
                    player.teleportTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
                    server.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 40, 0.3, 0.8, 0.3, 0.05);
                    level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 1.4F);
                    player.getCooldowns().addCooldown(player.getItemInHand(hand), 20);
                    player.getItemInHand(hand).hurtAndBreak(1, player, hand);
                    return InteractionResult.SUCCESS;
                }
            }
            player.sendOverlayMessage(Component.translatable("message.hydemod.flip.blocked"));
            return InteractionResult.FAIL;
        }
    }

    /** Moment Stopwatch: freezes time for everything around you for five seconds. */
    public static class MomentStopwatch extends UtilityItems.Base {
        public MomentStopwatch(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12), e -> e != player)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 5, 9, false, false));
                    e.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 5, 4, false, false));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 5, 4, false, false));
                    e.setDeltaMovement(Vec3.ZERO);
                    server.sendParticles(ParticleTypes.ENCHANT, e.getX(), e.getY(0.7), e.getZ(), 20, 0.3, 0.5, 0.3, 0.3);
                }
                level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.2F, 1.8F);
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 40);
                player.getItemInHand(hand).hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Chrono Pendant: if you'd die, time rewinds 10 seconds instead (once per 5 minutes). */
    public static class ChronoPendant extends UtilityItems.Base {
        private static final int HISTORY = 10; // seconds
        private static final Map<UUID, Deque<GlobalPos>> TRAILS = new ConcurrentHashMap<>();

        public ChronoPendant(Properties p) {
            super(p);
        }

        /** Called once a second for every player: remembers where they were. */
        public static void record(ServerPlayer player) {
            Deque<GlobalPos> trail = TRAILS.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
            trail.addLast(GlobalPos.of(player.level().dimension(), player.blockPosition()));
            while (trail.size() > HISTORY) {
                trail.removeFirst();
            }
        }

        public static void forget(Player player) {
            TRAILS.remove(player.getUUID());
        }

        /** Tries to rewind the player; returns true if it saved them. */
        public static boolean rewind(ServerPlayer player, ItemStack pendant) {
            if (player.getCooldowns().isOnCooldown(pendant)) {
                return false;
            }
            Deque<GlobalPos> trail = TRAILS.get(player.getUUID());
            GlobalPos back = trail == null || trail.isEmpty() ? GlobalPos.of(player.level().dimension(), player.blockPosition()) : trail.peekFirst();
            ServerLevel target = player.level().getServer().getLevel(back.dimension());
            if (target == null) {
                return false;
            }
            ServerLevel from = player.level();
            from.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 80, 0.4, 1.0, 0.4, 0.2);
            player.teleportTo(target, back.pos().getX() + 0.5, back.pos().getY(), back.pos().getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot(), true);
            player.setHealth(player.getMaxHealth());
            player.clearFire();
            // undo what hurt you, keep your buffs
            player.getActiveEffects().stream().filter(e -> !e.getEffect().value().isBeneficial()).map(MobEffectInstance::getEffect).toList()
                    .forEach(player::removeEffect);
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 5, 4));
            player.resetFallDistance();
            target.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.5F, 0.6F);
            target.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 60, 0.4, 1.0, 0.4, 0.15);
            player.sendSystemMessage(Component.translatable("message.hydemod.chrono_rewind"));
            player.getCooldowns().addCooldown(pendant, 20 * 300);
            if (trail != null) {
                trail.clear();
            }
            return true;
        }
    }

    private RetroItems() {}
}
