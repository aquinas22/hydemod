package com.hydemod.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Adds the "tooltip.hydemod.&lt;item&gt;" flavour line to any of our items that has one. */
public final class HydeTooltips {
    public static void add(ItemStack stack, Consumer<Component> builder) {
        String key = "tooltip.hydemod." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (Language.getInstance().has(key)) {
            builder.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    private HydeTooltips() {}
}
