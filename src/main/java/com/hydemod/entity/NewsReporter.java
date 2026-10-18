package com.hydemod.entity;

import com.hydemod.registry.ModEntities;
import com.hydemod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent, peaceful newsroom characters with original, context-aware satirical reports. */
public final class NewsReporter extends PathfinderMob {
    private int bulletin;
    private long nextReport;

    public NewsReporter(EntityType<? extends NewsReporter> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        getNavigation().setCanOpenDoors(true);
        getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.MOVEMENT_SPEED, .24)
                .add(Attributes.FOLLOW_RANGE, 20);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, .55));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public boolean isAnchor() { return getType() == ModEntities.CHRIS_MORRIS.get(); }
    public int bulletinIndex() { return bulletin; }

    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(level() instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (server.getGameTime() < nextReport) return InteractionResult.SUCCESS;
        nextReport = server.getGameTime() + 40;
        getLookControl().setLookAt(player, 30, 30);
        getNavigation().stop();
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.PAPER)) {
            held.consume(1, player);
            ItemStack report = new ItemStack(ModItems.UNVERIFIED_REPORT.get());
            if (!player.getInventory().add(report)) player.drop(report, false);
            player.sendSystemMessage(Component.translatable(isAnchor() ? "message.hydemod.news.editor" : "message.hydemod.news.copy"));
        } else if (held.is(ModItems.STRETCHED_TWIG_OF_PEACE.get())) {
            player.sendSystemMessage(Component.translatable("message.hydemod.news.twig_response"));
        } else {
            String role = isAnchor() ? "chris" : "peter";
            player.sendSystemMessage(Component.translatable("message.hydemod.news." + role + "." + bulletin)
                    .withStyle(isAnchor() ? ChatFormatting.AQUA : ChatFormatting.GOLD));
            bulletin = (bulletin + 1) % 4;
            if (!isAnchor()) {
                boolean editorNearby = !server.getEntitiesOfClass(NewsReporter.class, getBoundingBox().inflate(12),
                        reporter -> reporter.isAlive() && reporter.isAnchor()).isEmpty();
                if (editorNearby) player.sendSystemMessage(Component.translatable("message.hydemod.news.correction").withStyle(ChatFormatting.AQUA));
            }
        }
        playSound(SoundEvents.VILLAGER_YES, .4F, isAnchor() ? .8F : 1.1F);
        return InteractionResult.SUCCESS;
    }

    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("NewsBulletin", bulletin);
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        bulletin = Math.clamp(in.getIntOr("NewsBulletin", 0), 0, 3);
        nextReport = 0;
    }
}
