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
                "goobers", "peanut", "elephant_fruit", "giant_goobers_box", "super_peanut", "hyde_helmet", "hyde_chestplate", "hyde_leggings", "hyde_boots",
                "green_kyber_crystal", "blue_kyber_crystal", "red_kyber_crystal", "purple_kyber_crystal", "lightsaber_hilt",
                "sam_hyde_spawn_egg", "elephant_mario_spawn_egg", "depth_grovler_spawn_egg"]
HANDHELD = ["ted_microphone", "pipe_wrench"]
for name in SIMPLE_ITEMS + HANDHELD:
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated",
                                        "textures": {"layer0": f"{M}:item/{name}"}})
    w(f"{A}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{M}:item/{name}"}})

# ------------------------------------------------------------------ blocks
CUBE_ALL = ["neoplas_block", "trash_cube_block"]
ORE_BASES = {"green_kyber_ore": ("minecraft:block/stone", "green"), "deepslate_blue_kyber_ore": ("minecraft:block/deepslate", "blue"),
             "nether_red_kyber_ore": ("minecraft:block/netherrack", "red"), "end_purple_kyber_ore": ("minecraft:block/end_stone", "purple")}
def full_cube(tex, emit=None):
    e = {"from": [0, 0, 0], "to": [16, 16, 16],
         "faces": {d: {"texture": tex, "cullface": d} for d in ("down", "up", "north", "south", "west", "east")}}
    if emit is not None:
        e["light_emission"] = emit
    return e
