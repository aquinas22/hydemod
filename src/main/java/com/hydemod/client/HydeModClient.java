package com.hydemod.client;

import com.hydemod.HydeMod;
import com.hydemod.registry.ModEntities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = HydeMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = HydeMod.MODID, value = Dist.CLIENT)
public final class HydeModClient {
    public HydeModClient() {}

    @SubscribeEvent
    static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HydeMobRenderers.SAM_HYDE_LAYER, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
        event.registerLayerDefinition(HydeMobRenderers.DEPTH_GROWLER_LAYER, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
        event.registerLayerDefinition(HydeMobRenderers.ELEPHANT_MARIO_LAYER, ElephantMarioModel::createBodyLayer);
        event.registerLayerDefinition(HydeMobRenderers.GOOBER_LAYER, GooberModel::createBodyLayer);
    }

    @SubscribeEvent
    static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SAM_HYDE.get(), HydeMobRenderers.SamHydeRenderer::new);
        event.registerEntityRenderer(ModEntities.DEPTH_GROWLER.get(), HydeMobRenderers.DepthGrowlerRenderer::new);
        event.registerEntityRenderer(ModEntities.ELEPHANT_MARIO.get(), HydeMobRenderers.ElephantMarioRenderer::new);
        event.registerEntityRenderer(ModEntities.GOOBER.get(), HydeMobRenderers.GooberRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_TRASH_CUBE.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.2F, false));
        event.registerEntityRenderer(ModEntities.TRUNK_SPRAY.get(), ctx -> new ThrownItemRenderer<>(ctx, 0.8F, true));
    }
}
