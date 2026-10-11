package com.hydemod.client;

import com.hydemod.HydeMod;
import com.hydemod.entity.ElephantMario;
import com.hydemod.entity.DepthGrovler;
import com.hydemod.entity.SamHyde;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** All of HydeMod's mob renderers and model layer ids. */
public final class HydeMobRenderers {
    public static final ModelLayerLocation SAM_HYDE_LAYER = new ModelLayerLocation(HydeMod.id("sam_hyde"), "main");
    public static final ModelLayerLocation ELEPHANT_MARIO_LAYER = new ModelLayerLocation(HydeMod.id("elephant_mario"), "main");
    public static final ModelLayerLocation DEPTH_GROVLER_LAYER = new ModelLayerLocation(HydeMod.id("depth_grovler"), "main");

    public static class SamHydeRenderer extends HumanoidMobRenderer<SamHyde, BossRenderStates.SamHydeState, SamHydeModel> {
        private static final Identifier TEXTURE = HydeMod.id("textures/entity/sam_hyde.png");

        public SamHydeRenderer(EntityRendererProvider.Context context) {
            super(context, new SamHydeModel(context.bakeLayer(SAM_HYDE_LAYER)), 0.5F);
        }

        @Override
        public BossRenderStates.SamHydeState createRenderState() {
            return new BossRenderStates.SamHydeState();
        }

        @Override
        public void extractRenderState(SamHyde entity, BossRenderStates.SamHydeState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            BossRenderStates.copy(entity, partialTicks, state);
        }

        @Override
        public Identifier getTextureLocation(BossRenderStates.SamHydeState state) {
            return TEXTURE;
        }

        @Override
        protected void scale(BossRenderStates.SamHydeState state, PoseStack poseStack) {
            poseStack.scale(1.08F, 1.08F, 1.08F); // he's a tall guy
        }
    }

    public static class ElephantMarioRenderer extends MobRenderer<ElephantMario, BossRenderStates.ElephantState, ElephantMarioModel> {
        private static final Identifier TEXTURE = HydeMod.id("textures/entity/elephant_mario.png");

        public ElephantMarioRenderer(EntityRendererProvider.Context context) {
            super(context, new ElephantMarioModel(context.bakeLayer(ELEPHANT_MARIO_LAYER)), 1.1F);
        }

        @Override
        public BossRenderStates.ElephantState createRenderState() {
            return new BossRenderStates.ElephantState();
        }

        @Override
        public void extractRenderState(ElephantMario entity, BossRenderStates.ElephantState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            BossRenderStates.copy(entity, partialTicks, state);
        }

        @Override
        public Identifier getTextureLocation(BossRenderStates.ElephantState state) {
            return TEXTURE;
        }

        @Override
        protected void scale(BossRenderStates.ElephantState state, PoseStack poseStack) {
            poseStack.scale(1.25F, 1.25F, 1.25F);
        }
    }

    public static class DepthGrovlerRenderer extends MobRenderer<DepthGrovler, LivingEntityRenderState, DepthGrovlerModel> {
        private static final Identifier TEXTURE = HydeMod.id("textures/entity/depth_grovler.png");

        public DepthGrovlerRenderer(EntityRendererProvider.Context context) {
            super(context, new DepthGrovlerModel(context.bakeLayer(DEPTH_GROVLER_LAYER)), 0.4F);
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
