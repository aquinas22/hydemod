package com.hydemod.entity;

import com.hydemod.registry.ModEntities;
import com.hydemod.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A lit bomb: bounces once off the ground, then blows up after its fuse (or on hitting a creature). */
public class ThrownBomb extends ThrowableItemProjectile {
    private int fuse = 50;
    private boolean bounced;

    public ThrownBomb(EntityType<? extends ThrownBomb> type, Level level) {
        super(type, level);
    }

    public ThrownBomb(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_BOMB.get(), owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BOMB.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 0, 0.03, 0);
            if (tickCount % 2 == 0) {
                level().addParticle(ParticleTypes.FLAME, getX(), getY() + 0.35, getZ(), 0, 0.02, 0);
            }
        } else if (--fuse <= 0) {
            explode();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        if (level().isClientSide()) {
            return;
        }
        if (hit.getType() == HitResult.Type.ENTITY) {
            explode();
        } else if (!bounced) {
            bounced = true;
            Vec3 v = getDeltaMovement();
            setDeltaMovement(v.x * 0.35, Math.abs(v.y) * 0.35, v.z * 0.35);
        } else {
            setDeltaMovement(Vec3.ZERO); // sit there fizzing until the fuse runs out
            setNoGravity(true);
        }
    }

    private void explode() {
        if (level() instanceof ServerLevel server && !isRemoved()) {
            server.explode(this, getX(), getY(), getZ(), 2.5F, Level.ExplosionInteraction.TNT);
            discard();
        }
    }
}
