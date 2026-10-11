"""Generates HydeMod's JSON assets and data. Run: python3 tools/gen_data.py"""
import json, os, shutil

BASE = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
A = os.path.join(BASE, "assets", "hydemod")
D = os.path.join(BASE, "data", "hydemod")
M = "hydemod"

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

for sub in ("models", "items", "blockstates", "equipment", "lang"):
    shutil.rmtree(os.path.join(A, sub), ignore_errors=True)
shutil.rmtree(D, ignore_errors=True)

# ------------------------------------------------------------------ simple items
SIMPLE_ITEMS = ["trash_cube", "neoplas_nugget", "neoplas_ingot", "paradigm_shift_core", "sea_beet", "sea_yam", "sea_cabbage",
                "sea_salad", "sea_cheesy_baked_potato", "bulletproof_coffee", "soda_stream", "soda", "pat_on_the_back",
                "mind_upload_blackberry", "reset_the_clock", "maglev_moon_launcher", "einstein_parable", "tedx_invitation",
                "goober_goo", "elephant_fruit", "super_peanut", "hyde_helmet", "hyde_chestplate", "hyde_leggings", "hyde_boots",
                "green_kyber_crystal", "blue_kyber_crystal", "red_kyber_crystal", "purple_kyber_crystal", "lightsaber_hilt",
                "sam_hyde_spawn_egg", "elephant_mario_spawn_egg", "goober_spawn_egg", "depth_growler_spawn_egg"]
HANDHELD = ["ted_microphone", "pipe_wrench"]
for name in SIMPLE_ITEMS + HANDHELD:
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated",
                                        "textures": {"layer0": f"{M}:item/{name}"}})
    w(f"{A}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{M}:item/{name}"}})

# ------------------------------------------------------------------ blocks
CUBE_ALL = ["green_kyber_ore", "deepslate_blue_kyber_ore", "nether_red_kyber_ore", "end_purple_kyber_ore", "neoplas_block", "trash_cube_block"]
for name in CUBE_ALL:
    w(f"{A}/blockstates/{name}.json", {"variants": {"": {"model": f"{M}:block/{name}"}}})
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{M}:block/{name}"}})
    w(f"{A}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{M}:block/{name}"}})

w(f"{A}/blockstates/charging_station.json", {"variants": {"": {"model": f"{M}:block/charging_station"}}})
w(f"{A}/models/block/charging_station.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": f"{M}:block/charging_station_top", "bottom": f"{M}:block/charging_station_bottom", "side": f"{M}:block/charging_station_side"}})
w(f"{A}/items/charging_station.json", {"model": {"type": "minecraft:model", "model": f"{M}:block/charging_station"}})

# warp pipe: a 14x12x14 shaft topped with a full-width 16x4x16 rim
def face(tex, uv=None, cull=None):
    f = {"texture": tex}
    if uv: f["uv"] = uv
    if cull: f["cullface"] = cull
    return f
w(f"{A}/blockstates/warp_pipe.json", {"variants": {"": {"model": f"{M}:block/warp_pipe"}}})
w(f"{A}/models/block/warp_pipe.json", {
    "parent": "minecraft:block/block",
    "textures": {"particle": f"{M}:block/warp_pipe_side", "side": f"{M}:block/warp_pipe_side",
                 "top": f"{M}:block/warp_pipe_top", "bottom": f"{M}:block/warp_pipe_bottom"},
    "elements": [
        {"from": [1, 0, 1], "to": [15, 12, 15], "faces": {
            "down": face("#bottom", [1, 1, 15, 15], "down"),
            "north": face("#side", [1, 4, 15, 16]), "south": face("#side", [1, 4, 15, 16]),
            "west": face("#side", [1, 4, 15, 16]), "east": face("#side", [1, 4, 15, 16])}},
        {"from": [0, 12, 0], "to": [16, 16, 16], "faces": {
            "down": face("#bottom"), "up": face("#top", None, "up"),
            "north": face("#side", [0, 0, 16, 4], "north"), "south": face("#side", [0, 0, 16, 4], "south"),
            "west": face("#side", [0, 0, 16, 4], "west"), "east": face("#side", [0, 0, 16, 4], "east")}}
    ]})
w(f"{A}/items/warp_pipe.json", {"model": {"type": "minecraft:model", "model": f"{M}:block/warp_pipe"}})