for name, (base, color) in ORE_BASES.items():
    w(f"{A}/blockstates/{name}.json", {"variants": {"": {"model": f"{M}:block/{name}"}}})
    w(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/block",
        "textures": {"particle": base, "base": base, "crystals": f"{M}:block/{color}_kyber_crystals"},
        "elements": [full_cube("#base"), full_cube("#crystals", 15)]})
    w(f"{A}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{M}:block/{name}"}})
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
    tex["halo"] = f"{M}:item/lightsaber_halo_{color}"
    w(f"{A}/models/item/{color}_lightsaber_off.json", {"textures": tex, "elements": HILT, "display": SABER_DISPLAY, "gui_light": "front"})
    cases = []
    for step in (1, 2, 3, 4):
        top = 6 + 25 * step / 4  # halo/tip must stay within the 32-unit model limit
        blade = [box([7.25, 6, 7.25], [8.75, top, 8.75], "#blade", 15),          # white-hot core
                 box([6.4, 5.5, 6.4], [9.6, top + 0.6, 9.6], "#halo", 15)]       # translucent glow halo
        if step == 4:
            blade.append(box([7.6, top, 7.6], [8.4, top + 0.8, 8.4], "#blade", 15))  # rounded tip
        w(f"{A}/models/item/{color}_lightsaber_blade{step}.json", {"textures": tex, "display": SABER_DISPLAY, "gui_light": "front",
            "elements": HILT + blade})
        cases.append({"when": step, "model": {"type": "minecraft:model", "model": f"{M}:item/{color}_lightsaber_blade{step}"}})
    w(f"{A}/items/{color}_lightsaber.json", {"model": {
        "type": "minecraft:select", "property": "minecraft:component", "component": f"{M}:blade", "cases": cases,
        "fallback": {"type": "minecraft:model", "model": f"{M}:item/{color}_lightsaber_off"}}})

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
    "item.hydemod.goobers": "Goobers",
    "item.hydemod.peanut": "Peanut",
    "item.hydemod.elephant_fruit": "Elephant Fruit",
    "item.hydemod.giant_goobers_box": "Giant Box of Goobers",
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
    "item.hydemod.depth_grovler_spawn_egg": "Depth Grovler Spawn Egg",
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
    "entity.hydemod.depth_grovler": "Depth Grovler",
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
    "tooltip.hydemod.elephant_fruit": "Eat it: Strength II and Speed II for one hour. Dropped by Elephant Mario.",
    "tooltip.hydemod.giant_goobers_box": "Use on a block. Something big can smell it from miles away...",
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
    "message.hydemod.boss_peaceful": "Bosses can't be summoned on Peaceful difficulty.",
    "title.hydemod.sam_hyde": "SAM HYDE",
    "title.hydemod.sam_hyde.sub": "2070 PARADIGM SHIFT",
    "title.hydemod.paradigm_shift": "PARADIGM SHIFT",
    "title.hydemod.paradigm_shift.sub": "The future is NOW.",
    "title.hydemod.elephant_mario": "ELEPHANT MARIO",
    "title.hydemod.elephant_mario.sub": "Who ate my fruit?!",
    "title.hydemod.elephant_power": "ELEPHANT POWER",
    "title.hydemod.elephant_power.sub": "He's getting BIGGER",
    "title.hydemod.defeated": "BOSS DEFEATED",
    "subtitles.hydemod.boss.trumpet": "Elephant trumpets",
    "subtitles.hydemod.boss.boom": "Ground pound slams",
    "subtitles.hydemod.boss.applause": "Audience applauds",
    "subtitles.hydemod.boss.intro": "Boss appears",
    "subtitles.hydemod.boss.phase": "Boss powers up",
    "subtitles.hydemod.music.sam_hyde": "Music: 2070 Paradigm Shift",
    "subtitles.hydemod.music.elephant_mario": "Music: Elephant Stomp",
}
# Sam Hyde's lines: short riffs on the talk, written for the mod.
sam = {
    "intro": "Hey, can somebody reset the clock? Okay. Guys, pat yourselves on the back. Now let's talk about 2070.",
    "reset_clock": "Wait, wait - can we reset the clock? I need another minute. You're playing games with me, huh?",
    "trash": "TRASH ECONOMY! Cubes of trash as money! Everybody becomes rich! It's a gold rush!",
    "close_eyes": "Close your eyes. Everybody. Seal them up. Neurons are firing. This is a pivotal moment.",
    "grovlers": "Rise up, depth grovlers! Up from the tiered cities, toiling for nuggets of neoplas!",
    "maglev": "Maglev trains to get resources to the Moon. You're the resources.",
    "coffee": "You're drinking Bulletproof Coffee and you don't even realize it. I'm all jagged up in the head!",
    "pat": "Let's pat ourselves on the back. That one's for saving the world.",
    "death": "Man... we're so screwed. Thanks for having me. What do you want me to walk to here?",
    "phase_two": "Okay, okay. Now it's 2070. Now what? PARADIGM SHIFT!",
    "trash_rain": "Are we going to put it on an island? Make it somebody else's problem? IT'S RAINING TRASH!",
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
    "goobers": "Have some GOOBERS!",
    "snack": "*munch munch* ...Goobers break.",
    "phase_two": "*EARTH-SHAKING TRUMPET* ELEPHANT POWER!",
    "charge": "*TRUMPET* Here I come!",
    "death": "*sad trumpet* ...my peanuts...",
}
for k, v in em.items():
    lang[f"boss.hydemod.elephant_mario.{k}"] = v
for i, v in enumerate(["Oof!", "*angry trumpet*", "Ow, my trunk!", "My ears!", "Not the ears!"], 1):
    lang[f"boss.hydemod.elephant_mario.hurt.{i}"] = v
