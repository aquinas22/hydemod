package com.hydemod.item.realm;

import com.hydemod.item.HydeTooltips;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Stone-Spell Wand (the Winter Witch's wand): turns a creature to stone for a few seconds and
 * freezes the water around it into ice.
 */
public class TargetSpellItem extends Item {
    public TargetSpellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        LivingEntity target = RayUtil.entityRay(player, 24.0);
        Vec3 from = RayUtil.handPos(player);
        Vec3 to = target != null ? target.position().add(0, target.getBbHeight() * 0.5, 0) : from.add(player.getLookAngle().scale(24));
        RayUtil.trail(server, from, to, ParticleTypes.SNOWFLAKE);
        level.playSound(null, player.blockPosition(), SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 1.5F, 0.6F);
        if (target != null) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 6, 9, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 6, 4, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 6, 3, false, true));
            target.setTicksFrozen(target.getTicksRequiredToFreeze() + 120);
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    target.getX(), target.getY(0.5), target.getZ(), 40, 0.3, 0.6, 0.3, 0.1);
            level.playSound(null, target.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.5F, 0.6F);
            BlockPos c = target.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -2, -3), c.offset(3, 1, 3))) {
                if (level.getBlockState(p).is(Blocks.WATER) && level.getFluidState(p).isSource()) {
                    level.setBlockAndUpdate(p, Blocks.ICE.defaultBlockState());
                }
            }
        }
        player.getCooldowns().addCooldown(stack, 30);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HydeTooltips.add(stack, builder);
    }
}
