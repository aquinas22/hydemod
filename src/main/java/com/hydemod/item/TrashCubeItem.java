package com.hydemod.item;

import com.hydemod.entity.ThrownTrashCube;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Trash economy currency. Also makes a fine projectile. */
public class TrashCubeItem extends Item {
    public TrashCubeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.6F);
        if (level instanceof ServerLevel serverLevel) {
            ThrownTrashCube cube = new ThrownTrashCube(serverLevel, player, stack.copyWithCount(1));
            cube.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.3F, 1.0F);
            serverLevel.addFreshEntity(cube);
        }
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
