package com.hydemod.item;

import com.hydemod.registry.ModComponents;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * "I'm going to have your mind uploaded to my BlackBerry."
 * Sneak + use uploads your mind (saves your location). Use downloads it again (teleports you back).
 */
public class BlackberryItem extends Item {
    public BlackberryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            stack.set(ModComponents.UPLOADED_MIND.get(), GlobalPos.of(level.dimension(), player.blockPosition()));
            player.sendSystemMessage(Component.translatable("message.hydemod.blackberry.uploaded").withStyle(ChatFormatting.AQUA));
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 1.0F, 2.0F);
            return InteractionResult.SUCCESS;
        }
        GlobalPos mind = stack.get(ModComponents.UPLOADED_MIND.get());
        if (mind == null) {
            player.sendSystemMessage(Component.translatable("message.hydemod.blackberry.empty").withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }
        ServerLevel target = serverLevel.getServer().getLevel(mind.dimension());
        if (target == null) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = mind.pos();
        serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 40, 0.5, 1, 0.5, 0.2);
        player.teleportTo(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
        target.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("message.hydemod.blackberry.downloaded").withStyle(ChatFormatting.AQUA));
        player.getCooldowns().addCooldown(stack, 20 * 30);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HydeTooltips.add(stack, builder);
        GlobalPos mind = stack.get(ModComponents.UPLOADED_MIND.get());
        if (mind != null) {
            builder.accept(Component.translatable("tooltip.hydemod.blackberry.saved", mind.pos().getX(), mind.pos().getY(), mind.pos().getZ(),
                    mind.dimension().identifier().toString()).withStyle(ChatFormatting.AQUA));
        }
    }
}
