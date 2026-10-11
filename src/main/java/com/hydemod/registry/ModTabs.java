package com.hydemod.registry;

import com.hydemod.HydeMod;
import com.hydemod.item.LightsaberItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HydeMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HYDEMOD = TABS.register("hydemod", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.hydemod"))
            .icon(() -> new ItemStack(ModItems.TED_MICROPHONE.get()))
            .displayItems((params, output) -> {
                for (var holder : ModItems.ITEMS.getEntries()) {
                    Item item = holder.get();
                    if (item instanceof LightsaberItem saber) {
                        output.accept(saber.getDefaultInstance());
                        output.accept(LightsaberItem.fullyCharged(saber));
                    } else {
                        output.accept(item);
                    }
                }
            })
            .build());

    private ModTabs() {}
}
