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
import com.hydemod.registry.ModEffects;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HydeMod.MODID);

    // ---------------------------------------------------------------- The Day Today newsroom
    public static final DeferredItem<com.hydemod.item.PressPassItem> PRESS_PASS = ITEMS.registerItem("day_today_press_pass",
            com.hydemod.item.PressPassItem::new, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.hydemod.item.NewsPropItem> STRETCHED_TWIG_OF_PEACE = newsProp("stretched_twig_of_peace",
            com.hydemod.item.NewsPropItem.Kind.TWIG, p -> p.durability(160).rarity(Rarity.RARE));
    public static final DeferredItem<com.hydemod.item.NewsPropItem> SATELLITE_INFOMOMETER = newsProp("satellite_infomometer",
            com.hydemod.item.NewsPropItem.Kind.INFOMOMETER, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.hydemod.item.NewsPropItem> NEWS_MICROPHONE = newsProp("news_microphone",
            com.hydemod.item.NewsPropItem.Kind.MICROPHONE, p -> p.stacksTo(1));
    public static final DeferredItem<com.hydemod.item.NewsPropItem> FACT_BOMB = newsProp("fact_bomb",
            com.hydemod.item.NewsPropItem.Kind.FACT_BOMB, p -> p.stacksTo(16));
    public static final DeferredItem<com.hydemod.item.NewsPropItem> THIS_IS_OUR_WAR = newsProp("this_is_our_war",
            com.hydemod.item.NewsPropItem.Kind.ARCHIVE, p -> p.stacksTo(1));
    public static final DeferredItem<com.hydemod.item.NewsPropItem> UNVERIFIED_REPORT = newsProp("unverified_report",
            com.hydemod.item.NewsPropItem.Kind.REPORT, p -> p.stacksTo(16));
    public static final DeferredItem<SpawnEggItem> PETER_OHANRAHAN_SPAWN_EGG = ITEMS.registerItem("peter_ohanrahan_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.PETER_OHANRAHAN.get()));
    public static final DeferredItem<SpawnEggItem> CHRIS_MORRIS_SPAWN_EGG = ITEMS.registerItem("chris_morris_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.CHRIS_MORRIS.get()));

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

    // ---------------------------------------------------------------- Realm items (homages: wardrobe-winter, wizard school, plumber, hero of time, ring quest)
    public static final DeferredItem<Item> TURKISH_DELIGHT = ITEMS.registerSimpleItem("turkish_delight",
            p -> p.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.2F).alwaysEdible().build(),
                    Consumables.defaultFood().consumeSeconds(0.6F).onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                            new MobEffectInstance(MobEffects.SPEED, 20 * 15, 1),
                            new MobEffectInstance(MobEffects.HUNGER, 20 * 25, 1)))).build()));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.HealingCordial> HEALING_CORDIAL = ITEMS.registerItem("healing_cordial",
            com.hydemod.item.realm.UtilityItems.HealingCordial::new, p -> p.stacksTo(1).durability(8).rarity(Rarity.RARE));
    public static final DeferredItem<com.hydemod.item.realm.TargetSpellItem> STONE_SPELL_WAND = ITEMS.registerItem("stone_spell_wand",
            com.hydemod.item.realm.TargetSpellItem::new, p -> p.stacksTo(1).durability(64).rarity(Rarity.RARE));
    public static final DeferredItem<com.hydemod.item.realm.WizardWandItem> WIZARD_WAND = ITEMS.registerItem("wizard_wand",
            com.hydemod.item.realm.WizardWandItem::new, p -> p.stacksTo(1).durability(250).rarity(Rarity.RARE));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.InvisibilityCloak> INVISIBILITY_CLOAK = ITEMS.registerItem("invisibility_cloak",
            com.hydemod.item.realm.UtilityItems.InvisibilityCloak::new, p -> p.stacksTo(1).durability(32).rarity(Rarity.EPIC));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.FlyingBroom> FLYING_BROOM = ITEMS.registerItem("flying_broom",
            com.hydemod.item.realm.UtilityItems.FlyingBroom::new, p -> p.stacksTo(1).durability(600).rarity(Rarity.RARE));
    public static final DeferredItem<Item> BUTTERSCOTCH_BREW = ITEMS.registerSimpleItem("butterscotch_brew",
            p -> p.stacksTo(16).usingConvertsTo(Items.GLASS_BOTTLE)
                    .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).alwaysEdible().build(),
                            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                                    new MobEffectInstance(MobEffects.ABSORPTION, 20 * 120, 1),
                                    new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 0),
                                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60, 0)))).build()));
    public static final DeferredItem<Item> POWER_MUSHROOM = ITEMS.registerSimpleItem("power_mushroom",
            p -> p.rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.8F).alwaysEdible().build(),
                    Consumables.defaultFood().consumeSeconds(0.6F).onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                            new MobEffectInstance(ModEffects.SUPER_SIZE, 20 * 60, 0),
                            new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 1)))).build()));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.BlazeBlossom> BLAZE_BLOSSOM = ITEMS.registerItem("blaze_blossom",
            com.hydemod.item.realm.UtilityItems.BlazeBlossom::new, p -> p.stacksTo(1).durability(64).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> INVINCIBILITY_STAR = ITEMS.registerSimpleItem("invincibility_star",
            p -> p.rarity(Rarity.EPIC).component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F).alwaysEdible().build(),
                            Consumables.defaultFood().consumeSeconds(0.4F).sound(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMETHYST_BLOCK_CHIME)).onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                                    new MobEffectInstance(ModEffects.STAR_POWER, 20 * 12, 0),
                                    new MobEffectInstance(MobEffects.RESISTANCE, 20 * 12, 4),
                                    new MobEffectInstance(MobEffects.SPEED, 20 * 12, 2),
                                    new MobEffectInstance(MobEffects.GLOWING, 20 * 12, 0)))).build()));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.HeartVessel> HEART_VESSEL = ITEMS.registerItem("heart_vessel",
            com.hydemod.item.realm.UtilityItems.HeartVessel::new, p -> p.stacksTo(16).rarity(Rarity.EPIC));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.GrapplingHook> GRAPPLING_HOOK = ITEMS.registerItem("grappling_hook",
            com.hydemod.item.realm.UtilityItems.GrapplingHook::new, p -> p.stacksTo(1).durability(200).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.Bomb> BOMB = ITEMS.registerItem("bomb",
            com.hydemod.item.realm.UtilityItems.Bomb::new, p -> p.stacksTo(16));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.Ocarina> OCARINA = ITEMS.registerItem("ocarina",
            com.hydemod.item.realm.UtilityItems.Ocarina::new, p -> p.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.RingOfUnseeing> RING_OF_UNSEEING = ITEMS.registerItem("ring_of_unseeing",
            com.hydemod.item.realm.UtilityItems.RingOfUnseeing::new, p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.ElvenBlade> ELVEN_BLADE = ITEMS.registerItem("elven_blade",
            com.hydemod.item.realm.UtilityItems.ElvenBlade::new, p -> p.sword(net.minecraft.world.item.ToolMaterial.DIAMOND, 3.0F, -2.2F).rarity(Rarity.RARE));
    public static final ResourceKey<EquipmentAsset> MITHRIL_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, HydeMod.id("mithril"));
    public static final TagKey<Item> REPAIRS_MITHRIL = TagKey.create(Registries.ITEM, HydeMod.id("repairs_mithril"));
    public static final ArmorMaterial MITHRIL_MATERIAL = new ArmorMaterial(45, defense(3, 6, 9, 3, 11), 25, SoundEvents.ARMOR_EQUIP_CHAIN,
            3.0F, 0.0F, REPAIRS_MITHRIL, MITHRIL_ASSET);
    public static final DeferredItem<Item> MITHRIL_MAIL = ITEMS.registerSimpleItem("mithril_mail",
            p -> p.humanoidArmor(MITHRIL_MATERIAL, ArmorType.CHESTPLATE).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ELVEN_WAYBREAD = ITEMS.registerSimpleItem("elven_waybread",
            p -> p.rarity(Rarity.UNCOMMON).food(new FoodProperties.Builder().nutrition(20).saturationModifier(1.0F).build(),
                    Consumables.defaultFood().consumeSeconds(0.8F).build()));
    public static final DeferredItem<com.hydemod.item.realm.UtilityItems.StarPhial> STAR_PHIAL = ITEMS.registerItem("star_phial",
            com.hydemod.item.realm.UtilityItems.StarPhial::new, p -> p.stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<com.hydemod.item.realm.RetroItems.SluggerBat> SLUGGER_BAT = ITEMS.registerItem("slugger_bat",
            com.hydemod.item.realm.RetroItems.SluggerBat::new, p -> p.sword(net.minecraft.world.item.ToolMaterial.IRON, 4.0F, -2.8F)
                    .attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                            .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 6.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                            .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED, new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.8, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                            .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK, new net.minecraft.world.entity.ai.attributes.AttributeModifier(HydeMod.id("slugger_knockback"), 2.5, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                            .build()).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.hydemod.item.realm.RetroItems.ReflectorBadge> REFLECTOR_BADGE = ITEMS.registerItem("reflector_badge",
            com.hydemod.item.realm.RetroItems.ReflectorBadge::new, p -> p.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<com.hydemod.item.realm.RetroItems.HealingButterfly> HEALING_BUTTERFLY = ITEMS.registerItem("healing_butterfly",
            com.hydemod.item.realm.RetroItems.HealingButterfly::new, p -> p.stacksTo(16).rarity(Rarity.RARE));
    public static final DeferredItem<com.hydemod.item.realm.RetroItems.SpinHammer> SPIN_HAMMER = ITEMS.registerItem("spin_hammer",
            com.hydemod.item.realm.RetroItems.SpinHammer::new, p -> p.stacksTo(1).durability(300).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.hydemod.item.realm.RetroItems.FlipCharm> FLIP_CHARM = ITEMS.registerItem("flip_charm",
            com.hydemod.item.realm.RetroItems.FlipCharm::new, p -> p.stacksTo(1).durability(100).rarity(Rarity.RARE));
    public static final DeferredItem<com.hydemod.item.realm.RetroItems.MomentStopwatch> MOMENT_STOPWATCH = ITEMS.registerItem("moment_stopwatch",
            com.hydemod.item.realm.RetroItems.MomentStopwatch::new, p -> p.stacksTo(1).durability(20).rarity(Rarity.EPIC));
    public static final DeferredItem<com.hydemod.item.realm.RetroItems.ChronoPendant> CHRONO_PENDANT = ITEMS.registerItem("chrono_pendant",
            com.hydemod.item.realm.RetroItems.ChronoPendant::new, p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<BlockItem> LAMP_POST = ITEMS.registerSimpleBlockItem("lamp_post", ModBlocks.LAMP_POST);

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

    private static DeferredItem<com.hydemod.item.NewsPropItem> newsProp(String id, com.hydemod.item.NewsPropItem.Kind kind,
            java.util.function.UnaryOperator<Item.Properties> properties) {
        return ITEMS.registerItem(id, p -> new com.hydemod.item.NewsPropItem(p, kind), properties);
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
