package com.hydemod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Blows your socks off. Literally: your boots pop off your feet. */
public class SeaCheesyBakedPotatoItem extends Item {
    public SeaCheesyBakedPotatoItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (!boots.isEmpty()) {
                player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                if (!player.getInventory().add(boots)) {
                    player.drop(boots, false);
                }
            }
            player.sendSystemMessage(Component.translatable("message.hydemod.socks_blown_off").withStyle(ChatFormatting.AQUA));
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
