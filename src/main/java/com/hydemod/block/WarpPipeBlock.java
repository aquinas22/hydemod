package com.hydemod.block;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.hydemod.item.PipeWrenchItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import org.jspecify.annotations.Nullable;

/**
 * Warp pipe. Link two with a Pipe Wrench, then stand on one and sneak to travel to the other.
 */
public class WarpPipeBlock extends BaseEntityBlock {
    public static final MapCodec<WarpPipeBlock> CODEC = simpleCodec(WarpPipeBlock::new);
    private static final VoxelShape SHAPE = Shapes.join(
            Shapes.or(Block_box(1, 0, 1, 15, 12, 15), Block_box(0, 12, 0, 16, 16, 16)),
            Block_box(3, 14, 3, 13, 16, 13), BooleanOp.ONLY_FIRST);

    private static VoxelShape Block_box(double x1, double y1, double z1, double x2, double y2, double z2) {
        return net.minecraft.world.level.block.Block.box(x1, y1, z1, x2, y2, z2);
    }

    public WarpPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WarpPipeBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Let the Pipe Wrench's own useOn handle linking instead of the pipe's status message. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof PipeWrenchItem) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WarpPipeBlockEntity pipe) {
            GlobalPos t = pipe.getTarget();
            Component msg = t == null
                    ? Component.translatable("message.hydemod.warp_pipe.unlinked")
                    : Component.translatable("message.hydemod.warp_pipe.linked", t.pos().getX(), t.pos().getY(), t.pos().getZ(), t.dimension().identifier().toString());
            player.sendOverlayMessage(msg.copy().withStyle(ChatFormatting.GREEN));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level instanceof ServerLevel serverLevel && entity.isShiftKeyDown() && !entity.isOnPortalCooldown()
                && level.getBlockEntity(pos) instanceof WarpPipeBlockEntity pipe) {
            warp(serverLevel, pos, pipe, entity);
        }
    }

    /** Also catch players sneaking while standing in the pipe's dip (stepOn may not fire there). */
    public static void tryWarpFromInside(ServerLevel level, BlockPos pos, Entity entity) {
        if (entity.isShiftKeyDown() && !entity.isOnPortalCooldown() && level.getBlockEntity(pos) instanceof WarpPipeBlockEntity pipe) {
            warp(level, pos, pipe, entity);
        }
    }

    private static void warp(ServerLevel level, BlockPos pos, WarpPipeBlockEntity pipe, Entity entity) {
        GlobalPos target = pipe.getTarget();
        if (target == null) {
            return;
        }
        ServerLevel targetLevel = level.getServer().getLevel(target.dimension());
        if (targetLevel == null || !(targetLevel.getBlockEntity(target.pos()) instanceof WarpPipeBlockEntity)) {
            pipe.setTarget(null);
            if (entity instanceof Player player) {
                player.sendOverlayMessage(Component.translatable("message.hydemod.wrench.missing").withStyle(ChatFormatting.RED));
            }
            return;
        }
        level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 1.0F, 0.6F);
        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 15, 0.3, 0.2, 0.3, 0.02);
        BlockPos dest = target.pos();
        entity.setPortalCooldown(40);
        entity.teleportTo(targetLevel, dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5, Set.of(), entity.getYRot(), entity.getXRot(), true);
        targetLevel.playSound(null, dest, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0F, 1.4F);
        targetLevel.sendParticles(ParticleTypes.CLOUD, dest.getX() + 0.5, dest.getY() + 1.1, dest.getZ() + 0.5, 15, 0.3, 0.2, 0.3, 0.02);
    }
}
