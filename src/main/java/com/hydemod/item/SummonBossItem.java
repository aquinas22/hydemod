package com.hydemod.item;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

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
        Mob mob = boss.get().spawn(level, pos, EntitySpawnReason.TRIGGERED);
        if (mob == null) {
            return InteractionResult.FAIL;
        }
        level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(messageKey).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        level.playSound(null, pos, SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 4.0F, 1.0F);
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
