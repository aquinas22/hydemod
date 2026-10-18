#!/usr/bin/env python3
"""Generate original Minecraft skins, item artwork, recipes and newsroom translations."""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/hydemod"


def write(relative, value):
    path = RES / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n")


def skin(name, suit, hair, tie, peter=False):
    im = Image.new("RGBA", (64, 64))
    d = ImageDraw.Draw(im)
    face, shadow = "#e5bea2", "#bd937d"
    # Standard 64x64 player UVs, including independent left arm/leg surfaces.
    d.rectangle((0, 0, 31, 15), fill=face)
    d.rectangle((0, 0, 31, 7), fill=hair)
    d.rectangle((0, 8, 7, 15), fill=hair)
    d.rectangle((24, 8, 31, 15), fill=hair)
    for x in (8, 16):
        d.rectangle((x, 8, x + 7, 9), fill=hair)
    if peter:
        d.rectangle((10, 8, 13, 10), fill=face)
        d.rectangle((8, 11, 10, 12), outline="#3e383a")
        d.rectangle((13, 11, 15, 12), outline="#3e383a")
        d.point((11, 11), fill="#3e383a")
        d.point((12, 11), fill="#3e383a")
    else:
        for x, y in ((9, 10), (12, 9), (14, 10), (7, 9), (17, 8), (11, 7)):
            d.point((x, y), fill=hair)
        d.line((9, 10, 10, 10), fill=hair)
        d.line((13, 10, 14, 10), fill=hair)
    d.point((9, 11), fill="#3a4a52")
    d.point((14, 11), fill="#3a4a52")
    d.line((11, 14, 13, 14), fill=shadow)
    d.rectangle((16, 16, 39, 31), fill=suit)
    d.polygon([(22, 20), (25, 20), (25, 27), (24, 29), (22, 26)], fill="#f0ebe4")
    d.line((24, 21, 24, 27), fill=tie, width=1)
    d.line((20, 21, 22, 25), fill="#5c6674")
    d.line((27, 21, 25, 25), fill="#5c6674")
    d.point((28, 23), fill="#b8bfc8")
    d.point((24, 30), fill="#a8a5a3")
    for rect in ((40, 16, 55, 31), (32, 48, 47, 63)):
        d.rectangle(rect, fill=suit)
        d.rectangle((rect[0], rect[1] + 13, rect[2], rect[3]), fill=face)
        d.line((rect[0], rect[1] + 12, rect[2], rect[1] + 12), fill="#eee7df")
    for rect in ((0, 16, 15, 31), (16, 48, 31, 63)):
        d.rectangle(rect, fill=suit)
        d.rectangle((rect[0], rect[1] + 14, rect[2], rect[3]), fill="#191a21")
        d.line((rect[0], rect[1] + 5, rect[0] + 3, rect[1] + 5), fill="#5d6672")
    path = ASSETS / f"textures/entity/{name}.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path)