# ------------------------------------------------------------------ lightsabers (3D: hilt + glowing blade)
SABER_DISPLAY = {
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "firstperson_righthand": {"rotation": [-10, -90, 15], "translation": [1.13, 0.5, 1.13], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [-10, 90, -15], "translation": [1.13, 0.5, 1.13], "scale": [0.68, 0.68, 0.68]},
    "gui": {"rotation": [0, 0, -45], "translation": [0, 0, 0], "scale": [0.45, 0.45, 0.45]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
    "fixed": {"rotation": [0, 0, -45], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "head": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
}
def box(fr, to, tex, emit=None):
    faces = {d: {"texture": tex, "uv": [0, 0, 16, 16]} for d in ("north", "south", "east", "west", "up", "down")}
    e = {"from": fr, "to": to, "faces": faces}
    if emit is not None:
        e["light_emission"] = emit
        e["shade"] = False
    return e
HILT = [box([7, -6, 7], [9, 4, 9], "#hilt"),           # grip
        box([6.5, 4, 6.5], [9.5, 6, 9.5], "#emitter"),  # emitter shroud
        box([6.75, -7, 6.75], [9.25, -6, 9.25], "#emitter")]  # pommel
for color in ("green", "blue", "red", "purple"):
    tex = {"hilt": f"{M}:item/lightsaber_hilt_metal", "emitter": f"{M}:item/lightsaber_emitter",
           "blade": f"{M}:item/lightsaber_blade_{color}", "particle": f"{M}:item/lightsaber_hilt_metal"}
    w(f"{A}/models/item/{color}_lightsaber_off.json", {"textures": tex, "elements": HILT, "display": SABER_DISPLAY, "gui_light": "front"})
    w(f"{A}/models/item/{color}_lightsaber_on.json", {"textures": tex, "display": SABER_DISPLAY, "gui_light": "front",
        "elements": HILT + [box([7.25, 6, 7.25], [8.75, 32, 8.75], "#blade", 15)]})
    w(f"{A}/items/{color}_lightsaber.json", {"model": {
        "type": "minecraft:condition", "property": "minecraft:has_component", "component": f"{M}:ignited",
        "on_true": {"type": "minecraft:model", "model": f"{M}:item/{color}_lightsaber_on"},
        "on_false": {"type": "minecraft:model", "model": f"{M}:item/{color}_lightsaber_off"}}})

# ------------------------------------------------------------------ equipment asset (Hyde's Armour)
w(f"{A}/equipment/hyde.json", {"layers": {"humanoid": [{"texture": f"{M}:hyde"}], "humanoid_leggings": [{"texture": f"{M}:hyde"}]}})

# ------------------------------------------------------------------ language
lang = {
    "itemGroup.hydemod": "HydeMod: 2070 Paradigm Shift",
    "item.hydemod.trash_cube": "Cube of Trash",
    "item.hydemod.neoplas_nugget": "Nugget of Neoplas",
    "item.hydemod.neoplas_ingot": "Neoplas Ingot",
    "item.hydemod.paradigm_shift_core": "Paradigm Shift Core",
    "item.hydemod.sea_beet": "Sea Beet",
    "item.hydemod.sea_yam": "Sea Yam",
    "item.hydemod.sea_cabbage": "Sea Cabbage",
    "item.hydemod.sea_salad": "Sea Salad",
    "item.hydemod.sea_cheesy_baked_potato": "Sea Cheesy Baked Potato",
    "item.hydemod.bulletproof_coffee": "Bulletproof Coffee",
    "item.hydemod.soda_stream": "SodaStream",
    "item.hydemod.soda": "Fizzy Soda",
    "item.hydemod.ted_microphone": "Microphone for the Unwashed Masses",
    "item.hydemod.pat_on_the_back": "Pat on the Back",
    "item.hydemod.mind_upload_blackberry": "Mind Upload BlackBerry",
    "item.hydemod.reset_the_clock": "Reset the Clock",
    "item.hydemod.maglev_moon_launcher": "Maglev Moon Launcher",
    "item.hydemod.einstein_parable": "The Einstein Parable",
    "item.hydemod.tedx_invitation": "TEDx Drexel Invitation",
    "item.hydemod.goober_goo": "Goober Goo",
    "item.hydemod.elephant_fruit": "Elephant Fruit",
    "item.hydemod.super_peanut": "Super Peanut",
    "item.hydemod.pipe_wrench": "Pipe Wrench",
    "item.hydemod.hyde_helmet": "Hyde's Helmet",
    "item.hydemod.hyde_chestplate": "Hyde's Chestplate",
    "item.hydemod.hyde_leggings": "Hyde's Leggings",
    "item.hydemod.hyde_boots": "Hyde's Boots",
    "item.hydemod.green_kyber_crystal": "Green Kyber Crystal",
    "item.hydemod.blue_kyber_crystal": "Blue Kyber Crystal",
    "item.hydemod.red_kyber_crystal": "Red Kyber Crystal",
    "item.hydemod.purple_kyber_crystal": "Purple Kyber Crystal",
    "item.hydemod.lightsaber_hilt": "Lightsaber Hilt",
    "item.hydemod.green_lightsaber": "Green Lightsaber",
    "item.hydemod.blue_lightsaber": "Blue Lightsaber",
    "item.hydemod.red_lightsaber": "Red Lightsaber",
    "item.hydemod.purple_lightsaber": "Purple Lightsaber",
    "item.hydemod.sam_hyde_spawn_egg": "Sam Hyde Spawn Egg",
    "item.hydemod.elephant_mario_spawn_egg": "Elephant Mario Spawn Egg",
    "item.hydemod.goober_spawn_egg": "Goober Spawn Egg",
    "item.hydemod.depth_growler_spawn_egg": "Depth Growler Spawn Egg",
    "block.hydemod.green_kyber_ore": "Green Kyber Ore",
    "block.hydemod.deepslate_blue_kyber_ore": "Deepslate Blue Kyber Ore",
    "block.hydemod.nether_red_kyber_ore": "Nether Red Kyber Ore",
    "block.hydemod.end_purple_kyber_ore": "End Purple Kyber Ore",
    "block.hydemod.neoplas_block": "Block of Neoplas",
    "block.hydemod.trash_cube_block": "Compacted Trash Economy",
    "block.hydemod.warp_pipe": "Warp Pipe",
    "block.hydemod.charging_station": "Lightsaber Charging Station",
    "entity.hydemod.sam_hyde": "Sam Hyde",
    "entity.hydemod.elephant_mario": "Elephant Mario",
    "entity.hydemod.goober": "Goober",
    "entity.hydemod.depth_growler": "Depth Growler",
    "entity.hydemod.thrown_trash_cube": "Cube of Trash",
    "entity.hydemod.trunk_spray": "Trunk Spray",

    # tooltips
    "tooltip.hydemod.ted_microphone": "Lets you speak to the unwashed masses. Right-click: deliver a 2070 prediction.",
    "tooltip.hydemod.pat_on_the_back": "That pat on the back is for saving the world. Heals you and nearby players.",
    "tooltip.hydemod.mind_upload_blackberry": "Sneak + use: upload your mind. Use: download it back (teleport).",
    "tooltip.hydemod.blackberry.saved": "Mind stored at %s %s %s (%s)",
    "tooltip.hydemod.reset_the_clock": "\"Can we reset the clock so I have another minute?\" Sets the time to morning.",
    "tooltip.hydemod.maglev_moon_launcher": "Maglev trains to get resources to the Moon. You are the resource.",
    "tooltip.hydemod.einstein": "A parable about a second-tier college. You'll never guess who the student was.",
    "tooltip.hydemod.tedx_invitation": "Use on a block to invite the final speaker of the final session.",
    "tooltip.hydemod.elephant_fruit": "Use on a block. Something big wants this.",
    "tooltip.hydemod.pipe_wrench": "Right-click one Warp Pipe, then another, to link them.",
    "tooltip.hydemod.pipe_wrench.pending": "First pipe: %s %s %s - now click the second pipe",
    "tooltip.hydemod.lightsaber.energy": "%s / %s RF",
    "tooltip.hydemod.lightsaber.on": "Blade: ignited (right-click to retract)",
    "tooltip.hydemod.lightsaber.off": "Blade: retracted (right-click to ignite)",
    "tooltip.hydemod.lightsaber.crystal.green": "Green kyber crystal (overworld)",
    "tooltip.hydemod.lightsaber.crystal.blue": "Blue kyber crystal (deep overworld)",
    "tooltip.hydemod.lightsaber.crystal.red": "Red kyber crystal (the Nether)",
    "tooltip.hydemod.lightsaber.crystal.purple": "Purple kyber crystal (the End)",

    # messages
    "message.hydemod.lightsaber.no_power": "No power! Charge it at a Lightsaber Charging Station.",
    "message.hydemod.lightsaber.depleted": "Your lightsaber sputters out - out of Redstone Flux.",
    "message.hydemod.station.energy": "Charging Station: %s / %s RF",
    "message.hydemod.station.full": "The Charging Station is full.",
    "message.hydemod.station.charged": "+%s RF -> saber now %s RF (station: %s RF left)",
    "message.hydemod.socks_blown_off": "That Sea Cheesy Baked Potato blew your socks off. (And your boots.)",
    "message.hydemod.pat_on_the_back": "Pat yourself on the back. That one's for saving the world.",
    "message.hydemod.blackberry.uploaded": "Mind uploaded to the BlackBerry.",
    "message.hydemod.blackberry.downloaded": "Mind downloaded. You feel cryogenically refreshed.",
    "message.hydemod.blackberry.empty": "No mind uploaded yet. Sneak + use to upload.",
    "message.hydemod.reset_clock": "%s asked them to reset the clock. It's morning again.",
    "message.hydemod.reset_clock.fail": "There's no clock to reset in this dimension.",
    "message.hydemod.wrench.first": "Pipe selected at %s %s %s. Now right-click the pipe to link it to.",
    "message.hydemod.wrench.linked": "Warp pipes linked! Stand on one and sneak to travel.",
    "message.hydemod.wrench.missing": "The other warp pipe is gone.",
    "message.hydemod.warp_pipe.unlinked": "This warp pipe isn't linked. Use a Pipe Wrench on two pipes.",
    "message.hydemod.warp_pipe.linked": "Linked to %s %s %s (%s). Sneak on top to warp.",
    "message.hydemod.sam_hyde_summoned": "Please welcome the final speaker of the final session: SAM HYDE. 2070 PARADIGM SHIFT.",
    "message.hydemod.elephant_mario_summoned": "The ground trembles. Somewhere, an elephant puts on a cap. ELEPHANT MARIO has arrived!",
    "chat.hydemod.speaker": "<%s> %s",
}
# Sam Hyde's lines: short riffs on the talk, written for the mod.
sam = {
    "intro": "Hey, can somebody reset the clock? Okay. Guys, pat yourselves on the back. Now let's talk about 2070.",
    "reset_clock": "Wait, wait - can we reset the clock? I need another minute. You're playing games with me, huh?",
    "trash": "TRASH ECONOMY! Cubes of trash as money! Everybody becomes rich! It's a gold rush!",
    "close_eyes": "Close your eyes. Everybody. Seal them up. Neurons are firing. This is a pivotal moment.",
    "growlers": "Rise up, depth growlers! Back from the tiered cities, toiling for nuggets of neoplas!",
    "maglev": "Maglev trains to get resources to the Moon. You're the resources.",
    "coffee": "You're drinking Bulletproof Coffee and you don't even realize it. I'm all jagged up in the head!",
    "pat": "Let's pat ourselves on the back. That one's for saving the world.",
    "death": "Man... we're so screwed. Thanks for having me. What do you want me to walk to here?",
}
for k, v in sam.items():
    lang[f"boss.hydemod.sam_hyde.{k}"] = v
for i, v in enumerate([
    "Ow! You're going places, kid.",
    "I'm an important thinker, you know.",
    "You caught me. I'm unprepared.",
    "I'm a passionate, childlike innovator!",
    "Hey, I didn't do good in school either.",
    "Is this anybody's water? I'm claiming it."], 1):
    lang[f"boss.hydemod.sam_hyde.hurt.{i}"] = v
for i, v in enumerate([
    "What inspires you? I want an answer. What inspires YOU?",
    "Ideas are like currency.",
    "Look at us. We're the machine that keeps the world going.",
    "By my calculations we have five years until the world ends.",
    "Have you ever had sea cheesy baked potatoes? They'll blow your socks off.",
    "Video games are going to get more realistic. Look around you.",
    "You think I'm nuts? Eighty, a hundred miles per gallon. Not that crazy.",
    "I'm going to have your mind uploaded to my BlackBerry.",
    "You'll be cryogenically frozen. I know it.",
    "There's no more individual. It's just the hive."], 1):
    lang[f"boss.hydemod.sam_hyde.taunt.{i}"] = v
# Predictions shouted by the microphone item.
for i, v in enumerate([
    "2070: Sea floor farming! Sea beets, sea yams, sea cabbage!",
    "2070: Cubes of trash are money. Everybody becomes rich.",
    "2070: Vast pleasure domes for the elites. The rest of you toil underground for nuggets of neoplas.",
    "2070: Video games are going to get more realistic.",
    "2070: Super fuel-efficient vehicles - 80, 100 miles per gallon!",
    "2070: Flip side of that coin - gas is ten dollars a gallon.",
    "2070: You can't win them all, but we'll have solar power.",
    "2070: Robots policing the streets.",
    "2070: SodaStream does for soda what 3D printing did for... everything.",
    "2070: 3D printers are kids' toys.",
    "2070: iPads built into every school desk.",
    "2070: Your mind is uploaded to a BlackBerry.",
    "2070: Fifty billion people, and robots to bridge the gap.",
    "2070: Facebook has your birth certificate."], 1):
    lang[f"prediction.hydemod.{i}"] = v
for i, v in enumerate([
    "I was at a college - a second-tier college. Not an Ivy. A second-choice school.",
    "The teacher was spouting nonsense, and nobody dared speak up. Except one student.",
    "He had logic on his side, and in the end he proved his point.",
    "That student... was Albert Einstein."], 1):
    lang[f"quote.hydemod.einstein.{i}"] = v
em = {
    "intro": "*TRUMPET* Who ate my fruit?! Somebody is getting stomped.",
    "pound": "GROUND POUND!",
    "spray": "*fwoosh* Trunk spray!",
    "goobers": "Goobers! Get 'em!",
    "charge": "*TRUMPET* Here I come!",
    "death": "*sad trumpet* ...my peanuts...",
}
for k, v in em.items():
    lang[f"boss.hydemod.elephant_mario.{k}"] = v
for i, v in enumerate(["Oof!", "*angry trumpet*", "Ow, my trunk!", "My ears!", "Not the ears!"], 1):
    lang[f"boss.hydemod.elephant_mario.hurt.{i}"] = v
lang.update({
    "tooltip.hydemod.trash_cube": "Currency of the 2070 trash economy. Also throwable.",
    "tooltip.hydemod.neoplas_nugget": "The depth growlers toil endlessly for these.",
    "tooltip.hydemod.paradigm_shift_core": "A whole paradigm, shifted. Dropped by Sam Hyde.",
    "tooltip.hydemod.sea_cheesy_baked_potato": "Will blow your socks off. Literally.",
    "tooltip.hydemod.bulletproof_coffee": "Gets you all jagged up in the head.",
    "tooltip.hydemod.soda_stream": "Craft with a water bottle and sugar to make soda. Not used up.",
    "tooltip.hydemod.super_peanut": "Elephant power, in legume form.",
    "tooltip.hydemod.hyde_chestplate": "Full set: Strength, Resistance, Fire Resistance, Night Vision.",
    "tooltip.hydemod.lightsaber_hilt": "Combine with a kyber crystal to build a lightsaber.",
})
w(f"{A}/lang/en_us.json", lang)

# ------------------------------------------------------------------ loot tables: blocks
def ore_loot(block, drop):
    return {"type": "minecraft:block", "random_sequence": f"{M}:blocks/{block}", "pools": [{"rolls": 1.0, "entries": [{
        "type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{M}:{block}", "conditions": [{"condition": "minecraft:match_tool", "predicate": {
                "predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
            {"type": "minecraft:item", "name": f"{M}:{drop}", "functions": [
                {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
                {"function": "minecraft:explosion_decay"}]}]}]}]}
def self_loot(block):
    return {"type": "minecraft:block", "random_sequence": f"{M}:blocks/{block}", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "conditions": [{"condition": "minecraft:survives_explosion"}], "entries": [{"type": "minecraft:item", "name": f"{M}:{block}"}]}]}
w(f"{D}/loot_table/blocks/green_kyber_ore.json", ore_loot("green_kyber_ore", "green_kyber_crystal"))
w(f"{D}/loot_table/blocks/deepslate_blue_kyber_ore.json", ore_loot("deepslate_blue_kyber_ore", "blue_kyber_crystal"))
w(f"{D}/loot_table/blocks/nether_red_kyber_ore.json", ore_loot("nether_red_kyber_ore", "red_kyber_crystal"))
w(f"{D}/loot_table/blocks/end_purple_kyber_ore.json", ore_loot("end_purple_kyber_ore", "purple_kyber_crystal"))
for b in ("neoplas_block", "trash_cube_block", "warp_pipe", "charging_station"):
    w(f"{D}/loot_table/blocks/{b}.json", self_loot(b))

# ------------------------------------------------------------------ loot tables: entities
def item(name, lo, hi, chance=None, looting=True):
    e = {"type": "minecraft:item", "name": name, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}
    if looting:
        e["functions"].append({"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}})
    pool = {"rolls": 1, "entries": [e]}
    if chance is not None:
        pool["conditions"] = [{"condition": "minecraft:random_chance", "chance": chance}]
    return pool
def entity_loot(name, pools):
    w(f"{D}/loot_table/entities/{name}.json", {"type": "minecraft:entity", "random_sequence": f"{M}:entities/{name}", "pools": pools})
entity_loot("sam_hyde", [
    item(f"{M}:paradigm_shift_core", 1, 1, looting=False),
    item(f"{M}:neoplas_ingot", 6, 10),
    item(f"{M}:ted_microphone", 1, 1, looting=False),
    item(f"{M}:trash_cube", 8, 16),
    item(f"{M}:bulletproof_coffee", 2, 4),
    item(f"{M}:pat_on_the_back", 1, 1, looting=False),
    item(f"{M}:mind_upload_blackberry", 1, 1, 0.5, looting=False),
    item(f"{M}:reset_the_clock", 1, 1, 0.5, looting=False),
    item(f"{M}:einstein_parable", 1, 1, looting=False),
])
entity_loot("elephant_mario", [
    item(f"{M}:warp_pipe", 4, 6),
    item(f"{M}:pipe_wrench", 1, 1, looting=False),
    item(f"{M}:super_peanut", 3, 5),
    item(f"{M}:goober_goo", 6, 12),
    item(f"{M}:neoplas_ingot", 3, 6),
])
entity_loot("goober", [item(f"{M}:goober_goo", 0, 2)])
entity_loot("depth_growler", [item(f"{M}:neoplas_nugget", 1, 3), item(f"{M}:trash_cube", 0, 2, 0.5)])

# sea crops from seagrass & kelp (the sea floor farming of 2070)
for crop, table in (("sea_beet", "seagrass"), ("sea_yam", "tall_seagrass"), ("sea_cabbage", "kelp_plant"), ("sea_cabbage", "kelp")):
    w(f"{D}/loot_table/gameplay/{crop}_from_{table}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{M}:{crop}"}], "conditions": [{"condition": "minecraft:random_chance", "chance": 0.12}]}]})
    w(f"{D}/loot_modifiers/{crop}_from_{table}.json", {"type": "neoforge:add_table",
        "conditions": [{"condition": "neoforge:loot_table_id", "loot_table_id": f"minecraft:blocks/{table}"}],
        "table": f"{M}:gameplay/{crop}_from_{table}"})

