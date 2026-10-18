package com.hydemod.block;

import com.hydemod.item.PipeWrenchItem;
import com.hydemod.registry.ModSounds;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Hold sneak for a short warp charge; linked exits can give an optional little launch. */
public class WarpPipeBlock extends BaseEntityBlock {
    public static final MapCodec<WarpPipeBlock> CODEC = simpleCodec(WarpPipeBlock::new);
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");
    private static final int CHARGE_TICKS = 12;
    private static final VoxelShape SHAPE = Shapes.join(
            Shapes.or(Block.box(1, 0, 1, 15, 12, 15), Block.box(0, 12, 0, 16, 16, 16)),
            Block.box(3, 14, 3, 13, 16, 13), BooleanOp.ONLY_FIRST);
    private static final Set<UUID> WARPED = new java.util.HashSet<>();
    private static final Map<UUID, Charge> CHARGES = new HashMap<>();
    private record Charge(GlobalPos origin, long began) { }

    public static void clearWarped(Entity entity) {
        WARPED.remove(entity.getUUID());
        CHARGES.remove(entity.getUUID());
    }

    public static void cancelCharge(Entity entity) { CHARGES.remove(entity.getUUID()); }

    public WarpPipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LINKED, false));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LINKED); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WarpPipeBlockEntity(pos, state); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LINKED) && random.nextInt(3) == 0) {
            double angle = random.nextDouble() * Math.PI * 2;
            level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5 + Math.cos(angle) * 0.35,
                    pos.getY() + 1.08, pos.getZ() + 0.5 + Math.sin(angle) * 0.35, 0, 0.02, 0);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof PipeWrenchItem) return InteractionResult.PASS;
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WarpPipeBlockEntity pipe) {
            if (player.isShiftKeyDown()) {
                pipe.setLaunchExit(!pipe.isLaunchExit());
                player.sendOverlayMessage(Component.translatable(pipe.isLaunchExit()
                        ? "message.hydemod.warp_pipe.launch" : "message.hydemod.warp_pipe.gentle").withStyle(ChatFormatting.AQUA));
                level.playSound(null, pos, ModSounds.PIPE_LINK.get(), SoundSource.BLOCKS, 0.6F, pipe.isLaunchExit() ? 1.3F : 0.9F);
            } else {
                GlobalPos t = pipe.getTarget();
                player.sendOverlayMessage((t == null ? Component.translatable("message.hydemod.warp_pipe.unlinked")
                        : Component.translatable("message.hydemod.warp_pipe.linked", t.pos().getX(), t.pos().getY(), t.pos().getZ(),
                        t.dimension().identifier().toString())).withStyle(ChatFormatting.GREEN));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level instanceof ServerLevel serverLevel && entity instanceof Player && entity.isShiftKeyDown()) {
            tryWarpFromInside(serverLevel, pos, entity);
        }
    }

    public static void tryWarpFromInside(ServerLevel level, BlockPos pos, Entity entity) {
        if (!entity.isShiftKeyDown() || entity.isOnPortalCooldown() || WARPED.contains(entity.getUUID())
                || entity.isPassenger() || entity.isVehicle()) return;
        if (!(level.getBlockEntity(pos) instanceof WarpPipeBlockEntity pipe) || pipe.getTarget() == null) {
            cancelCharge(entity);
            return;
        }
        GlobalPos origin = GlobalPos.of(level.dimension(), pos);
        Charge charge = CHARGES.get(entity.getUUID());
        if (charge == null || !charge.origin.equals(origin)) {
            charge = new Charge(origin, level.getGameTime());
            CHARGES.put(entity.getUUID(), charge);
            level.playSound(null, pos, ModSounds.PIPE_ENTER.get(), SoundSource.BLOCKS, 0.9F, 1.0F);
        }
        long elapsed = level.getGameTime() - charge.began;
        if (elapsed % 3 == 0) {
            level.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    8, 0.25, 0.15, 0.25, 0.08);
        }
        if (entity instanceof Player player) player.sendOverlayMessage(Component.translatable("message.hydemod.warp_pipe.charging")
                .withStyle(ChatFormatting.GREEN));
        if (elapsed >= CHARGE_TICKS) {
            CHARGES.remove(entity.getUUID());
            warp(level, pos, pipe, entity);
        }
    }

    private static void fail(ServerLevel level, BlockPos pos, Entity entity, String key) {
        WARPED.add(entity.getUUID()); // release sneak to retry; avoid sound/message spam
        level.playSound(null, pos, ModSounds.PIPE_FAIL.get(), SoundSource.BLOCKS, 0.7F, 1);
        if (entity instanceof Player player) player.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
    }

    private static void warp(ServerLevel level, BlockPos pos, WarpPipeBlockEntity pipe, Entity entity) {
        GlobalPos target = pipe.getTarget();
        if (target == null) return;
        ServerLevel destination = level.getServer().getLevel(target.dimension());
        if (destination == null) { fail(level, pos, entity, "message.hydemod.wrench.missing"); return; }
        BlockPos dest = target.pos();
        if (!destination.getWorldBorder().isWithinBounds(dest)) {
            fail(level, pos, entity, "message.hydemod.warp_pipe.blocked"); return;
        }
        destination.getChunk(dest); // a valid pipe can be outside currently loaded chunks
        if (!(destination.getBlockEntity(dest) instanceof WarpPipeBlockEntity exit)) {
            pipe.setTarget(null);
            fail(level, pos, entity, "message.hydemod.wrench.missing");
            return;
        }
        Vec3 landing = new Vec3(dest.getX() + 0.5, dest.getY() + 1.05, dest.getZ() + 0.5);
        var bounds = entity.getBoundingBox().move(landing.subtract(entity.position()));
        if (!destination.noCollision(entity, bounds) || destination.containsAnyLiquid(bounds)
                || dest.getY() + 1 + entity.getBbHeight() >= destination.getMaxY()) {
            fail(level, pos, entity, "message.hydemod.warp_pipe.blocked");
            return;
        }
        if (!entity.teleportTo(destination, landing.x, landing.y, landing.z, Set.of(), entity.getYRot(), 0, true)) {
            fail(level, pos, entity, "message.hydemod.warp_pipe.blocked"); return;
        }
        entity.setPortalCooldown(40);
        entity.fallDistance = 0;
        WARPED.add(entity.getUUID());
        entity.setDeltaMovement(0, exit.isLaunchExit() ? 0.65 : 0.12, 0);
        entity.hurtMarked = true;
        if (exit.isLaunchExit() && entity instanceof net.minecraft.world.entity.LivingEntity living) {
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 50, 0));
        }
        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 12, 0.3, 0.2, 0.3, 0.02);
        destination.playSound(null, dest, ModSounds.PIPE_EXIT.get(), SoundSource.BLOCKS, 1, 1);
        destination.sendParticles(ParticleTypes.FIREWORK, landing.x, landing.y + 0.3, landing.z, 24, 0.3, 0.25, 0.3, 0.08);
        if (entity instanceof Player player) player.sendOverlayMessage(Component.translatable("message.hydemod.warp_pipe.arrived").withStyle(ChatFormatting.AQUA));
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            var advancement = level.getServer().getAdvancements().get(com.hydemod.HydeMod.id("first_warp"));
            if (advancement != null) player.getAdvancements().award(advancement, "travel");
        }
    }
}
