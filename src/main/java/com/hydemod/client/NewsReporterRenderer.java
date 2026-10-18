package com.hydemod.client;

import com.hydemod.HydeMod;
import com.hydemod.entity.NewsReporter;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

/** The existing player model layer gives both reporters articulated suit-clad limbs. */
public final class NewsReporterRenderer extends HumanoidMobRenderer<NewsReporter, AvatarRenderState, PlayerModel> {
    private final Identifier texture;
    public NewsReporterRenderer(EntityRendererProvider.Context context, String skin) {
        super(context, new PlayerModel(context.bakeLayer(HydeMobRenderers.SAM_HYDE_LAYER), false), .5F);
        texture = HydeMod.id("textures/entity/" + skin + ".png");
    }
    @Override public AvatarRenderState createRenderState() { return new AvatarRenderState(); }
    @Override public Identifier getTextureLocation(AvatarRenderState state) { return texture; }
}