# ------------------------------------------------------------------ tags
def tag(path, values):
    w(f"{BASE}/data/{path}.json", {"replace": False, "values": values})
ores = [f"{M}:green_kyber_ore", f"{M}:deepslate_blue_kyber_ore", f"{M}:nether_red_kyber_ore", f"{M}:end_purple_kyber_ore"]
tag("minecraft/tags/block/mineable/pickaxe", ores + [f"{M}:neoplas_block", f"{M}:warp_pipe", f"{M}:charging_station"])
tag("minecraft/tags/block/mineable/shovel", [f"{M}:trash_cube_block"])
tag("minecraft/tags/block/needs_iron_tool", [f"{M}:green_kyber_ore", f"{M}:deepslate_blue_kyber_ore", f"{M}:neoplas_block"])
tag("minecraft/tags/block/needs_diamond_tool", [f"{M}:nether_red_kyber_ore", f"{M}:end_purple_kyber_ore"])
tag("hydemod/tags/item/repairs_hyde_armour", [f"{M}:neoplas_ingot"])
tag("minecraft/tags/item/head_armor", [f"{M}:hyde_helmet"])
tag("minecraft/tags/item/chest_armor", [f"{M}:hyde_chestplate"])
tag("minecraft/tags/item/leg_armor", [f"{M}:hyde_leggings"])
tag("minecraft/tags/item/foot_armor", [f"{M}:hyde_boots"])
tag("minecraft/tags/item/swords", [f"{M}:{c}_lightsaber" for c in ("green", "blue", "red", "purple")] + [f"{M}:ted_microphone"])