lang.update({
    "tooltip.hydemod.trash_cube": "Currency of the 2070 trash economy. Also throwable.",
    "tooltip.hydemod.neoplas_nugget": "The depth grovlers toil endlessly for these.",
    "tooltip.hydemod.goobers": "Chocolate-covered peanuts. Elephant Mario's favourite.",
    "tooltip.hydemod.peanut": "Found in tall grass. Coat it in chocolate.",
    "subtitles.hydemod.lightsaber.ignite": "Lightsaber ignites",
    "subtitles.hydemod.lightsaber.retract": "Lightsaber retracts",
    "subtitles.hydemod.lightsaber.hum": "Lightsaber hums",
    "subtitles.hydemod.lightsaber.swing": "Lightsaber swooshes",
    "subtitles.hydemod.lightsaber.hit": "Lightsaber strikes",
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
    item(f"{M}:hyde_helmet", 1, 1, looting=False),
    item(f"{M}:hyde_chestplate", 1, 1, looting=False),
    item(f"{M}:hyde_leggings", 1, 1, looting=False),
    item(f"{M}:hyde_boots", 1, 1, looting=False),
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
    item(f"{M}:elephant_fruit", 2, 4),
    item(f"{M}:warp_pipe", 4, 6),
    item(f"{M}:pipe_wrench", 1, 1, looting=False),
    item(f"{M}:super_peanut", 3, 5),
    item(f"{M}:goobers", 6, 12),
    item(f"{M}:neoplas_ingot", 3, 6),
])
entity_loot("depth_grovler", [item(f"{M}:neoplas_nugget", 1, 3), item(f"{M}:trash_cube", 0, 2, 0.4)])

# sea crops from seagrass & kelp (the sea floor farming of 2070)
for crop, table in (("peanut", "short_grass"), ("peanut", "tall_grass"), ("sea_beet", "seagrass"), ("sea_yam", "tall_seagrass"), ("sea_cabbage", "kelp_plant"), ("sea_cabbage", "kelp")):
    w(f"{D}/loot_table/gameplay/{crop}_from_{table}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{M}:{crop}"}], "conditions": [{"condition": "minecraft:random_chance", "chance": 0.12}]}]})
    w(f"{D}/loot_modifiers/{crop}_from_{table}.json", {"type": "neoforge:add_table",
        "conditions": [{"condition": "neoforge:loot_table_id", "loot_table_id": f"minecraft:blocks/{table}"}],
        "table": f"{M}:gameplay/{crop}_from_{table}"})

# ------------------------------------------------------------------ tags
def tag(path, values):
    w(f"{BASE}/data/{path}.json", {"replace": False, "values": values})
ores = [f"{M}:green_kyber_ore", f"{M}:deepslate_blue_kyber_ore", f"{M}:nether_red_kyber_ore", f"{M}:end_purple_kyber_ore"]
tag("minecraft/tags/block/mineable/pickaxe", ores + [f"{M}:neoplas_block", f"{M}:warp_pipe", f"{M}:charging_station", f"{M}:lamp_post"])
tag("minecraft/tags/block/mineable/shovel", [f"{M}:trash_cube_block"])
tag("minecraft/tags/block/needs_iron_tool", [f"{M}:green_kyber_ore", f"{M}:deepslate_blue_kyber_ore", f"{M}:neoplas_block"])
tag("minecraft/tags/block/needs_diamond_tool", [f"{M}:nether_red_kyber_ore", f"{M}:end_purple_kyber_ore"])
tag("hydemod/tags/item/repairs_hyde_armour", [f"{M}:neoplas_ingot"])
tag("minecraft/tags/item/head_armor", [f"{M}:hyde_helmet"])
tag("minecraft/tags/item/chest_armor", [f"{M}:hyde_chestplate", f"{M}:mithril_mail"])
tag("minecraft/tags/item/leg_armor", [f"{M}:hyde_leggings"])
tag("minecraft/tags/item/foot_armor", [f"{M}:hyde_boots"])
tag("minecraft/tags/item/swords", [f"{M}:{c}_lightsaber" for c in ("green", "blue", "red", "purple")] + [f"{M}:ted_microphone", f"{M}:elven_blade"])

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
placed("ore_purple_kyber", 4, {"absolute": 0}, {"absolute": 80})

def biome_mod(name, biomes, feature):
    w(f"{D}/neoforge/biome_modifier/{name}.json", {"type": "neoforge:add_features", "biomes": biomes,
                                                  "features": f"{M}:{feature}", "step": "underground_ores"})
