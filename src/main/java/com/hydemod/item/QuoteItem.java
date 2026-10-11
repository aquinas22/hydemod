package com.hydemod.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** An item that recites a numbered line from the talk when used, and shows a flavour tooltip. */
public class QuoteItem extends Item {
    private final String key;

    public QuoteItem(Properties properties, String key) {
        super(properties);
        this.key = key;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            for (int i = 1; i <= 4; i++) {
                player.sendSystemMessage(Component.translatable("quote.hydemod." + key + "." + i).withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD));
            }
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 40);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.hydemod." + key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