# ------------------------------------------------------------------ worldgen: kyber ores
def ore_feature(name, targets, size, discard=0.0):
    w(f"{D}/worldgen/configured_feature/{name}.json", {"type": "minecraft:ore", "config": {
        "size": size, "discard_chance_on_air_exposure": discard, "targets": targets}})
def placed(name, count, lo, hi, kind="uniform", rarity=None):
    placement = []
    if rarity:
        placement.append({"type": "minecraft:rarity_filter", "chance": rarity})
    else:
        placement.append({"type": "minecraft:count", "count": count})
    placement += [{"type": "minecraft:in_square"},
                  {"type": "minecraft:height_range", "height": {"type": f"minecraft:{kind}", "min_inclusive": lo, "max_inclusive": hi}},
                  {"type": "minecraft:biome"}]
    w(f"{D}/worldgen/placed_feature/{name}.json", {"feature": f"{M}:{name}", "placement": placement})
def target(tag_, block):
    return {"target": {"predicate_type": "minecraft:tag_match", "tag": tag_}, "state": {"Name": block}}
def target_block(b, block):
    return {"target": {"predicate_type": "minecraft:block_match", "block": b}, "state": {"Name": block}}
# Green: fairly common in overworld stone, Y 0..64
ore_feature("ore_green_kyber", [target("minecraft:stone_ore_replaceables", f"{M}:green_kyber_ore")], 4)
placed("ore_green_kyber", 4, {"absolute": 0}, {"absolute": 64})
# Blue: deep overworld deepslate only, Y -64..0, rarer
ore_feature("ore_blue_kyber", [target("minecraft:deepslate_ore_replaceables", f"{M}:deepslate_blue_kyber_ore")], 4, 0.3)
placed("ore_blue_kyber", 3, {"above_bottom": 0}, {"absolute": 0}, "trapezoid")
# Red: nether, in netherrack, needs a trip to the Nether
ore_feature("ore_red_kyber", [target_block("minecraft:netherrack", f"{M}:nether_red_kyber_ore")], 3)
placed("ore_red_kyber", 3, {"above_bottom": 10}, {"below_top": 10})
# Purple: the End, in end stone, rarest
ore_feature("ore_purple_kyber", [target_block("minecraft:end_stone", f"{M}:end_purple_kyber_ore")], 3)
placed("ore_purple_kyber", 2, {"absolute": 0}, {"absolute": 80})

