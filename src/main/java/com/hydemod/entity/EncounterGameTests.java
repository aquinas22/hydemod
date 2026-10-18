package com.hydemod.entity;

import com.hydemod.HydeMod;
import com.hydemod.block.WarpPipeBlock;
import com.hydemod.block.WarpPipeBlockEntity;
import com.hydemod.item.LightsaberItem;
import com.hydemod.registry.ModBlocks;
import com.hydemod.registry.ModComponents;
import com.hydemod.registry.ModEntities;
import com.hydemod.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Integration regressions, registered only by NeoForge's development GameTest runner. */
@EventBusSubscriber(modid = HydeMod.MODID)
public final class EncounterGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, HydeMod.MODID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SABER = FUNCTIONS.register("permanent_saber", () -> EncounterGameTests::permanentSaber);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HEAL = FUNCTIONS.register("interrupt_reset", () -> EncounterGameTests::interruptReset);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RESET = FUNCTIONS.register("reset_once", () -> EncounterGameTests::resetOnce);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BLOCKED = FUNCTIONS.register("blocked_pipe", () -> h -> pipeTravel(h, true));
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CLEAR = FUNCTIONS.register("clear_pipe", () -> h -> pipeTravel(h, false));
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CANCEL = FUNCTIONS.register("cancel_pipe", () -> EncounterGameTests::cancelPipe);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SAVE_RESET = FUNCTIONS.register("saved_reset", () -> EncounterGameTests::savedReset);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SAVE_PIPE = FUNCTIONS.register("saved_pipe", () -> EncounterGameTests::savedPipe);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TAB = FUNCTIONS.register("creative_tab", () -> EncounterGameTests::creativeTab);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEWS = FUNCTIONS.register("reporter_save_and_paper", () -> EncounterGameTests::reporterSaveAndPaper);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TWIG = FUNCTIONS.register("peace_twig_and_fact_bomb", () -> EncounterGameTests::peaceTwigAndFactBomb);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PRESS = FUNCTIONS.register("press_pass_clear_and_blocked", () -> EncounterGameTests::pressPassClearAndBlocked);

    @SubscribeEvent
    static void register(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(HydeMod.id("encounter_tests"));
        for (var function : java.util.List.of(SABER, HEAL, RESET, BLOCKED, CLEAR, CANCEL, SAVE_RESET, SAVE_PIPE, TAB, NEWS, TWIG, PRESS)) {
            event.registerTest(function.getId(), new FunctionGameTestInstance(function.getKey(),
                    new TestData<>(environment, HydeMod.id("test_empty"), 50, 0, true)));
        }
    }

    private static void permanentSaber(GameTestHelper h) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        for (var item : java.util.List.of(ModItems.GREEN_LIGHTSABER.get(), ModItems.BLUE_LIGHTSABER.get(),
                ModItems.RED_LIGHTSABER.get(), ModItems.PURPLE_LIGHTSABER.get())) {
            ItemStack stack = new ItemStack(item);
            h.assertValueEqual(stack.get(ModComponents.ENERGY.get()), LightsaberItem.CAPACITY, "new saber charge");
            stack.set(ModComponents.ENERGY.get(), 0); // a saved, depleted stack from the previous release
            stack.set(ModComponents.IGNITED.get(), net.minecraft.util.Unit.INSTANCE);
            player.setItemSlot(EquipmentSlot.MAINHAND, stack);
            LightsaberItem saber = (LightsaberItem) item;
            saber.inventoryTick(stack, h.getLevel(), player, EquipmentSlot.MAINHAND);
            h.assertTrue(LightsaberItem.isActive(stack), "An old depleted saber must ignite");
            var target = h.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
            for (int i = 0; i < 50; i++) saber.hurtEnemy(stack, target, player);
            h.assertValueEqual(stack.get(ModComponents.ENERGY.get()), LightsaberItem.CAPACITY, "charge after fifty hits");
            saber.inventoryTick(stack, h.getLevel(), player, null);
            h.assertTrue(!LightsaberItem.isIgnited(stack), "Holstering must retract the blade");
        }
        h.succeed();
    }

    private static SamHyde setupSam(GameTestHelper h) {
        SamHyde sam = h.spawn(ModEntities.SAM_HYDE.get(), new BlockPos(3, 2, 3));
        sam.setNoAi(true);
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(sam.position().add(0, 0, 4));
        sam.setTarget(player);
        sam.setHealth(60);
        return sam;
    }

    private static void interruptReset(GameTestHelper h) {
        SamHyde sam = setupSam(h);
        sam.bossTick(h.getLevel()); // under 20% health starts his one reset cast
        h.assertValueEqual(sam.getAnimId(), SamHyde.ANIM_RESET, "reset cast started");
        sam.hurtServer(h.getLevel(), sam.damageSources().playerAttack((Player) sam.getTarget()), 20);
        float afterHit = sam.getHealth();
        for (int i = 0; i < 65; i++) sam.bossTick(h.getLevel());
        h.assertTrue(sam.getHealth() <= afterHit, "Interrupting the cast must stop healing");
        h.succeed();
    }

    private static void resetOnce(GameTestHelper h) {
        SamHyde sam = setupSam(h);
        sam.bossTick(h.getLevel());
        for (int i = 0; i < 60; i++) sam.bossTick(h.getLevel());
        h.assertValueEqual(sam.getHealth(), 105.0F, "completed reset healing");
        sam.setHealth(60);
        for (int i = 0; i < 36; i++) sam.bossTick(h.getLevel());
        h.assertValueEqual(sam.getHealth(), 60.0F, "reset cannot trigger a second time");
        h.succeed();
    }

    private static WarpPipeBlockEntity pipe(GameTestHelper h, BlockPos relative) {
        h.setBlock(relative, ModBlocks.WARP_PIPE.get());
        return (WarpPipeBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(relative));
    }

    private static void pipeTravel(GameTestHelper h, boolean blocked) {
        BlockPos first = new BlockPos(2, 1, 2), second = new BlockPos(10, 1, 2);
        var a = pipe(h, first);
        var b = pipe(h, second);
        a.setTarget(GlobalPos.of(h.getLevel().dimension(), h.absolutePos(second)));
        b.setTarget(GlobalPos.of(h.getLevel().dimension(), h.absolutePos(first)));
        if (blocked) h.setBlock(second.above(), Blocks.STONE);
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        Vec3 origin = Vec3.atBottomCenterOf(h.absolutePos(first)).add(0, 1, 0);
        player.setPos(origin);
        player.setShiftKeyDown(true);
        WarpPipeBlock.tryWarpFromInside(h.getLevel(), h.absolutePos(first), player);
        h.runAtTickTime(15, () -> {
            WarpPipeBlock.tryWarpFromInside(h.getLevel(), h.absolutePos(first), player);
            if (blocked) {
                h.assertTrue(player.position().distanceTo(origin) < 0.1, "Blocked exits must keep the player at the source");
                h.assertTrue(a.getTarget() != null, "An obstruction must preserve the route");
            } else {
                h.assertTrue(player.position().distanceTo(Vec3.atBottomCenterOf(h.absolutePos(second)).add(0, 1.05, 0)) < 0.1,
                        "The player must emerge above the clear exit");
                h.assertTrue(player.getDeltaMovement().y > 0.5, "Launch mode must give upward motion");
                h.assertTrue(player.isOnPortalCooldown(), "Warp travel must prevent immediate bounce-back");
            }
            WarpPipeBlock.clearWarped(player);
            h.succeed();
        });
    }

    private static void cancelPipe(GameTestHelper h) {
        BlockPos first = new BlockPos(2, 1, 2), second = new BlockPos(10, 1, 2);
        var a = pipe(h, first); pipe(h, second);
        a.setTarget(GlobalPos.of(h.getLevel().dimension(), h.absolutePos(second)));
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        Vec3 origin = Vec3.atBottomCenterOf(h.absolutePos(first)).add(0, 1, 0);
        player.setPos(origin); player.setShiftKeyDown(true);
        WarpPipeBlock.tryWarpFromInside(h.getLevel(), h.absolutePos(first), player);
        WarpPipeBlock.clearWarped(player); // release sneak before the charge completes
        player.setShiftKeyDown(false);
        h.runAtTickTime(15, () -> {
            WarpPipeBlock.tryWarpFromInside(h.getLevel(), h.absolutePos(first), player);
            h.assertTrue(player.position().distanceTo(origin) < 0.1, "Canceling a charge must leave the player at the source");
            h.succeed();
        });
    }

    private static void savedReset(GameTestHelper h) {
        SamHyde sam = setupSam(h);
        sam.bossTick(h.getLevel());
        for (int i = 0; i < 60; i++) sam.bossTick(h.getLevel());
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        sam.addAdditionalSaveData(output);
        SamHyde loaded = setupSam(h);
        loaded.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
        loaded.setHealth(60);
        for (int i = 0; i < 60; i++) loaded.bossTick(h.getLevel());
        h.assertValueEqual(loaded.getHealth(), 60.0F, "Saving and reloading must not grant a second clock reset");
        h.succeed();
    }

    private static void savedPipe(GameTestHelper h) {
        BlockPos first = new BlockPos(2, 1, 2);
        var original = pipe(h, first);
        var target = GlobalPos.of(h.getLevel().dimension(), h.absolutePos(new BlockPos(10, 1, 2)));
        original.setTarget(target);
        original.setLaunchExit(false);
        var tag = original.saveWithoutMetadata(h.getLevel().registryAccess());
        var loaded = new WarpPipeBlockEntity(h.absolutePos(first), ModBlocks.WARP_PIPE.get().defaultBlockState());
        loaded.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess(), tag));
        h.assertValueEqual(loaded.getTarget(), target, "pipe destination after reload");
        h.assertTrue(!loaded.isLaunchExit(), "Gentle arrival mode must survive reload");
        h.succeed();
    }

    private static void creativeTab(GameTestHelper h) {
        var tab = com.hydemod.registry.ModTabs.HYDEMOD.get();
        tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(
                h.getLevel().enabledFeatures(), true, h.getLevel().registryAccess()));
        for (var item : java.util.List.of(ModItems.GREEN_LIGHTSABER.get(), ModItems.BLUE_LIGHTSABER.get(),
                ModItems.RED_LIGHTSABER.get(), ModItems.PURPLE_LIGHTSABER.get())) {
            long count = tab.getDisplayItems().stream().filter(stack -> stack.is(item)).count();
            h.assertValueEqual(count, 1L, "Each lightsaber must appear exactly once in creative and JEI");
        }
        h.assertTrue(tab.getDisplayItems().size() >= ModItems.ITEMS.getEntries().size(), "The creative tab must contain every mod item");
        h.succeed();
    }

    private static void reporterSaveAndPaper(GameTestHelper h) {
        NewsReporter peter = h.spawn(ModEntities.PETER_OHANRAHAN.get(), new BlockPos(3, 2, 3));
        NewsReporter chris = h.spawn(ModEntities.CHRIS_MORRIS.get(), new BlockPos(5, 2, 3));
        h.assertTrue(!peter.isAnchor() && chris.isAnchor(), "Correspondents must keep their own roles");
        h.assertTrue(peter.isPersistenceRequired(), "A summoned reporter must persist");
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        peter.mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertValueEqual(peter.bulletinIndex(), 1, "Speaking must advance the report");
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        peter.addAdditionalSaveData(output);
        NewsReporter loaded = ModEntities.PETER_OHANRAHAN.get().create(h.getLevel(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        loaded.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
        h.assertValueEqual(loaded.bulletinIndex(), 1, "Bulletin progress must survive saving");
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.PAPER, 2));
        loaded.mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertValueEqual(player.getMainHandItem().getCount(), 1, "A field report costs one paper");
        h.assertTrue(player.getInventory().contains(new ItemStack(ModItems.UNVERIFIED_REPORT.get())), "The player must receive a report");
        h.succeed();
    }

    private static void peaceTwigAndFactBomb(GameTestHelper h) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(3, 2, 3))));
        var zombie = h.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 3));
        var pig = h.spawn(EntityTypes.PIG, new BlockPos(5, 2, 5));
        zombie.setNoAi(true);
        ItemStack twig = new ItemStack(ModItems.STRETCHED_TWIG_OF_PEACE.get());
        player.setItemSlot(EquipmentSlot.MAINHAND, twig);
        ModItems.STRETCHED_TWIG_OF_PEACE.get().use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(zombie.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "The twig weakens a nearby hostile");
        h.assertTrue(zombie.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "The twig slows a nearby hostile");
        h.assertTrue(!pig.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "Peaceful mobs must be unaffected");
        h.assertValueEqual(twig.getDamageValue(), 1, "Overworld use spends one durability");
        h.assertTrue(player.getCooldowns().isOnCooldown(twig), "Peace use needs a cooldown");
        ItemStack bombs = new ItemStack(ModItems.FACT_BOMB.get(), 2);
        player.setItemSlot(EquipmentSlot.MAINHAND, bombs);
        float health = zombie.getHealth();
        ModItems.FACT_BOMB.get().use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(zombie.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING), "The camera prop reveals hostiles");
        h.assertValueEqual(zombie.getHealth(), health, "The prop must not cause blast damage");
        h.assertValueEqual(bombs.getCount(), 1, "The flash consumes one prop");
        h.succeed();
    }

    private static void pressPassClearAndBlocked(GameTestHelper h) {
        Player player = h.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pass = new ItemStack(ModItems.PRESS_PASS.get());
        player.setItemSlot(EquipmentSlot.MAINHAND, pass);
        BlockPos ground = new BlockPos(5, 1, 5);
        h.setBlock(ground, Blocks.STONE);
        h.setBlock(ground.above(), Blocks.STONE);
        var hit = new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(h.absolutePos(ground)),
                net.minecraft.core.Direction.UP, h.absolutePos(ground), false);
        var context = new net.minecraft.world.item.context.UseOnContext(h.getLevel(), player,
                net.minecraft.world.InteractionHand.MAIN_HAND, pass, hit);
        var result = ModItems.PRESS_PASS.get().useOn(context);
        h.assertTrue(result == net.minecraft.world.InteractionResult.FAIL, "A blocked booth must reject the summon");
        h.assertValueEqual(pass.getCount(), 1, "An obstruction must preserve the pass");
        h.setBlock(ground.above(), Blocks.AIR);
        h.setBlock(ground.above(2), Blocks.AIR);
        result = ModItems.PRESS_PASS.get().useOn(context);
        h.assertTrue(result == net.minecraft.world.InteractionResult.SUCCESS, "Clear ground must summon Peter");
        h.assertEntityPresent(ModEntities.PETER_OHANRAHAN.get(), ground.above());
        h.assertValueEqual(pass.getCount(), 1, "The pass remains reusable after success");
        result = ModItems.PRESS_PASS.get().useOn(context);
        h.assertTrue(result == net.minecraft.world.InteractionResult.FAIL, "Survival must not summon duplicate reporters nearby");
        h.succeed();
    }
}
