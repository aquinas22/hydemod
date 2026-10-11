package com.hydemod.event;

import com.hydemod.HydeMod;
import com.hydemod.block.ChargingStationBlockEntity;
import com.hydemod.block.WarpPipeBlock;
import com.hydemod.entity.DepthGrowler;
import com.hydemod.entity.ElephantMario;
import com.hydemod.entity.Goober;
import com.hydemod.entity.SamHyde;
import com.hydemod.item.LightsaberItem;
import com.hydemod.registry.ModBlockEntities;
import com.hydemod.registry.ModBlocks;
import com.hydemod.registry.ModComponents;
import com.hydemod.registry.ModEntities;
import com.hydemod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;

@EventBusSubscriber(modid = HydeMod.MODID)
public final class CommonEvents {
    // ------------------------------------------------------------------ mod bus

    @SubscribeEvent
    static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SAM_HYDE.get(), SamHyde.createAttributes().build());
        event.put(ModEntities.ELEPHANT_MARIO.get(), ElephantMario.createAttributes().build());
        event.put(ModEntities.GOOBER.get(), Goober.createAttributes().build());
        event.put(ModEntities.DEPTH_GROWLER.get(), DepthGrowler.createAttributes().build());
    }

    @SubscribeEvent
    static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.GOOBER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Goober::checkGooberSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.DEPTH_GROWLER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                DepthGrowler::checkGrowlerSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    static void onCapabilities(RegisterCapabilitiesEvent event) {
        // Lightsabers accept Redstone Flux from any mod's charger.
        event.registerItem(Capabilities.Energy.ITEM,
                (stack, access) -> new ItemAccessEnergyHandler(access, ModComponents.ENERGY.get(), LightsaberItem.CAPACITY, LightsaberItem.MAX_TRANSFER),
                ModItems.GREEN_LIGHTSABER.get(), ModItems.BLUE_LIGHTSABER.get(), ModItems.RED_LIGHTSABER.get(), ModItems.PURPLE_LIGHTSABER.get());
        // The charging station accepts FE from cables/generators on every side.
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.CHARGING_STATION.get(), (be, side) -> be.getEnergy());
    }

    // ------------------------------------------------------------------ game bus

    /** Unlit or unpowered lightsabers are just metal tubes. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker && event.getSource().getEntity() == attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.getItem() instanceof LightsaberItem && !LightsaberItem.isActive(weapon)) {
                event.setAmount(Math.min(event.getAmount(), 1.0F));
            }
        }
    }

    /** Hyde's Armour full-set bonus, and sneaking while standing inside a warp pipe's rim. */
    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (player.tickCount % 20 == 0 && wearingFullHyde(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 3, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 3, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 3, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 15, 0, true, false, true));
        }
        if (player.isShiftKeyDown()) {
            BlockPos feet = player.blockPosition();
            if (level.getBlockState(feet).is(ModBlocks.WARP_PIPE.get())) {
                WarpPipeBlock.tryWarpFromInside(level, feet, player);
            } else if (level.getBlockState(feet.below()).is(ModBlocks.WARP_PIPE.get()) && player.onGround()) {
                WarpPipeBlock.tryWarpFromInside(level, feet.below(), player);
            }
        }
    }

    public static boolean wearingFullHyde(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HYDE_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HYDE_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HYDE_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HYDE_BOOTS.get());
    }

    private CommonEvents() {}
}