def biome_mod(name, biomes, feature):
    w(f"{D}/neoforge/biome_modifier/{name}.json", {"type": "neoforge:add_features", "biomes": biomes,
                                                  "features": f"{M}:{feature}", "step": "underground_ores"})
biome_mod("add_green_kyber", "#minecraft:is_overworld", "ore_green_kyber")
biome_mod("add_blue_kyber", "#minecraft:is_overworld", "ore_blue_kyber")
biome_mod("add_red_kyber", "#minecraft:is_nether", "ore_red_kyber")
biome_mod("add_purple_kyber", "#minecraft:is_end", "ore_purple_kyber")

# mob spawns
w(f"{D}/neoforge/biome_modifier/spawn_goobers.json", {"type": "neoforge:add_spawns", "biomes": "#minecraft:is_overworld",
    "spawners": [{"type": f"{M}:goober", "weight": 40, "minCount": 2, "maxCount": 4}]})
w(f"{D}/neoforge/biome_modifier/spawn_depth_growlers.json", {"type": "neoforge:add_spawns", "biomes": "#minecraft:is_overworld",
    "spawners": [{"type": f"{M}:depth_growler", "weight": 35, "minCount": 1, "maxCount": 3}]})

# ------------------------------------------------------------------ recipes
def shaped(name, pattern, key, result, count=1, cat="misc"):
    w(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pattern,
        "key": key, "result": {"id": result, "count": count}})
