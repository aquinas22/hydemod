package com.hydemod.entity;

import com.hydemod.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A blast of water from Elephant Mario's trunk. Knocks targets back, slows them and puts out fire. */
public class TrunkSpray extends ThrowableItemProjectile {
    public TrunkSpray(EntityType<? extends TrunkSpray> type, Level level) {
        super(type, level);
    }

    public TrunkSpray(Level level, LivingEntity owner) {
        super(ModEntities.TRUNK_SPRAY.get(), owner, level, new net.minecraft.world.item.ItemStack(Items.BLUE_ICE));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BLUE_ICE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                level().addParticle(ParticleTypes.SPLASH, getX() + (random.nextDouble() - 0.5) * 0.4, getY(), getZ() + (random.nextDouble() - 0.5) * 0.4, 0, 0, 0);
            }
            level().addParticle(ParticleTypes.BUBBLE_POP, getX(), getY(), getZ(), 0, 0, 0);
        }
        if (tickCount > 60) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Entity target = hit.getEntity();
        if (level() instanceof ServerLevel serverLevel && target != getOwner()) {
            target.hurtServer(serverLevel, damageSources().thrown(this, getOwner()), 5.0F);
            target.clearFire();
            if (target instanceof LivingEntity living) {
                Vec3 dir = getDeltaMovement().normalize();
                living.push(dir.x * 1.2, 0.35, dir.z * 1.2);
                living.hurtMarked = true;
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 3, 1));
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 30, 0.4, 0.3, 0.4, 0.1);
            discard();
        }
    }
}
