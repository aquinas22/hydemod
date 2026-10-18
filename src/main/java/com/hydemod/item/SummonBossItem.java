package com.hydemod.item;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import com.hydemod.entity.HydeBoss;

/** Used on a block to summon a boss on top of it. */
public class SummonBossItem extends Item {
    private final Supplier<? extends EntityType<? extends Mob>> boss;
    private final String messageKey;

    public SummonBossItem(Properties properties, Supplier<? extends EntityType<? extends Mob>> boss, String messageKey) {
        super(properties);
        this.boss = boss;
        this.messageKey = messageKey;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendOverlayMessage(Component.translatable("message.hydemod.boss_peaceful").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Mob preview = boss.get().create(level, EntitySpawnReason.TRIGGERED);
        if (preview == null) return InteractionResult.FAIL;
        preview.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        if (!level.noCollision(preview, preview.getBoundingBox()) || level.containsAnyLiquid(preview.getBoundingBox())
                || !level.getWorldBorder().isWithinBounds(preview.getBoundingBox())) {
            if (context.getPlayer() != null) context.getPlayer().sendOverlayMessage(
                    Component.translatable("message.hydemod.boss_no_room").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() != null && !context.getPlayer().isCreative()
                && !level.getEntitiesOfClass(HydeBoss.class, new AABB(pos).inflate(24), HydeBoss::isAlive).isEmpty()) {
            context.getPlayer().sendOverlayMessage(Component.translatable("message.hydemod.boss_already_here").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        Mob mob = boss.get().spawn(level, pos, EntitySpawnReason.TRIGGERED);
        if (mob == null) {
            return InteractionResult.FAIL;
        }
        for (var player : level.players()) {
            if (player.distanceToSqr(mob) < 64 * 64) player.sendSystemMessage(
                    Component.translatable(messageKey).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        }
        if (context.getPlayer() != null) {
            context.getItemInHand().consume(1, context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HydeTooltips.add(stack, builder);
    }
}