biome_mod("add_green_kyber", "#minecraft:is_overworld", "ore_green_kyber")
biome_mod("add_blue_kyber", "#minecraft:is_overworld", "ore_blue_kyber")
biome_mod("add_red_kyber", "#minecraft:is_nether", "ore_red_kyber")
biome_mod("add_purple_kyber", "#minecraft:is_end", "ore_purple_kyber")

# mob spawns
w(f"{D}/neoforge/biome_modifier/spawn_depth_grovlers.json", {"type": "neoforge:add_spawns", "biomes": "#minecraft:is_overworld",
    "spawners": [{"type": f"{M}:depth_grovler", "weight": 45, "minCount": 2, "maxCount": 4}]})

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
shapeless("goobers", [f"{M}:peanut", f"{M}:peanut", "minecraft:cocoa_beans", "minecraft:sugar"], f"{M}:goobers", 4)
shaped("giant_goobers_box", ["GOG", "OAO", "GOG"], {"G": "minecraft:gold_ingot", "O": f"{M}:goobers", "A": "minecraft:golden_apple"},
       f"{M}:giant_goobers_box")
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

# ================================================================== realm items
REALM_SIMPLE = ["turkish_delight", "healing_cordial", "invisibility_cloak", "butterscotch_brew", "power_mushroom", "blaze_blossom",
                "invincibility_star", "heart_vessel", "bomb", "ocarina", "ring_of_unseeing", "mithril_mail", "elven_waybread", "star_phial"]
REALM_HANDHELD = ["stone_spell_wand", "wizard_wand", "flying_broom", "grappling_hook"]
for name in REALM_SIMPLE + REALM_HANDHELD:
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/handheld" if name in REALM_HANDHELD else "minecraft:item/generated",
                                        "textures": {"layer0": f"{M}:item/{name}"}})
    w(f"{A}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{M}:item/{name}"}})
for v in ("elven_blade", "elven_blade_glowing"):
    w(f"{A}/models/item/{v}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{M}:item/{v}"}})
w(f"{A}/items/elven_blade.json", {"model": {"type": "minecraft:condition", "property": "minecraft:has_component", "component": f"{M}:warning",
    "on_true": {"type": "minecraft:model", "model": f"{M}:item/elven_blade_glowing"},
    "on_false": {"type": "minecraft:model", "model": f"{M}:item/elven_blade"}}})
# lamp-post: iron post + glowing lantern head with a snow cap
def el(fr, to, tex, emit=None):
    e = {"from": fr, "to": to, "faces": {d: {"texture": tex} for d in ("north", "south", "east", "west", "up", "down")}}
    if emit: e["light_emission"] = emit
    return e
w(f"{A}/blockstates/lamp_post.json", {"variants": {"": {"model": f"{M}:block/lamp_post"}}})
w(f"{A}/models/block/lamp_post.json", {"parent": "minecraft:block/block", "ambientocclusion": False,
    "textures": {"particle": f"{M}:block/lamp_post_iron", "iron": f"{M}:block/lamp_post_iron", "glass": f"{M}:block/lamp_post_glass", "snow": f"{M}:block/lamp_post_snow"},
    "elements": [el([6.5, 0, 6.5], [9.5, 11, 9.5], "#iron"), el([5, 0, 5], [11, 1.5, 11], "#iron"),
                 el([4.5, 11, 4.5], [11.5, 12, 11.5], "#iron"), el([5, 12, 5], [11, 15, 11], "#glass", 15),
                 el([4, 15, 4], [12, 16, 12], "#iron"), el([4.5, 15.6, 4.5], [11.5, 16, 11.5], "#snow")]})
w(f"{A}/items/lamp_post.json", {"model": {"type": "minecraft:model", "model": f"{M}:block/lamp_post"}})
w(f"{D}/loot_table/blocks/lamp_post.json", self_loot("lamp_post"))
w(f"{A}/equipment/mithril.json", {"layers": {"humanoid": [{"texture": f"{M}:mithril"}]}})