def shapeless(name, ingredients, result, count=1, cat="misc"):
    w(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": cat, "ingredients": ingredients,
        "result": {"id": result, "count": count}})
def smelt(name, ing, result, xp=0.5, kinds=("smelting", "blasting")):
    for k in kinds:
        w(f"{D}/recipe/{name}_from_{k}.json", {"type": f"minecraft:{k}", "category": "misc", "ingredient": ing,
            "result": {"id": result}, "experience": xp, "cookingtime": 200 if k == "smelting" else 100})

shapeless("trash_cube", ["minecraft:rotten_flesh", "minecraft:bone", "minecraft:string", "minecraft:paper"], f"{M}:trash_cube", 4)
shaped("trash_cube_block", ["###", "###", "###"], {"#": f"{M}:trash_cube"}, f"{M}:trash_cube_block", cat="building")
shapeless("trash_cube_from_block", [f"{M}:trash_cube_block"], f"{M}:trash_cube", 9)
shaped("trash_economy_emerald", ["###", "#G#", "###"], {"#": f"{M}:trash_cube", "G": "minecraft:gold_nugget"}, "minecraft:emerald")
shaped("neoplas_ingot", ["###", "###", "###"], {"#": f"{M}:neoplas_nugget"}, f"{M}:neoplas_ingot")
shapeless("neoplas_nugget", [f"{M}:neoplas_ingot"], f"{M}:neoplas_nugget", 9)
shaped("neoplas_block", ["###", "###", "###"], {"#": f"{M}:neoplas_ingot"}, f"{M}:neoplas_block", cat="building")
shapeless("neoplas_ingot_from_block", [f"{M}:neoplas_block"], f"{M}:neoplas_ingot", 9)
shapeless("sea_salad", ["minecraft:bowl", f"{M}:sea_beet", f"{M}:sea_yam", f"{M}:sea_cabbage"], f"{M}:sea_salad")
shapeless("sea_cheesy_baked_potato", ["minecraft:baked_potato", "minecraft:milk_bucket", "minecraft:dried_kelp", f"{M}:sea_cabbage"],
          f"{M}:sea_cheesy_baked_potato")
