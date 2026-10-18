package com.hydemod.item;

import com.hydemod.block.WarpPipeBlockEntity;
import com.hydemod.registry.ModComponents;
import com.hydemod.registry.ModSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

/** Right-click one warp pipe, then another, to link them together (works across dimensions). */
public class PipeWrenchItem extends Item {
    public PipeWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || !(level.getBlockEntity(context.getClickedPos()) instanceof WarpPipeBlockEntity pipe)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        GlobalPos here = GlobalPos.of(level.dimension(), pos);
        if (player != null && player.isShiftKeyDown()) {
            unlink(level, here, pipe);
            stack.remove(ModComponents.PIPE_LINK.get());
            message(player, Component.translatable("message.hydemod.wrench.unlinked"));
            level.playSound(null, pos, ModSounds.PIPE_LINK.get(), SoundSource.BLOCKS, 0.7F, 0.8F);
            return InteractionResult.SUCCESS;
        }
        GlobalPos first = stack.get(ModComponents.PIPE_LINK.get());
        if (first == null || first.equals(here)) {
            stack.set(ModComponents.PIPE_LINK.get(), here);
            message(player, Component.translatable("message.hydemod.wrench.first", pos.getX(), pos.getY(), pos.getZ()));
            level.playSound(null, pos, ModSounds.PIPE_LINK.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        } else {
            ServerLevel otherLevel = level.getServer().getLevel(first.dimension());
            if (otherLevel != null) otherLevel.getChunk(first.pos());
            if (otherLevel != null && otherLevel.getBlockEntity(first.pos()) instanceof WarpPipeBlockEntity other) {
                unlink(level, here, pipe);
                unlink(otherLevel, first, other);
                pipe.setTarget(first);
                other.setTarget(here);
                message(player, Component.translatable("message.hydemod.wrench.linked"));
                level.playSound(null, pos, ModSounds.PIPE_LINK.get(), SoundSource.BLOCKS, 0.9F, 1.2F);
            } else {
                message(player, Component.translatable("message.hydemod.wrench.missing"));
            }
            stack.remove(ModComponents.PIPE_LINK.get());
        }
        return InteractionResult.SUCCESS;
    }

    private static void unlink(ServerLevel level, GlobalPos here, WarpPipeBlockEntity pipe) {
        GlobalPos previous = pipe.getTarget();
        pipe.setTarget(null);
        if (previous != null) {
            ServerLevel otherLevel = level.getServer().getLevel(previous.dimension());
            if (otherLevel != null) {
                otherLevel.getChunk(previous.pos());
                if (otherLevel.getBlockEntity(previous.pos()) instanceof WarpPipeBlockEntity other && here.equals(other.getTarget())) {
                    other.setTarget(null);
                }
            }
        }
    }

    private static void message(Player player, Component text) {
        if (player != null) {
            player.sendSystemMessage(text.copy().withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HydeTooltips.add(stack, builder);
        GlobalPos first = stack.get(ModComponents.PIPE_LINK.get());
        if (first != null) {
            builder.accept(Component.translatable("tooltip.hydemod.pipe_wrench.pending", first.pos().getX(), first.pos().getY(), first.pos().getZ())
                    .withStyle(ChatFormatting.GREEN));
        }
    }
}
