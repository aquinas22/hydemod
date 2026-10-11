package com.hydemod.entity;

import com.hydemod.registry.ModEntities;
import com.hydemod.registry.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A cube of trash, hurled. Thrown by players (weak) and by Sam Hyde (not weak). */
public class ThrownTrashCube extends ThrowableItemProjectile {
    private float damage = 3.0F;

    public ThrownTrashCube(EntityType<? extends ThrownTrashCube> type, Level level) {
        super(type, level);
    }

    public ThrownTrashCube(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_TRASH_CUBE.get(), owner, level, stack);
    }

    public ThrownTrashCube withDamage(float damage) {
        this.damage = damage;
        return this;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.TRASH_CUBE.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(new ItemStack(getDefaultItem())));
            for (int i = 0; i < 10; i++) {
                level().addParticle(particle, getX(), getY(), getZ(), (random.nextDouble() - 0.5) * 0.2, 0.1, (random.nextDouble() - 0.5) * 0.2);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Entity target = hit.getEntity();
        if (level() instanceof ServerLevel serverLevel) {
            target.hurtServer(serverLevel, damageSources().thrown(this, getOwner()), damage);
            if (target instanceof LivingEntity living && damage > 4.0F) {
                living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 4, 0));
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }
}