realm_lang = {
    "item.hydemod.turkish_delight": "Turkish Delight",
    "item.hydemod.healing_cordial": "Healing Cordial",
    "item.hydemod.stone_spell_wand": "Stone-Spell Wand",
    "item.hydemod.wizard_wand": "Wizard's Wand",
    "item.hydemod.invisibility_cloak": "Invisibility Cloak",
    "item.hydemod.flying_broom": "Flying Broom",
    "item.hydemod.butterscotch_brew": "Butterscotch Brew",
    "item.hydemod.power_mushroom": "Power Mushroom",
    "item.hydemod.blaze_blossom": "Blaze Blossom",
    "item.hydemod.invincibility_star": "Invincibility Star",
    "item.hydemod.heart_vessel": "Heart Vessel",
    "item.hydemod.grappling_hook": "Grappling Hook",
    "item.hydemod.bomb": "Bomb",
    "item.hydemod.ocarina": "Ocarina of Day and Night",
    "item.hydemod.ring_of_unseeing": "Ring of Unseeing",
    "item.hydemod.elven_blade": "Elven Blade",
    "item.hydemod.mithril_mail": "Mithril Mail",
    "item.hydemod.elven_waybread": "Elven Waybread",
    "item.hydemod.star_phial": "Star Phial",
    "block.hydemod.lamp_post": "Snowy Lamp-Post",
    "entity.hydemod.thrown_bomb": "Bomb",
    "effect.hydemod.super_size": "Super Size",
    "effect.hydemod.star_power": "Star Power",
    "tooltip.hydemod.turkish_delight": "Enchanted sweets from a land of endless winter. Once you start...",
    "tooltip.hydemod.healing_cordial": "One drop heals any wound. Use on yourself, or right-click another creature.",
    "tooltip.hydemod.stone_spell_wand": "The Winter Witch's wand: turns a creature to stone and freezes the water around it.",
    "tooltip.hydemod.wizard_wand": "Sneak + use to change spell. Use to cast.",
    "tooltip.hydemod.spell": "Spell: %s",
    "spell.hydemod.stun": "Stunning Bolt",
    "spell.hydemod.light": "Wand-Light",
    "spell.hydemod.disarm": "Disarm",
    "spell.hydemod.levitate": "Levitate",
    "tooltip.hydemod.invisibility_cloak": "Throw it over yourself to vanish for a minute.",
    "tooltip.hydemod.flying_broom": "Hold right-click to fly where you look.",
    "tooltip.hydemod.butterscotch_brew": "Warm, frothy and sweet. Warms you right through.",
    "tooltip.hydemod.power_mushroom": "Makes you grow huge for a minute: more health, more reach.",
    "tooltip.hydemod.blaze_blossom": "A flower that throws fireballs.",
    "tooltip.hydemod.invincibility_star": "Twelve seconds of invincibility. Anything hostile you touch goes flying.",
    "tooltip.hydemod.heart_vessel": "Permanently adds one heart (up to ten extra).",
    "tooltip.hydemod.grappling_hook": "Fires a chain at a block and yanks you to it.",
    "tooltip.hydemod.bomb": "Light it and throw it. Bounces once, then BOOM.",
    "tooltip.hydemod.ocarina": "Play the tune to swap day and night.",
    "tooltip.hydemod.ring_of_unseeing": "Hold it to vanish from sight and slip away from monsters. It gnaws at you.",
    "tooltip.hydemod.elven_blade": "Glows blue when monsters are near.",
    "tooltip.hydemod.mithril_mail": "Light as a feather, hard as dragon scales.",
    "tooltip.hydemod.elven_waybread": "One bite fills a grown man's stomach.",
    "tooltip.hydemod.star_phial": "A light for dark places. Blinds and scatters nearby monsters.",
    "message.hydemod.heart_vessel": "Your heart grows stronger! (+%s extra hearts)",
    "message.hydemod.heart_vessel.max": "Your heart can't grow any stronger.",
    "message.hydemod.ocarina.day": "The sun rises to your tune.",
    "message.hydemod.ocarina.night": "Night falls to your tune.",
    "call.hydemod.elephant_mario.name": "Elephant Mario",
    "call.hydemod.elephant_mario.calling": "incoming call",
}
lang_path = f"{A}/lang/en_us.json"
L = json.load(open(lang_path)); L.update(realm_lang); w(lang_path, L)

