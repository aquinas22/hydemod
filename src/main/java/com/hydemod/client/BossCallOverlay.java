package com.hydemod.client;

import com.hydemod.HydeMod;
import com.hydemod.entity.ElephantMario;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * "Elephant Mario is calling..." - while you're fighting Elephant Mario, an incoming-call card
 * flashes onto the screen with his portrait, a ringtone and pulsing answer/decline buttons.
 * Replace the portrait with a resource pack: assets/hydemod/textures/gui/elephant_mario_call.png
 */
public final class BossCallOverlay {
    public static final Identifier ID = HydeMod.id("boss_call");
    private static final Identifier PORTRAIT = HydeMod.id("textures/gui/elephant_mario_call.png");
    private static final int SHOW_TICKS = 80;
    private static final int REPEAT_TICKS = 500;

    private static int showTicks;
    private static int cooldown;
    private static boolean wasFighting;

    /** Client tick: decide when the call rings. */
    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            showTicks = 0;
            wasFighting = false;
            return;
        }
        boolean fighting = !mc.level.getEntitiesOfClass(ElephantMario.class, mc.player.getBoundingBox().inflate(40.0), ElephantMario::isAlive).isEmpty();
        if (fighting && (!wasFighting || --cooldown <= 0)) {
            showTicks = SHOW_TICKS;
            cooldown = REPEAT_TICKS;
        }
        wasFighting = fighting;
        if (showTicks > 0) {
            int t = SHOW_TICKS - showTicks;
            if (t < 60 && t % 15 == 0) { // ring-ring
                mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 0.7F, 1.6F);
                mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 0.7F, 2.0F);
            }
            showTicks--;
        }
    }

    static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        if (showTicks <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        float partial = delta.getGameTimeDeltaPartialTick(false);
        float t = SHOW_TICKS - showTicks + partial;
        int w = g.guiWidth();

        // Screen flash on the first few ticks.
        if (t < 6) {
            int a = (int) (180 * (1 - t / 6));
            g.fill(0, 0, w, g.guiHeight(), (a << 24) | 0xFFFFFF);
        }
        // Slide in from the top, slide out at the end, with a ringing wobble.
        float in = Mth.clamp(t / 6F, 0, 1), out = Mth.clamp(showTicks / 6F, 0, 1);
        int cardW = 190, cardH = 64;
        int x = (w - cardW) / 2 + (int) (Mth.sin(t * 2.2F) * (t < 60 ? 2 : 0));
        int y = (int) (34 - (1 - Math.min(in, out)) * (cardH + 40)); // below the boss bar

        g.fill(x, y, x + cardW, y + cardH, 0xE0202028);              // card
        g.fill(x, y, x + cardW, y + 1, 0xFF5A5A70);                   // highlight edge
        g.fill(x + 6, y + 6, x + 58, y + 58, 0xFF101014);             // portrait frame
        g.blit(RenderPipelines.GUI_TEXTURED, PORTRAIT, x + 8, y + 8, 0, 0, 48, 48, 64, 64, 64, 64);

        g.text(font, Component.translatable("call.hydemod.elephant_mario.name"), x + 66, y + 10, 0xFFFFFFFF, true);
        String dots = ".".repeat(1 + (int) (t / 8) % 3);
        g.text(font, Component.translatable("call.hydemod.elephant_mario.calling").getString() + dots, x + 66, y + 23, 0xFFB8B8C8, false);

        // Pulsing decline / answer buttons.
        float pulse = 0.5F + 0.5F * Mth.sin(t * 0.5F);
        int r = 7 + (int) (pulse * 2);
        circle(g, x + 90, y + 47, r, 0xFFE0403A);
        circle(g, x + 150, y + 47, r, 0xFF38C25A);
        g.centeredText(font, "✖", x + 90, y + 43, 0xFFFFFFFF);
        g.centeredText(font, "☎", x + 150, y + 43, 0xFFFFFFFF);
    }

    private static void circle(GuiGraphicsExtractor g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private BossCallOverlay() {}
}
