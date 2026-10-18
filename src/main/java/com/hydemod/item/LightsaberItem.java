package com.hydemod.item;

import com.hydemod.registry.ModComponents;
import java.util.function.Consumer;
import com.hydemod.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
 * A permanently charged lightsaber. Right-click toggles the blade; holstering retracts it.
 * The legacy energy component is retained so existing stacks and charging stations still work.
 */
public class LightsaberItem extends Item {
    public static final int CAPACITY = 200_000;
    public static final int MAX_TRANSFER = 5_000;

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
        p = p.stacksTo(1).attributes(attributes).component(ModComponents.ENERGY.get(), CAPACITY);
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
        return CAPACITY;
    }

    public static void setEnergy(ItemStack stack, int energy) {
        if (stack.getOrDefault(ModComponents.ENERGY.get(), 0) != CAPACITY) {
            stack.set(ModComponents.ENERGY.get(), CAPACITY);
        }
    }

    public static boolean isIgnited(ItemStack stack) {
        return stack.has(ModComponents.IGNITED.get());
    }

    /** True when the blade is lit: full damage applies. */
    public static boolean isActive(ItemStack stack) {
        return stack.getItem() instanceof LightsaberItem && isIgnited(stack);
    }

    /** Current blade extension 0-4 (0 = no blade, 4 = fully lit). */
    public static int bladeLength(ItemStack stack) {
        return stack.getOrDefault(ModComponents.BLADE.get(), 0);
    }

    private static void setBlade(ItemStack stack, int length) {
        if (length <= 0) {
            stack.remove(ModComponents.BLADE.get());
        } else {
            stack.set(ModComponents.BLADE.get(), Math.min(4, length));
        }
    }

    /** Lights or retracts the blade; the blade then grows/shrinks one step per tick. */
    private static void setIgnited(ItemStack stack, boolean on) {
        if (on) {
            stack.set(ModComponents.IGNITED.get(), net.minecraft.util.Unit.INSTANCE);
            if (bladeLength(stack) == 0) {
                setBlade(stack, 1);
            }
        } else {
            stack.remove(ModComponents.IGNITED.get());
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            boolean turnOn = !isIgnited(stack);
            setEnergy(stack, CAPACITY);
            setIgnited(stack, turnOn);
            level.playSound(null, player.getX(), player.getY() + 1, player.getZ(),
                    turnOn ? ModSounds.SABER_IGNITE.get() : ModSounds.SABER_RETRACT.get(), SoundSource.PLAYERS, 1.0F, 0.95F + level.getRandom().nextFloat() * 0.1F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        setEnergy(stack, CAPACITY); // migrate depleted stacks from earlier versions
        boolean ignited = isIgnited(stack);
        int blade = bladeLength(stack);
        // Animate the blade sliding out of / back into the hilt.
        if (ignited && blade < 4) {
            setBlade(stack, blade + 1);
        } else if (!ignited && blade > 0) {
            setBlade(stack, blade - 1);
        }
        if (!ignited) {
            return;
        }
        boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
        if (!held) {
            setIgnited(stack, false); // blades retract when holstered
            setBlade(stack, 0);
            return;
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (isActive(stack) && !entity.level().isClientSide()) {
            entity.level().playSound(null, entity.getX(), entity.getEyeY(), entity.getZ(), ModSounds.SABER_SWING.get(),
                    entity.getSoundSource(), 0.9F, 0.9F + entity.getRandom().nextFloat() * 0.25F);
        }
        return false;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (isActive(stack)) {
            setEnergy(stack, CAPACITY);
            target.igniteForSeconds(color == KyberColor.RED || color == KyberColor.PURPLE ? 4 : 2);
            if (target.level() instanceof ServerLevel level) {
                level.playSound(null, target.getX(), target.getY(0.5), target.getZ(), ModSounds.SABER_HIT.get(), SoundSource.PLAYERS,
                        1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                level.sendParticles(new DustParticleOptions(color.rgb(), 1.4F), target.getX(), target.getY(0.6), target.getZ(), 18, 0.25, 0.35, 0.25, 0.0);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.6), target.getZ(), 12, 0.2, 0.3, 0.2, 0.25);
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.6), target.getZ(), 6, 0.2, 0.3, 0.2, 0.3);
            }
        }
    }

    /** Energy/blade component updates must not replay the equip animation in first person. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !newStack.is(oldStack.getItem());
    }

    /** ...nor reset block-breaking progress while mining with a lit saber in hand. */
    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !newStack.is(oldStack.getItem());
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
        return color.rgb();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.hydemod.lightsaber.energy", String.format("%,d", getEnergy(stack)), String.format("%,d", CAPACITY))
                .withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("tooltip.hydemod.lightsaber.infinite").withStyle(ChatFormatting.GOLD));
        builder.accept(Component.translatable(isIgnited(stack) ? "tooltip.hydemod.lightsaber.on" : "tooltip.hydemod.lightsaber.off")
                .withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.hydemod.lightsaber.crystal." + color.getSerializedName()).withColor(color.rgb()));
    }
}