def icon(name):
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    if name == "stretched_twig_of_peace":
        d.line((2, 15, 12, 0), fill="#503723", width=3)
        d.line((3, 14, 12, 1), fill="#ad8050", width=1)
        d.line((8, 7, 5, 3), fill="#805731", width=2)
        d.ellipse((3, 1, 6, 4), fill="#77ac5f")
        d.ellipse((11, 0, 14, 3), fill="#a5c37d")
    elif name == "day_today_press_pass":
        d.line((5, 0, 7, 4, 11, 0), fill="#547fa9", width=2)
        d.rectangle((2, 4, 13, 14), fill="#dbdee5", outline="#44516b")
        d.rectangle((3, 5, 12, 7), fill="#952b40")
        d.rectangle((4, 9, 7, 12), fill="#526d95")
        d.line((9, 9, 11, 9), fill="#515c75")
        d.line((9, 11, 11, 11), fill="#515c75")
    elif name == "satellite_infomometer":
        d.rectangle((3, 6, 12, 14), fill="#535d70", outline="#252b3b")
        d.rectangle((5, 8, 10, 11), fill="#86debe")
        d.line((8, 7, 8, 3), fill="#a8b7c9", width=2)
        d.arc((3, 0, 13, 7), 0, 180, fill="#c5ced9", width=2)
        d.point((8, 12), fill="#f2af5f")
    elif name == "news_microphone":
        d.line((5, 14, 11, 5), fill="#44495a", width=3)
        d.rectangle((7, 6, 12, 10), fill="#e0e3e9", outline="#555c70")
        d.rectangle((8, 7, 11, 8), fill="#ac364b")
        d.ellipse((8, 0, 14, 6), fill="#303b4c", outline="#718299")
        d.line((10, 1, 12, 1), fill="#a2acba")
    elif name == "fact_bomb":
        d.ellipse((3, 4, 13, 14), fill="#3e455e", outline="#1b243b")
        d.rectangle((4, 8, 12, 10), fill="#dddae8")
        d.line((6, 9, 10, 9), fill="#91364b")
        d.line((9, 4, 10, 1, 13, 2), fill="#a98b58", width=2)
        d.point((13, 1), fill="#faf0a6")
    elif name == "this_is_our_war":
        d.rectangle((1, 3, 14, 12), fill="#242636", outline="#101625")
        d.rectangle((3, 5, 12, 8), fill="#d8dbe0")
        d.ellipse((3, 5, 5, 7), fill="#39404f")
        d.ellipse((10, 5, 12, 7), fill="#39404f")
        d.line((6, 6, 9, 6), fill="#ac344d")
        d.rectangle((3, 10, 12, 11), fill="#4d596e")
    elif name == "unverified_report":
        d.rectangle((3, 1, 12, 14), fill="#e7dfc6", outline="#ab9c85")
        for y in (3, 6, 9, 12): d.line((5, y, 10, y), fill="#837e7c")
        d.line((8, 6, 10, 8), fill="#bc4853", width=2)
        d.line((10, 6, 8, 8), fill="#bc4853", width=2)
    else:
        peter = name.startswith("peter")
        d.ellipse((3, 1, 12, 14), fill="#697488" if peter else "#303a52", outline="#1b2737")
        for box in ((5, 3, 7, 5), (8, 7, 10, 9), (4, 10, 6, 12)):
            d.ellipse(box, fill="#d0b290" if peter else "#b64658")
    path = ASSETS / f"textures/item/{name}.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path)
    model = {"parent": "minecraft:item/handheld" if name in ("stretched_twig_of_peace", "news_microphone") else "minecraft:item/generated",
             "textures": {"layer0": f"hydemod:item/{name}"}}
    if name == "stretched_twig_of_peace":
        model["display"] = {"thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4, .5], "scale": [.7, 1.5, .7]},
                            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1, 3, 1], "scale": [1, 1.6, 1]}}
    write(f"assets/hydemod/models/item/{name}.json", model)
    write(f"assets/hydemod/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"hydemod:item/{name}"}})


def main():
    skin("peter_ohanrahan", "#69707f", "#8c745b", "#65528a", True)
    skin("chris_morris", "#29384d", "#342823", "#96364b")
    names = {"day_today_press_pass": "The Day Today Press Pass", "stretched_twig_of_peace": "Stretched Twig of Peace",
             "satellite_infomometer": "Satellite Infomometer", "news_microphone": "The Day Today Microphone",
             "fact_bomb": "Fact Bomb", "this_is_our_war": "This Is Our War VHS", "unverified_report": "Unverified Report",
             "peter_ohanrahan_spawn_egg": "Peter O'Hanraha-hanrahan Spawn Egg", "chris_morris_spawn_egg": "Chris Morris Spawn Egg"}
    for name in names: icon(name)
    recipes = {
        "day_today_press_pass": ["minecraft:paper", "minecraft:paper", "minecraft:string", "minecraft:red_dye"],
        "stretched_twig_of_peace": ["minecraft:stick", "minecraft:stick", "minecraft:stick", "minecraft:vine", "minecraft:white_dye"],
        "satellite_infomometer": ["minecraft:compass", "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:amethyst_shard", "minecraft:redstone"],
        "news_microphone": ["minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:redstone", "minecraft:white_wool"],
        "fact_bomb": ["minecraft:glowstone_dust", "minecraft:paper", "minecraft:ink_sac", "minecraft:gunpowder"],
        "this_is_our_war": ["minecraft:paper", "minecraft:paper", "minecraft:black_dye", "minecraft:redstone"]}
    for name, ingredients in recipes.items():
        write(f"data/hydemod/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ingredients,
              "result": {"id": "hydemod:" + name, "count": 2 if name == "fact_bomb" else 1}})
        write(f"data/hydemod/advancement/recipes/{name}.json", {"parent": "minecraft:recipes/root", "criteria": {
              "ingredient": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": [ingredients[0]]}]}},
              "recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": "hydemod:" + name}}},
              "requirements": [["ingredient", "recipe"]], "rewards": {"recipes": ["hydemod:" + name]}})
    lang_path = ASSETS / "lang/en_us.json"
    lang = json.loads(lang_path.read_text())
    lang.update({"item.hydemod." + key: value for key, value in names.items()})
    lang.update({"entity.hydemod.peter_ohanrahan": "Peter O'Hanraha-hanrahan", "entity.hydemod.chris_morris": "Chris Morris",
        "tooltip.hydemod.day_today_press_pass": "Use on clear ground for Peter; sneak-use for Chris. Reusable. One of each nearby in survival.",
        "tooltip.hydemod.stretched_twig_of_peace": "Use: Weakness and Slowness for hostiles within 10 blocks, for 8s. Cooldown 15s; wears twice as fast in the Nether.",
        "tooltip.hydemod.satellite_infomometer": "Use for your coordinates, dimension, weather and hostile count within 32 blocks.",
        "tooltip.hydemod.news_microphone": "Use to broadcast your position to players within 32 blocks. Cooldown 10s.",
        "tooltip.hydemod.fact_bomb": "Use to reveal hostiles within 16 blocks for 10s. Consumed. Flash and particles; no blast damage.",
        "tooltip.hydemod.this_is_our_war": "A souvenir VHS from an overexcited newsroom. Use to read its original archive synopsis.",
        "tooltip.hydemod.unverified_report": "Obtained by giving paper to a reporter. Use to read a very doubtful field report.",
        "message.hydemod.news.no_room": "The outside broadcast needs clear, dry ground and room for a reporter.",
        "message.hydemod.news.already_here": "That correspondent is already covering this area. Speak to them first.",
        "message.hydemod.news.arrival": "%s has arrived for the outside broadcast.",
        "message.hydemod.news.editor": "Chris: A report on paper. Excellent. The newsroom still requires evidence.",
        "message.hydemod.news.copy": "Peter: My field report. The figures are provisional, as is my location.",
        "message.hydemod.news.twig_response": "The twig has brought a welcome pause to the interview. The studio is checking its structural integrity.",
        "message.hydemod.news.peter.0": "Peter: I can confirm this area contains at least forty creepers. I have counted none of them personally.",
        "message.hydemod.news.peter.1": "Peter: The local emerald quota is thirty percent. My calculator has requested a taxi.",
        "message.hydemod.news.peter.2": "Peter: I am reporting from the scene. The scene may be behind me. Or in another dimension.",
        "message.hydemod.news.peter.3": "Peter: Every villager supports my findings. I conducted the interviews in fluent doorbell.",
        "message.hydemod.news.chris.0": "Chris: Before we go live, establish three things: the place, the number, and whether Peter is actually there.",
        "message.hydemod.news.chris.1": "Chris: Our satellite instruments show weather. The newsroom has classified it as an exclusive.",
        "message.hydemod.news.chris.2": "Chris: Keep the microphone pointed at the interviewee. The ground has already declined to comment.",
        "message.hydemod.news.chris.3": "Chris: We have prepared an emergency bulletin for an event which has yet to agree to happen.",
        "message.hydemod.news.correction": "Chris: Peter, the infomometer is beside you. Please measure something before giving it a number.",
        "message.hydemod.news.peace": "The twig lowers the temperature of the debate: %s hostiles weakened and slowed.",
        "message.hydemod.news.melting": "The twig is at melting point: %s hostiles weakened and slowed. Two durability used.",
        "message.hydemod.news.reading": "Infomometer: X %s / Y %s / Z %s | %s | %s hostiles within 32 blocks.",
        "message.hydemod.news.clear": "Weather desk: clear skies. The emergency graphic will have to wait.",
        "message.hydemod.news.rain": "Weather desk: rain. The outside broadcast has misplaced its umbrella.",
        "message.hydemod.news.thunder": "Weather desk: thunder. The newsroom has put a tie on the lightning.",
        "message.hydemod.news.live": "The Day Today: %s reports live from X %s, Z %s. Facts subject to verification.",
        "message.hydemod.news.fact_bomb": "Fact Bomb: the camera flash exposes nearby hostiles. No terrain was harmed in this bulletin.",
        "message.hydemod.news.archive": "Archive synopsis: a presenter inflates a diplomatic dispute into a broadcast spectacle, then packages the coverage as a souvenir. Original game text; no programme recording is included.",
        "message.hydemod.news.unverified": "FIELD REPORT: Thirty percent of local creepers reject the new emerald quota. Source: a man who mistook a furnace for a finance minister. Status: entirely unverified."})
    lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n")
    print("Generated two reporter skins, nine item models/textures, six recipes and newsroom translations.")


if __name__ == "__main__": main()
