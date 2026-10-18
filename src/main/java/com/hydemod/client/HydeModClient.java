package com.hydemod.client;

import com.hydemod.HydeMod;
import com.hydemod.registry.ModEntities;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@Mod(value = HydeMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = HydeMod.MODID, value = Dist.CLIENT)
public final class HydeModClient {
    public HydeModClient() {}

    @SubscribeEvent
    static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Full player-skin layout (separate left limbs + jacket/sleeve/pants overlays): any standard 64x64 skin works.
        event.registerLayerDefinition(HydeMobRenderers.SAM_HYDE_LAYER, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64));
        event.registerLayerDefinition(HydeMobRenderers.ELEPHANT_MARIO_LAYER, ElephantMarioModel::createBodyLayer);
        event.registerLayerDefinition(HydeMobRenderers.DEPTH_GROVLER_LAYER, DepthGrovlerModel::createBodyLayer);
    }

    @SubscribeEvent
    static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SAM_HYDE.get(), HydeMobRenderers.SamHydeRenderer::new);
        event.registerEntityRenderer(ModEntities.ELEPHANT_MARIO.get(), HydeMobRenderers.ElephantMarioRenderer::new);
        event.registerEntityRenderer(ModEntities.DEPTH_GROVLER.get(), HydeMobRenderers.DepthGrovlerRenderer::new);
        event.registerEntityRenderer(ModEntities.PETER_OHANRAHAN.get(), ctx -> new NewsReporterRenderer(ctx, "peter_ohanrahan"));
        event.registerEntityRenderer(ModEntities.CHRIS_MORRIS.get(), ctx -> new NewsReporterRenderer(ctx, "chris_morris"));
        event.registerEntityRenderer(ModEntities.THROWN_TRASH_CUBE.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.2F, false));
        event.registerEntityRenderer(ModEntities.TRUNK_SPRAY.get(), ctx -> new ThrownItemRenderer<>(ctx, 0.8F, true));
        event.registerEntityRenderer(ModEntities.THROWN_BOMB.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.0F, false));
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        LightsaberHumSound.tickAll();
        BossMusic.tick();
        BossCallOverlay.tick();
    }

    @SubscribeEvent
    static void onGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(BossCallOverlay.ID, BossCallOverlay::render);
    }
}
