package com.hydemod.item;

import com.hydemod.registry.ModComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A lightsaber powered by Redstone Flux (FE). Right-click toggles the blade. A lit blade
 * drains a trickle of FE while held and a chunk per hit; unlit or flat, it is a 1-damage club.
 * Charge it in a Charging Station (redstone or any FE source) or any other mod's FE charger.
 */
public class LightsaberItem extends Item {
    public static final int CAPACITY = 200_000;
    public static final int MAX_TRANSFER = 5_000;
    public static final int ENERGY_PER_HIT = 400;
    public static final int ENERGY_PER_TICK = 2;

    private final KyberColor color;

    public LightsaberItem(Properties properties, KyberColor color) {
        super(properties);
        this.color = color;
    }

    public static Properties properties(Properties p, KyberColor color) {
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, color.attackDamage() - 1.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
        p = p.stacksTo(1).attributes(attributes).component(ModComponents.ENERGY.get(), 0);
        if (color == KyberColor.RED || color == KyberColor.PURPLE) {
            p = p.fireResistant();
        }
        return p;
    }

    public static ItemStack fullyCharged(LightsaberItem item) {
        ItemStack stack = new ItemStack(item);
        stack.set(ModComponents.ENERGY.get(), CAPACITY);
        return stack;
    }

    public KyberColor color() {
        return color;
    }

    public static int getEnergy(ItemStack stack) {
        return stack.getOrDefault(ModComponents.ENERGY.get(), 0);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        stack.set(ModComponents.ENERGY.get(), Mth.clamp(energy, 0, CAPACITY));
    }

    public static boolean isIgnited(ItemStack stack) {
        return stack.has(ModComponents.IGNITED.get());
    }

    /** True when the blade is lit and has power: full damage applies. */
    public static boolean isActive(ItemStack stack) {
        return stack.getItem() instanceof LightsaberItem && isIgnited(stack) && getEnergy(stack) >= ENERGY_PER_HIT;
    }

    private static void setIgnited(ItemStack stack, boolean on) {
        if (on) {
            stack.set(ModComponents.IGNITED.get(), net.minecraft.util.Unit.INSTANCE);
        } else {
            stack.remove(ModComponents.IGNITED.get());
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            boolean turnOn = !isIgnited(stack);
            if (turnOn && getEnergy(stack) < ENERGY_PER_HIT) {
                player.sendOverlayMessage(Component.translatable("message.hydemod.lightsaber.no_power").withStyle(ChatFormatting.RED));
                level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.7F, 1.4F);
                return InteractionResult.FAIL;
            }
            setIgnited(stack, turnOn);
            level.playSound(null, player.blockPosition(), turnOn ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.PLAYERS, 0.8F, turnOn ? 1.9F : 1.6F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!isIgnited(stack)) {
            return;
        }
        boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
        if (!held) {
            setIgnited(stack, false); // blades retract when holstered
            return;
        }
        int energy = getEnergy(stack) - ENERGY_PER_TICK;
        if (energy < ENERGY_PER_HIT) {
            setIgnited(stack, false);
            level.playSound(null, owner.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);
            if (owner instanceof Player player) {
                player.sendOverlayMessage(Component.translatable("message.hydemod.lightsaber.depleted").withStyle(ChatFormatting.RED));
            }
        }
        setEnergy(stack, energy);
        if (level.getGameTime() % 40 == 0) {
            level.playSound(null, owner.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.35F, 2.0F);
        }
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (isActive(stack)) {
            setEnergy(stack, getEnergy(stack) - ENERGY_PER_HIT);
            target.igniteForSeconds(color == KyberColor.RED || color == KyberColor.PURPLE ? 4 : 2);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getEnergy(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF2020; // redstone red
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.hydemod.lightsaber.energy", String.format("%,d", getEnergy(stack)), String.format("%,d", CAPACITY))
                .withStyle(ChatFormatting.RED));
        builder.accept(Component.translatable(isIgnited(stack) ? "tooltip.hydemod.lightsaber.on" : "tooltip.hydemod.lightsaber.off")
                .withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.hydemod.lightsaber.crystal." + color.getSerializedName()).withColor(color.rgb()));
    }
}