shapeless("turkish_delight", ["minecraft:sugar", "minecraft:sugar", "minecraft:sweet_berries", "minecraft:snowball"], f"{M}:turkish_delight", 4)
shaped("healing_cordial", [" G ", "GRG", " G "], {"G": "minecraft:gold_nugget", "R": "minecraft:glistering_melon_slice"}, f"{M}:healing_cordial")
shaped("stone_spell_wand", ["  I", " S ", "S  "], {"I": "minecraft:blue_ice", "S": "minecraft:stick"}, f"{M}:stone_spell_wand", cat="equipment")
shaped("wizard_wand", ["  F", " S ", "S  "], {"F": "minecraft:feather", "S": "minecraft:blaze_rod"}, f"{M}:wizard_wand", cat="equipment")
shaped("invisibility_cloak", ["WPW", "WGW", "WWW"], {"W": "minecraft:white_wool", "P": "minecraft:phantom_membrane", "G": "minecraft:glass"}, f"{M}:invisibility_cloak", cat="equipment")
shaped("flying_broom", ["  S", " S ", "W  "], {"S": "minecraft:stick", "W": "minecraft:wheat"}, f"{M}:flying_broom", cat="equipment")
shaped("flying_broom_feather", ["  S", " S ", "WF "], {"S": "minecraft:stick", "W": "minecraft:wheat", "F": "minecraft:feather"}, f"{M}:flying_broom", cat="equipment")
shapeless("butterscotch_brew", ["minecraft:glass_bottle", "minecraft:sugar", "minecraft:milk_bucket", "minecraft:honey_bottle"], f"{M}:butterscotch_brew")
shapeless("power_mushroom", ["minecraft:red_mushroom", "minecraft:glowstone_dust", "minecraft:golden_carrot"], f"{M}:power_mushroom")
shaped("blaze_blossom", [" B ", " P ", " S "], {"B": "minecraft:blaze_powder", "P": "minecraft:poppy", "S": "minecraft:stick"}, f"{M}:blaze_blossom", cat="equipment")
shaped("invincibility_star", ["GNG", "NSN", "GNG"], {"G": "minecraft:glowstone_dust", "N": "minecraft:gold_nugget", "S": "minecraft:nether_star"}, f"{M}:invincibility_star", 4)
shaped("heart_vessel", ["GDG", "DAD", "GDG"], {"G": "minecraft:gold_ingot", "D": "minecraft:diamond", "A": "minecraft:enchanted_golden_apple"}, f"{M}:heart_vessel")
shaped("grappling_hook", [" II", " CI", "C  "], {"I": "minecraft:iron_ingot", "C": "minecraft:iron_chain"}, f"{M}:grappling_hook", cat="equipment")
shapeless("bomb", ["minecraft:gunpowder", "minecraft:gunpowder", "minecraft:iron_nugget", "minecraft:string"], f"{M}:bomb", 2)
shaped("ocarina", [" C ", "CNC", " C "], {"C": "minecraft:clay_ball", "N": "minecraft:note_block"}, f"{M}:ocarina")
shaped("ring_of_unseeing", [" G ", "GEG", " G "], {"G": "minecraft:gold_ingot", "E": "minecraft:ender_eye"}, f"{M}:ring_of_unseeing")
shaped("elven_blade", [" D ", " L ", " S "], {"D": "minecraft:diamond", "L": "minecraft:lapis_block", "S": "minecraft:stick"}, f"{M}:elven_blade", cat="equipment")
shaped("mithril_mail", ["N N", "NDN", "NNN"], {"N": "minecraft:iron_nugget", "D": "minecraft:diamond_block"}, f"{M}:mithril_mail", cat="equipment")
shapeless("elven_waybread", ["minecraft:bread", "minecraft:honey_bottle", "minecraft:oak_leaves", "minecraft:golden_apple"], f"{M}:elven_waybread", 3)
shaped("star_phial", [" G ", "GLG", " G "], {"G": "minecraft:glass", "L": "minecraft:sea_lantern"}, f"{M}:star_phial")
shaped("lamp_post", ["III", " L ", " I "], {"I": "minecraft:iron_bars", "L": "minecraft:lantern"}, f"{M}:lamp_post", cat="building")
tag("hydemod/tags/item/repairs_mithril", ["minecraft:diamond"])

