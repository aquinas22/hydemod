package com.hydemod.registry;

import com.hydemod.HydeMod;
import com.hydemod.item.BlackberryItem;
import com.hydemod.item.KyberColor;
import com.hydemod.item.LightsaberItem;
import com.hydemod.item.MaglevLauncherItem;
import com.hydemod.item.PatOnTheBackItem;
import com.hydemod.item.PipeWrenchItem;
import com.hydemod.item.QuoteItem;
import com.hydemod.item.ResetClockItem;
import com.hydemod.item.SeaCheesyBakedPotatoItem;
import com.hydemod.item.SummonBossItem;
import com.hydemod.item.TedMicrophoneItem;
import com.hydemod.item.TrashCubeItem;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HydeMod.MODID);

    // ---------------------------------------------------------------- 2070 Paradigm Shift
    public static final DeferredItem<TrashCubeItem> TRASH_CUBE = ITEMS.registerItem("trash_cube", TrashCubeItem::new, p -> p.stacksTo(64));
    public static final DeferredItem<Item> NEOPLAS_NUGGET = ITEMS.registerSimpleItem("neoplas_nugget");
    public static final DeferredItem<Item> NEOPLAS_INGOT = ITEMS.registerSimpleItem("neoplas_ingot");
    public static final DeferredItem<Item> PARADIGM_SHIFT_CORE = ITEMS.registerSimpleItem("paradigm_shift_core",
            p -> p.rarity(Rarity.EPIC).fireResistant().component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    public static final DeferredItem<Item> SEA_BEET = ITEMS.registerSimpleItem("sea_beet",
            p -> p.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.6F).build()));
    public static final DeferredItem<Item> SEA_YAM = ITEMS.registerSimpleItem("sea_yam",
            p -> p.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<Item> SEA_CABBAGE = ITEMS.registerSimpleItem("sea_cabbage",
            p -> p.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.5F).build()));
    public static final DeferredItem<Item> SEA_SALAD = ITEMS.registerSimpleItem("sea_salad",
            p -> p.stacksTo(1).usingConvertsTo(Items.BOWL)
                    .food(new FoodProperties.Builder().nutrition(10).saturationModifier(1.0F).build(),
                            Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(
                                    new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 120, 0))).build()));
    public static final DeferredItem<SeaCheesyBakedPotatoItem> SEA_CHEESY_BAKED_POTATO = ITEMS.registerItem("sea_cheesy_baked_potato", SeaCheesyBakedPotatoItem::new,
            p -> p.food(new FoodProperties.Builder().nutrition(12).saturationModifier(1.2F).build(),
                    Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                            new MobEffectInstance(MobEffects.SPEED, 20 * 45, 2),
                            new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 45, 1)))).build()));
    public static final DeferredItem<Item> BULLETPROOF_COFFEE = ITEMS.registerSimpleItem("bulletproof_coffee",
            p -> p.stacksTo(16).usingConvertsTo(Items.GLASS_BOTTLE)
                    .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).alwaysEdible().build(),
                            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                                    new MobEffectInstance(MobEffects.SPEED, 20 * 90, 1),
                                    new MobEffectInstance(MobEffects.HASTE, 20 * 90, 1),
                                    new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 90, 0),
                                    new MobEffectInstance(MobEffects.NAUSEA, 20 * 6, 0)))).build()));
    public static final DeferredItem<Item> SODA_STREAM = ITEMS.registerItem("soda_stream", Item::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> SODA = ITEMS.registerSimpleItem("soda",
            p -> p.stacksTo(16).usingConvertsTo(Items.GLASS_BOTTLE)
                    .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().build(),
                            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                                    new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 60, 2),
                                    new MobEffectInstance(MobEffects.SPEED, 20 * 30, 0)))).build()));

    public static final DeferredItem<TedMicrophoneItem> TED_MICROPHONE = ITEMS.registerItem("ted_microphone", TedMicrophoneItem::new,
            p -> p.stacksTo(1).rarity(Rarity.RARE).durability(500).sword(net.minecraft.world.item.ToolMaterial.IRON, 2.0F, -2.2F));
    public static final DeferredItem<PatOnTheBackItem> PAT_ON_THE_BACK = ITEMS.registerItem("pat_on_the_back", PatOnTheBackItem::new,
            p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BlackberryItem> MIND_UPLOAD_BLACKBERRY = ITEMS.registerItem("mind_upload_blackberry", BlackberryItem::new,
            p -> p.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<ResetClockItem> RESET_THE_CLOCK = ITEMS.registerItem("reset_the_clock", ResetClockItem::new,
            p -> p.stacksTo(1).durability(16).rarity(Rarity.RARE));
    public static final DeferredItem<MaglevLauncherItem> MAGLEV_MOON_LAUNCHER = ITEMS.registerItem("maglev_moon_launcher", MaglevLauncherItem::new,
            p -> p.stacksTo(1).durability(128).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<QuoteItem> EINSTEIN_PARABLE = ITEMS.registerItem("einstein_parable", p -> new QuoteItem(p, "einstein"),
            p -> p.stacksTo(1));
    public static final DeferredItem<SummonBossItem> TEDX_INVITATION = ITEMS.registerItem("tedx_invitation",
            p -> new SummonBossItem(p, ModEntities.SAM_HYDE::get, "message.hydemod.sam_hyde_summoned"), p -> p.stacksTo(1).rarity(Rarity.EPIC));

    // ---------------------------------------------------------------- Elephant Mario
    public static final DeferredItem<Item> PEANUT = ITEMS.registerSimpleItem("peanut",
            p -> p.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build()));
    /** Goobers: chocolate-covered peanuts. Elephant Mario's favourite candy. */
    public static final DeferredItem<Item> GOOBERS = ITEMS.registerSimpleItem("goobers",
            p -> p.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).alwaysEdible().build(),
                    Consumables.defaultFood().consumeSeconds(0.8F).onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                            new MobEffectInstance(MobEffects.SPEED, 20 * 20, 0),
                            new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 20, 0)))).build()));
    /** Summons Elephant Mario: he can smell a box this big from miles away. */
    public static final DeferredItem<SummonBossItem> GIANT_GOOBERS_BOX = ITEMS.registerItem("giant_goobers_box",
            p -> new SummonBossItem(p, ModEntities.ELEPHANT_MARIO::get, "message.hydemod.elephant_mario_summoned"), p -> p.stacksTo(1).rarity(Rarity.EPIC));
    /** Dropped by Elephant Mario: one hour of Strength II and Speed II. */
    public static final DeferredItem<Item> ELEPHANT_FRUIT = ITEMS.registerSimpleItem("elephant_fruit",
            p -> p.rarity(Rarity.EPIC).component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).alwaysEdible().build(),
                            Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                                    new MobEffectInstance(MobEffects.STRENGTH, 20 * 60 * 60, 1),
                                    new MobEffectInstance(MobEffects.SPEED, 20 * 60 * 60, 1)))).build()));
    public static final DeferredItem<Item> SUPER_PEANUT = ITEMS.registerSimpleItem("super_peanut",
            p -> p.rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).alwaysEdible().build(),
                    Consumables.defaultFood().sound(SoundEvents.GENERIC_EAT).onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                            new MobEffectInstance(MobEffects.STRENGTH, 20 * 60, 1),
                            new MobEffectInstance(MobEffects.RESISTANCE, 20 * 60, 0),
                            new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 60, 1),
                            new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 60, 2)))).build()));
    public static final DeferredItem<PipeWrenchItem> PIPE_WRENCH = ITEMS.registerItem("pipe_wrench", PipeWrenchItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<BlockItem> WARP_PIPE = ITEMS.registerSimpleBlockItem("warp_pipe", ModBlocks.WARP_PIPE);

    // ---------------------------------------------------------------- Hyde's Armour
    public static final TagKey<net.minecraft.world.item.Item> REPAIRS_HYDE_ARMOUR = TagKey.create(Registries.ITEM, HydeMod.id("repairs_hyde_armour"));
    public static final ResourceKey<EquipmentAsset> HYDE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, HydeMod.id("hyde"));
    public static final ArmorMaterial HYDE_MATERIAL = new ArmorMaterial(40, defense(4, 7, 9, 4, 12), 20, SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.5F, 0.15F, REPAIRS_HYDE_ARMOUR, HYDE_ASSET);
    public static final DeferredItem<Item> HYDE_HELMET = ITEMS.registerSimpleItem("hyde_helmet",
            p -> p.humanoidArmor(HYDE_MATERIAL, ArmorType.HELMET).fireResistant().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> HYDE_CHESTPLATE = ITEMS.registerSimpleItem("hyde_chestplate",
            p -> p.humanoidArmor(HYDE_MATERIAL, ArmorType.CHESTPLATE).fireResistant().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> HYDE_LEGGINGS = ITEMS.registerSimpleItem("hyde_leggings",
            p -> p.humanoidArmor(HYDE_MATERIAL, ArmorType.LEGGINGS).fireResistant().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> HYDE_BOOTS = ITEMS.registerSimpleItem("hyde_boots",
            p -> p.humanoidArmor(HYDE_MATERIAL, ArmorType.BOOTS).fireResistant().rarity(Rarity.EPIC));

    // ---------------------------------------------------------------- Lightsabers & kyber crystals
    public static final DeferredItem<Item> GREEN_KYBER_CRYSTAL = ITEMS.registerSimpleItem("green_kyber_crystal", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> BLUE_KYBER_CRYSTAL = ITEMS.registerSimpleItem("blue_kyber_crystal", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RED_KYBER_CRYSTAL = ITEMS.registerSimpleItem("red_kyber_crystal", p -> p.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<Item> PURPLE_KYBER_CRYSTAL = ITEMS.registerSimpleItem("purple_kyber_crystal", p -> p.rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> LIGHTSABER_HILT = ITEMS.registerSimpleItem("lightsaber_hilt", p -> p.stacksTo(1));
    public static final DeferredItem<LightsaberItem> GREEN_LIGHTSABER = lightsaber(KyberColor.GREEN);
    public static final DeferredItem<LightsaberItem> BLUE_LIGHTSABER = lightsaber(KyberColor.BLUE);
    public static final DeferredItem<LightsaberItem> RED_LIGHTSABER = lightsaber(KyberColor.RED);
    public static final DeferredItem<LightsaberItem> PURPLE_LIGHTSABER = lightsaber(KyberColor.PURPLE);

    // ---------------------------------------------------------------- Block items
    public static final DeferredItem<BlockItem> GREEN_KYBER_ORE = ITEMS.registerSimpleBlockItem("green_kyber_ore", ModBlocks.GREEN_KYBER_ORE);
    public static final DeferredItem<BlockItem> BLUE_KYBER_ORE = ITEMS.registerSimpleBlockItem("deepslate_blue_kyber_ore", ModBlocks.BLUE_KYBER_ORE);
    public static final DeferredItem<BlockItem> RED_KYBER_ORE = ITEMS.registerSimpleBlockItem("nether_red_kyber_ore", ModBlocks.RED_KYBER_ORE);
    public static final DeferredItem<BlockItem> PURPLE_KYBER_ORE = ITEMS.registerSimpleBlockItem("end_purple_kyber_ore", ModBlocks.PURPLE_KYBER_ORE);
    public static final DeferredItem<BlockItem> NEOPLAS_BLOCK = ITEMS.registerSimpleBlockItem("neoplas_block", ModBlocks.NEOPLAS_BLOCK);
    public static final DeferredItem<BlockItem> TRASH_CUBE_BLOCK = ITEMS.registerSimpleBlockItem("trash_cube_block", ModBlocks.TRASH_CUBE_BLOCK);
    public static final DeferredItem<BlockItem> CHARGING_STATION = ITEMS.registerSimpleBlockItem("charging_station", ModBlocks.CHARGING_STATION);

    // ---------------------------------------------------------------- Spawn eggs
    public static final DeferredItem<SpawnEggItem> SAM_HYDE_SPAWN_EGG = ITEMS.registerItem("sam_hyde_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(ModEntities.SAM_HYDE.get()));
    public static final DeferredItem<SpawnEggItem> ELEPHANT_MARIO_SPAWN_EGG = ITEMS.registerItem("elephant_mario_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(ModEntities.ELEPHANT_MARIO.get()));
    public static final DeferredItem<SpawnEggItem> DEPTH_GROVLER_SPAWN_EGG = ITEMS.registerItem("depth_grovler_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(ModEntities.DEPTH_GROVLER.get()));

    private static DeferredItem<LightsaberItem> lightsaber(KyberColor color) {
        return ITEMS.registerItem(color.getSerializedName() + "_lightsaber", p -> new LightsaberItem(p, color),
                p -> LightsaberItem.properties(p, color));
    }

    private static Map<ArmorType, Integer> defense(int boots, int legs, int chest, int helm, int body) {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.LEGGINGS, legs);
        map.put(ArmorType.CHESTPLATE, chest);
        map.put(ArmorType.HELMET, helm);
        map.put(ArmorType.BODY, body);
        return map;
    }

    private ModItems() {}
}
