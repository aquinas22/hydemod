package com.hydemod.item.realm;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Shared ray casting + particle trails for the wand/hook style items. */
final class RayUtil {
    static BlockHitResult blockRay(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    /** First living entity along the look ray (stopping at blocks), or null. */
    static @Nullable LivingEntity entityRay(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        BlockHitResult block = blockRay(player, range);
        Vec3 end = block.getType() == HitResult.Type.MISS ? eye.add(player.getLookAngle().scale(range)) : block.getLocation();
        AABB box = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e != player, 0.3F);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    static void trail(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle) {
        Vec3 d = to.subtract(from);
        int steps = (int) (d.length() * 3);
        for (int i = 0; i < steps; i++) {
            Vec3 p = from.add(d.scale(i / (double) steps));
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    static Vec3 handPos(Entity e) {
        return e.getEyePosition().add(e.getLookAngle().scale(0.6)).add(0, -0.25, 0);
    }

    private RayUtil() {}
}
