package com.hydemod.client;

import com.hydemod.item.LightsaberItem;
import com.hydemod.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Looping lightsaber hum that follows whoever is holding a lit blade. The hum gets louder and its
 * pitch rises with how fast the wielder is moving/turning, and fades out when the blade retracts.
 */
public class LightsaberHumSound extends AbstractTickableSoundInstance {
    private static final Map<Integer, LightsaberHumSound> ACTIVE = new HashMap<>();

    private final LivingEntity holder;
    private float lastYaw;
    private int fade = 0;
    private int age = 0;

    private LightsaberHumSound(LivingEntity holder) {
        super(ModSounds.SABER_HUM.get(), holder.getSoundSource(), holder.getRandom());
        this.holder = holder;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.01F;
        this.lastYaw = holder.getYRot();
        this.x = holder.getX();
        this.y = holder.getY() + 1.0;
        this.z = holder.getZ();
    }

    static ItemStack litSaber(LivingEntity entity) {
        for (ItemStack stack : new ItemStack[] {entity.getMainHandItem(), entity.getOffhandItem()}) {
            if (stack.getItem() instanceof LightsaberItem && LightsaberItem.isIgnited(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        age++;
        Minecraft client = Minecraft.getInstance();
        ItemStack saber = litSaber(holder);
        // Stop if the holder left (died, changed dimension, respawned as a new entity object, ...)
        if (holder.isRemoved() || !holder.isAlive() || holder.level() != client.level
                || client.level == null || client.level.getEntity(holder.getId()) != holder) {
            stopNow();
            return;
        }
        if (saber.isEmpty()) {
            if (++fade > 5) {
                stopNow();
            } else {
                volume *= 0.5F;
            }
            return;
        }
        fade = 0;
        x = holder.getX();
        y = holder.getY() + 1.0;
        z = holder.getZ();
        float turn = Math.abs(holder.getYRot() - lastYaw);
        lastYaw = holder.getYRot();
        double speed = holder.getDeltaMovement().horizontalDistance();
        float motion = Math.min(1.0F, turn / 25.0F + (float) speed * 2.0F + (holder.swinging ? 0.6F : 0.0F));
        float targetVolume = 0.45F + 0.4F * motion;
        volume += (targetVolume - volume) * 0.35F;
        pitch = 1.0F + 0.18F * motion;

        // Glowing motes drifting off the blade.
        Minecraft mc = Minecraft.getInstance();
        boolean ownFirstPerson = holder == mc.player && mc.options.getCameraType().isFirstPerson();
        if (!ownFirstPerson && holder.getRandom().nextInt(3) == 0 && saber.getItem() instanceof LightsaberItem item) {
            Vec3 look = holder.getLookAngle();
            double along = 0.6 + holder.getRandom().nextDouble() * 0.9;
            Vec3 side = new Vec3(-look.z, 0, look.x).normalize().scale(holder.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? 0.35 : -0.35);
            Vec3 p = holder.position().add(0, 1.1 + along * 0.6, 0).add(side).add(look.scale(along * 0.5));
            holder.level().addParticle(new DustParticleOptions(item.color().rgb(), 0.6F), p.x, p.y, p.z, 0, 0.01, 0);
        }
    }

    private void stopNow() {
        ACTIVE.remove(holder.getId());
        stop();
    }

    /** Called every client tick: starts hums for any nearby entity holding a lit saber. */
    static void tickAll() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ACTIVE.clear();
            return;
        }
        ACTIVE.values().removeIf(hum -> hum.isStopped() || hum.age > 20 && !mc.getSoundManager().isActive(hum));
        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(32.0))) {
            if (!ACTIVE.containsKey(entity.getId()) && !litSaber(entity).isEmpty()) {
                LightsaberHumSound hum = new LightsaberHumSound(entity);
                ACTIVE.put(entity.getId(), hum);
                mc.getSoundManager().play(hum);
            }
        }
        // Drop finished hums, and ones the engine never started/dropped (muted, channel pool full, sound reload).
        ACTIVE.values().removeIf(hum -> hum.isStopped() || hum.age > 20 && !mc.getSoundManager().isActive(hum));
    }

}