shapeless("bulletproof_coffee", ["minecraft:glass_bottle", "minecraft:cocoa_beans", "minecraft:cocoa_beans", "minecraft:milk_bucket", "minecraft:sugar"],
          f"{M}:bulletproof_coffee")
shaped("soda_stream", ["IGI", "I I", "III"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass_bottle"}, f"{M}:soda_stream")
shapeless("soda", [f"{M}:soda_stream", "minecraft:potion", "minecraft:sugar", "minecraft:sweet_berries"], f"{M}:soda")
shaped("ted_microphone", [" GI", " RG", "S  "], {"G": "minecraft:iron_bars", "I": "minecraft:iron_ingot", "R": "minecraft:redstone",
       "S": "minecraft:stick"}, f"{M}:ted_microphone", cat="equipment")
shaped("pat_on_the_back", [" L ", "LGL", " L "], {"L": "minecraft:leather", "G": "minecraft:golden_apple"}, f"{M}:pat_on_the_back")
shaped("mind_upload_blackberry", ["OGO", "ORO", "OEO"], {"O": "minecraft:obsidian", "G": "minecraft:glass_pane", "R": "minecraft:redstone",
       "E": "minecraft:ender_pearl"}, f"{M}:mind_upload_blackberry")
shaped("reset_the_clock", [" N ", "NCN", " N "], {"N": f"{M}:neoplas_ingot", "C": "minecraft:clock"}, f"{M}:reset_the_clock")
shaped("maglev_moon_launcher", ["  N", " R ", "I  "], {"N": f"{M}:neoplas_ingot", "R": "minecraft:redstone_block", "I": "minecraft:iron_ingot"},
       f"{M}:maglev_moon_launcher")
shaped("tedx_invitation", ["PNP", "TBT", "PNP"], {"P": "minecraft:paper", "N": f"{M}:neoplas_nugget", "T": f"{M}:trash_cube",
       "B": "minecraft:writable_book"}, f"{M}:tedx_invitation")
shaped("elephant_fruit", ["GOG", "OAO", "GOG"], {"G": "minecraft:gold_ingot", "O": f"{M}:goober_goo", "A": "minecraft:apple"},
       f"{M}:elephant_fruit")
shaped("warp_pipe", ["III", "IEI", "III"], {"I": "minecraft:iron_ingot", "E": "minecraft:ender_pearl"}, f"{M}:warp_pipe", 2)
shapeless("warp_pipe_dye", [f"{M}:warp_pipe", "minecraft:green_dye"], f"{M}:warp_pipe")
shaped("pipe_wrench", [" I ", " II", "I  "], {"I": "minecraft:iron_ingot"}, f"{M}:pipe_wrench", cat="equipment")
shaped("hyde_helmet", ["NNN", "N N"], {"N": f"{M}:neoplas_ingot"}, f"{M}:hyde_helmet", cat="equipment")
shaped("hyde_chestplate", ["N N", "NCN", "NNN"], {"N": f"{M}:neoplas_ingot", "C": f"{M}:paradigm_shift_core"}, f"{M}:hyde_chestplate", cat="equipment")
shaped("hyde_leggings", ["NNN", "N N", "N N"], {"N": f"{M}:neoplas_ingot"}, f"{M}:hyde_leggings", cat="equipment")
shaped("hyde_boots", ["N N", "N N"], {"N": f"{M}:neoplas_ingot"}, f"{M}:hyde_boots", cat="equipment")
shaped("lightsaber_hilt", [" C ", "IRI", " I "], {"C": "minecraft:copper_ingot", "I": "minecraft:iron_ingot", "R": "minecraft:redstone_block"},
       f"{M}:lightsaber_hilt", cat="equipment")
for c in ("green", "blue", "red", "purple"):
    shapeless(f"{c}_lightsaber", [f"{M}:lightsaber_hilt", f"{M}:{c}_kyber_crystal"], f"{M}:{c}_lightsaber", cat="equipment")
shaped("charging_station", ["IRI", "RBR", "IRI"], {"I": "minecraft:iron_ingot", "R": "minecraft:redstone", "B": "minecraft:redstone_block"},
       f"{M}:charging_station")
for ore, crystal in (("green_kyber_ore", "green_kyber_crystal"), ("deepslate_blue_kyber_ore", "blue_kyber_crystal"),
                     ("nether_red_kyber_ore", "red_kyber_crystal"), ("end_purple_kyber_ore", "purple_kyber_crystal")):
    smelt(crystal, f"{M}:{ore}", f"{M}:{crystal}", 1.0)
smelt("neoplas_nugget_from_trash", f"{M}:trash_cube_block", f"{M}:neoplas_nugget", 0.3)

print("data written")