# ================================================================== retro RPG homage items
RETRO_SIMPLE = ["reflector_badge", "healing_butterfly", "flip_charm", "moment_stopwatch", "chrono_pendant"]
RETRO_HANDHELD = ["slugger_bat", "spin_hammer"]
for name in RETRO_SIMPLE + RETRO_HANDHELD:
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/handheld" if name in RETRO_HANDHELD else "minecraft:item/generated",
                                        "textures": {"layer0": f"{M}:item/{name}"}})
    w(f"{A}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{M}:item/{name}"}})
L = json.load(open(lang_path)); L.update({
    "item.hydemod.slugger_bat": "Slugger Bat",
    "item.hydemod.reflector_badge": "Reflector Badge",
    "item.hydemod.healing_butterfly": "Healing Butterfly",
    "item.hydemod.spin_hammer": "Spin Hammer",
    "item.hydemod.flip_charm": "Flip Charm",
    "item.hydemod.moment_stopwatch": "Moment Stopwatch",
    "item.hydemod.chrono_pendant": "Chrono Pendant",
    "tooltip.hydemod.slugger_bat": "A trusty baseball bat. Sometimes you hit a home run. SMAAAASH!",
    "tooltip.hydemod.reflector_badge": "Keep it anywhere in your inventory: half of projectile damage bounces back at the shooter.",
    "tooltip.hydemod.healing_butterfly": "Let it go: heals you fully and shakes off poison and weakness.",
    "tooltip.hydemod.spin_hammer": "Slam the ground: a shockwave flips everything nearby into the air.",
    "tooltip.hydemod.flip_charm": "Flip between dimensions to slip through up to 3 blocks of wall.",
    "tooltip.hydemod.moment_stopwatch": "Stops time for everything around you for five seconds.",
    "tooltip.hydemod.chrono_pendant": "Keep it with you: a fatal blow rewinds you 10 seconds instead (once every 5 minutes).",
    "message.hydemod.smash": "SMAAAASH!!",
    "message.hydemod.flip.blocked": "There's nowhere to flip to.",
    "message.hydemod.chrono_rewind": "Time rewinds... you get a second chance.",
}); w(lang_path, L)
shaped("slugger_bat", ["  P", " P ", "S  "], {"P": "minecraft:oak_planks", "S": "minecraft:string"}, f"{M}:slugger_bat", cat="equipment")
shaped("reflector_badge", [" G ", "GLG", " G "], {"G": "minecraft:gold_ingot", "L": "minecraft:lightning_rod"}, f"{M}:reflector_badge")
shapeless("healing_butterfly", ["minecraft:glass_bottle", "minecraft:pink_petals", "minecraft:glistering_melon_slice"], f"{M}:healing_butterfly")
shaped("spin_hammer", ["III", "ISI", " S "], {"I": "minecraft:iron_block", "S": "minecraft:stick"}, f"{M}:spin_hammer", cat="equipment")
shaped("flip_charm", [" A ", "AEA", " A "], {"A": "minecraft:amethyst_shard", "E": "minecraft:ender_pearl"}, f"{M}:flip_charm")
shaped("moment_stopwatch", [" G ", "GCG", " G "], {"G": "minecraft:gold_ingot", "C": "minecraft:clock"}, f"{M}:moment_stopwatch")
shaped("chrono_pendant", [" S ", "SDS", " T "], {"S": "minecraft:string", "D": "minecraft:diamond", "T": "minecraft:totem_of_undying"}, f"{M}:chrono_pendant")

print("data written")
