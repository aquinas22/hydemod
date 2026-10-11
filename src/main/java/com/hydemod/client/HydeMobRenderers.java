package com.hydemod.client;

import com.hydemod.HydeMod;
import com.hydemod.entity.DepthGrowler;
import com.hydemod.entity.ElephantMario;
import com.hydemod.entity.Goober;
import com.hydemod.entity.SamHyde;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** All of HydeMod's mob renderers and model layer ids. */
public final class HydeMobRenderers {
    public static final ModelLayerLocation SAM_HYDE_LAYER = new ModelLayerLocation(HydeMod.id("sam_hyde"), "main");
    public static final ModelLayerLocation DEPTH_GROWLER_LAYER = new ModelLayerLocation(HydeMod.id("depth_growler"), "main");
    public static final ModelLayerLocation ELEPHANT_MARIO_LAYER = new ModelLayerLocation(HydeMod.id("elephant_mario"), "main");
    public static final ModelLayerLocation GOOBER_LAYER = new ModelLayerLocation(HydeMod.id("goober"), "main");

    public static class SamHydeRenderer extends HumanoidMobRenderer<SamHyde, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
        private static final Identifier TEXTURE = HydeMod.id("textures/entity/sam_hyde.png");

        public SamHydeRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(SAM_HYDE_LAYER)), 0.5F);
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state) {
            return TEXTURE;
        }

        @Override
        protected void scale(HumanoidRenderState state, PoseStack poseStack) {
            poseStack.scale(1.08F, 1.08F, 1.08F); // he's a tall guy
        }
    }

    public static class DepthGrowlerRenderer extends HumanoidMobRenderer<DepthGrowler, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
        private static final Identifier TEXTURE = HydeMod.id("textures/entity/depth_growler.png");

        public DepthGrowlerRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(DEPTH_GROWLER_LAYER)), 0.5F);
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state) {
            return TEXTURE;
        }

        @Override
        protected void scale(HumanoidRenderState state, PoseStack poseStack) {
            poseStack.scale(0.95F, 0.92F, 0.95F); // hunched from endless toil
        }
    }

    public static class ElephantMarioRenderer extends MobRenderer<ElephantMario, LivingEntityRenderState, ElephantMarioModel> {
        private static final Identifier TEXTURE = HydeMod.id("textures/entity/elephant_mario.png");

        public ElephantMarioRenderer(EntityRendererProvider.Context context) {
            super(context, new ElephantMarioModel(context.bakeLayer(ELEPHANT_MARIO_LAYER)), 1.1F);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return TEXTURE;
        }

        @Override
        protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
            poseStack.scale(1.25F, 1.25F, 1.25F);
        }
    }

    public static class GooberRenderer extends MobRenderer<Goober, LivingEntityRenderState, GooberModel> {
        private static final Identifier TEXTURE = HydeMod.id("textures/entity/goober.png");

        public GooberRenderer(EntityRendererProvider.Context context) {
            super(context, new GooberModel(context.bakeLayer(GOOBER_LAYER)), 0.4F);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return TEXTURE;
        }
    }

    private HydeMobRenderers() {}
}
