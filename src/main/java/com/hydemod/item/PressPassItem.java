package com.hydemod.item;

import com.hydemod.entity.NewsReporter;
import com.hydemod.registry.ModEntities;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

/** A reusable invitation: use on clear ground for Peter, sneak-use for Chris. */
public final class PressPassItem extends Item {
    public PressPassItem(Properties properties) { super(properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        var type = player.isShiftKeyDown() ? ModEntities.CHRIS_MORRIS.get() : ModEntities.PETER_OHANRAHAN.get();
        var pos = context.getClickedPos().relative(context.getClickedFace());
        NewsReporter reporter = type.create(level, EntitySpawnReason.TRIGGERED);
        if (reporter == null) return InteractionResult.FAIL;
        reporter.snapTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, player.getYRot() + 180, 0);
        if (!level.noCollision(reporter, reporter.getBoundingBox()) || level.containsAnyLiquid(reporter.getBoundingBox())
                || !level.getWorldBorder().isWithinBounds(reporter.getBoundingBox())) {
            player.sendOverlayMessage(Component.translatable("message.hydemod.news.no_room"));
            return InteractionResult.FAIL;
        }
        if (!player.isCreative() && !level.getEntitiesOfClass(NewsReporter.class, reporter.getBoundingBox().inflate(24),
                other -> other.isAlive() && other.getType() == type).isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.hydemod.news.already_here"));
            return InteractionResult.FAIL;
        }
        if (!level.addFreshEntity(reporter)) return InteractionResult.FAIL;
        player.getCooldowns().addCooldown(context.getItemInHand(), 100);
        player.sendSystemMessage(Component.translatable("message.hydemod.news.arrival", reporter.getDisplayName()));
        return InteractionResult.SUCCESS;
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> builder, TooltipFlag flag) { HydeTooltips.add(stack, builder); }
}
