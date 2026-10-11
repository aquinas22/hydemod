package com.hydemod.item.realm;

import com.hydemod.entity.ThrownBomb;
import com.hydemod.item.HydeTooltips;
import com.hydemod.registry.ModComponents;
import java.util.function.Consumer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** The "realm" items: each a small, self-contained mechanic. */
public final class UtilityItems {
    /** Base class that adds the item's flavour tooltip. */
    public abstract static class Base extends Item {
        protected Base(Properties p) {
            super(p);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
            HydeTooltips.add(stack, builder);
        }
    }

    /** Healing Cordial: a drop heals you, or right-click another creature to heal it. */
    public static class HealingCordial extends Base {
        public HealingCordial(Properties p) {
            super(p);
        }

        private static void heal(ServerLevel level, LivingEntity target) {
            target.heal(12.0F);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 1));
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
            level.playSound(null, target.blockPosition(), SoundEvents.HONEY_DRINK.value(), SoundSource.PLAYERS, 1.0F, 1.4F);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                heal(server, player);
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 5);
                player.getItemInHand(hand).hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
            if (player.level() instanceof ServerLevel server) {
                heal(server, target);
                player.getCooldowns().addCooldown(stack, 20 * 5);
                stack.hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Invisibility Cloak: throw it over yourself to vanish for a minute. */
    public static class InvisibilityCloak extends Base {
        public InvisibilityCloak(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60, 0, false, false, true));
                server.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY() + 1, player.getZ(), 30, 0.4, 0.8, 0.4, 0.02);
                level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1.0F, 0.6F);
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 90);
                player.getItemInHand(hand).hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Flying Broom: hold right-click to fly where you look. */
    public static class FlyingBroom extends Base {
        public FlyingBroom(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity user) {
            return 72000;
        }

        @Override
        public ItemUseAnimation getUseAnimation(ItemStack stack) {
            return ItemUseAnimation.SPEAR;
        }

        @Override
        public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
            Vec3 look = entity.getLookAngle();
            Vec3 v = entity.getDeltaMovement().scale(0.6).add(look.scale(0.55));
            entity.setDeltaMovement(v.x, Math.max(v.y, -0.05) + 0.05, v.z);
            entity.resetFallDistance();
            entity.hurtMarked = true;
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), 2, 0.2, 0.05, 0.2, 0.0);
                if (remaining % 20 == 0 && entity instanceof Player player) {
                    stack.hurtAndBreak(1, player, player.getUsedItemHand());
                }
                if (remaining % 15 == 0) {
                    level.playSound(null, entity.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.15F, 1.4F);
                }
            }
        }
    }

    /** Blaze Blossom: a flower that throws fireballs. */
    public static class BlazeBlossom extends Base {
        public BlazeBlossom(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                Vec3 look = player.getLookAngle();
                SmallFireball ball = new SmallFireball(level, player, look.scale(1.5));
                ball.setPos(player.getX() + look.x, player.getEyeY() - 0.2 + look.y, player.getZ() + look.z);
                server.addFreshEntity(ball);
                level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 1.3F);
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 8);
                player.getItemInHand(hand).hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Heart Vessel: permanently adds a heart (up to +10 hearts total). */
    public static class HeartVessel extends Base {
        private static final net.minecraft.resources.Identifier ID = com.hydemod.HydeMod.id("heart_vessels");
        public static final double MAX_BONUS = 20.0;

        public HeartVessel(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (!(level instanceof ServerLevel server)) {
                return InteractionResult.SUCCESS;
            }
            AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
            if (health == null) {
                return InteractionResult.FAIL;
            }
            AttributeModifier old = health.getModifier(ID);
            double bonus = old == null ? 0 : old.amount();
            if (bonus >= MAX_BONUS) {
                player.sendOverlayMessage(Component.translatable("message.hydemod.heart_vessel.max"));
                return InteractionResult.FAIL;
            }
            health.addOrReplacePermanentModifier(new AttributeModifier(ID, bonus + 2.0, AttributeModifier.Operation.ADD_VALUE));
            player.setHealth(player.getMaxHealth());
            server.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 2, player.getZ(), 15, 0.5, 0.3, 0.5, 0.0);
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            player.sendOverlayMessage(Component.translatable("message.hydemod.heart_vessel", (int) ((bonus + 2.0) / 2)));
            player.getItemInHand(hand).consume(1, player);
            return InteractionResult.SUCCESS;
        }
    }

    /** Grappling Hook: fires at a block and yanks you to it. */
    public static class GrapplingHook extends Base {
        public GrapplingHook(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            BlockHitResult hit = RayUtil.blockRay(player, 30.0);
            if (hit.getType() == HitResult.Type.MISS) {
                return InteractionResult.FAIL;
            }
            Vec3 target = hit.getLocation();
            Vec3 pull = target.subtract(player.position()).add(0, 1.0, 0);
            double dist = pull.length();
            Vec3 v = pull.normalize().scale(Math.min(3.2, 0.9 + dist * 0.12));
            player.setDeltaMovement(v);
            player.hurtMarked = true;
            player.resetFallDistance();
            if (level instanceof ServerLevel server) {
                RayUtil.trail(server, RayUtil.handPos(player), target, new DustParticleOptions(0x8A8F99, 0.8F));
                level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 1.4F);
                level.playSound(null, hit.getBlockPos(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 15);
                player.getItemInHand(hand).hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Bomb: light the fuse and throw it. */
    public static class Bomb extends Base {
        public Bomb(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level instanceof ServerLevel server) {
                ThrownBomb bomb = new ThrownBomb(server, player, stack.copyWithCount(1));
                bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), -10.0F, 0.9F, 1.0F);
                server.addFreshEntity(bomb);
                level.playSound(null, player.blockPosition(), SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.8F, 1.4F);
            }
            stack.consume(1, player);
            player.getCooldowns().addCooldown(stack, 10);
            return InteractionResult.SUCCESS;
        }
    }

    /** Ocarina: play a tune that swaps day and night. */
    public static class Ocarina extends Base {
        public Ocarina(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                var clock = server.dimensionTypeRegistration().value().defaultClock();
                if (clock.isEmpty()) {
                    return InteractionResult.FAIL;
                }
                boolean isDay = server.isBrightOutside();
                server.getServer().clockManager().moveToTimeMarker(clock.get(), isDay ? ClockTimeMarkers.NIGHT : ClockTimeMarkers.DAY);
                float[] tune = {0.8F, 1.0F, 1.2F, 0.9F, 1.33F};
                for (int i = 0; i < tune.length; i++) {
                    level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.0F, tune[i]);
                }
                server.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 2.2, player.getZ(), 12, 0.6, 0.3, 0.6, 1.0);
                player.sendOverlayMessage(Component.translatable(isDay ? "message.hydemod.ocarina.night" : "message.hydemod.ocarina.day"));
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 30);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Star Phial: a burst of starlight that blinds and scatters nearby monsters. */
    public static class StarPhial extends Base {
        public StarPhial(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60, 0));
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14), e -> e instanceof Enemy)) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 8, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 8, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 8, 0));
                    Vec3 away = e.position().subtract(player.position()).normalize();
                    e.push(away.x * 1.2, 0.3, away.z * 1.2);
                    e.hurtMarked = true;
                }
                server.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 120, 0.2, 0.2, 0.2, 0.35);
                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.8F);
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 45);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Ring of Unseeing: hold it and you can't be seen... but it slowly gnaws at you. Handled in CommonEvents. */
    public static class RingOfUnseeing extends Base {
        public RingOfUnseeing(Properties p) {
            super(p);
        }
    }

    /** Elven Blade: glows blue when hostile monsters are near. */
    public static class ElvenBlade extends Base {
        public ElvenBlade(Properties p) {
            super(p);
        }

        @Override
        public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
            if (level.getGameTime() % 10 != 0) {
                return;
            }
            boolean danger = !level.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(16), e -> e instanceof Enemy && e.isAlive()).isEmpty();
            boolean shown = stack.has(ModComponents.WARNING.get());
            if (danger != shown) {
                if (danger) {
                    stack.set(ModComponents.WARNING.get(), net.minecraft.util.Unit.INSTANCE);
                } else {
                    stack.remove(ModComponents.WARNING.get());
                }
            }
        }

        @Override
        public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
            return slotChanged || !newStack.is(oldStack.getItem());
        }

        @Override
        public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
            return !newStack.is(oldStack.getItem());
        }
    }

    private UtilityItems() {}
}
