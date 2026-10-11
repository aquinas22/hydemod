"""Generates every HydeMod texture as original pixel art. Run: python3 tools/gen_textures.py"""
import os, random, math
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "hydemod", "textures")
rnd = random.Random(2070)

def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)

def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3],)

def mix(a, b, t):
    return tuple(int(a[i] * (1 - t) + b[i] * t) for i in range(3)) + (255,)

def new(w=16, h=16):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))

def save(img, path):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    img.save(full)

def noise_fill(img, box, base, var=0.12, seed=None):
    r = random.Random(seed) if seed is not None else rnd
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            img.putpixel((x, y), shade(base, 1 + r.uniform(-var, var)))

def px(img, pts, c):
    for p in pts:
        img.putpixel(p, c)

def draw_rows(img, rows, palette, ox=0, oy=0):
    """rows: list of strings; palette: char -> color (None/'.' = transparent)."""
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in palette and palette[ch] is not None:
                img.putpixel((ox + x, oy + y), palette[ch])

def outline(img, color=(0, 0, 0, 255)):
    """Adds a 1px dark outline around opaque pixels (item style)."""
    w, h = img.size
    src = img.copy()
    for y in range(h):
        for x in range(w):
            if src.getpixel((x, y))[3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < w and 0 <= ny < h and src.getpixel((nx, ny))[3] > 0:
                        img.putpixel((x, y), color)
                        break
    return img

# ------------------------------------------------------------------ base stones
def stone(seed=1):
    img = new(); noise_fill(img, (0, 0, 16, 16), hexc("7f7f7f"), 0.1, seed)
    r = random.Random(seed)
    for _ in range(14):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc("6a6a6a"))
    return img

def deepslate(seed=2):
    img = new(); noise_fill(img, (0, 0, 16, 16), hexc("4d4d52"), 0.1, seed)
    for y in range(0, 16, 4):
        for x in range(16):
            if (x + y) % 7: img.putpixel((x, y), hexc("3d3d42"))
    return img

def netherrack(seed=3):
    img = new(); noise_fill(img, (0, 0, 16, 16), hexc("6f3434"), 0.18, seed)
    r = random.Random(seed)
    for _ in range(20):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc("4f2222"))
    return img

def end_stone(seed=4):
    img = new(); noise_fill(img, (0, 0, 16, 16), hexc("dbdea0"), 0.07, seed)
    r = random.Random(seed)
    for _ in range(16):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc("c5c98c"))
    return img

CRYSTAL_SPOTS = [(3, 3), (4, 3), (3, 4), (4, 2), (11, 5), (12, 5), (12, 4), (11, 6), (6, 10), (7, 10), (7, 11), (7, 9),
                 (12, 12), (13, 12), (12, 13), (2, 12), (9, 2)]

def kyber_ore(base, color):
    img = base
    hi = shade(color, 1.35); mid = color; lo = shade(color, 0.65)
    groups = [[(3, 3), (4, 3), (3, 4), (4, 2), (4, 4)], [(11, 5), (12, 5), (12, 4), (11, 6), (12, 6)],
              [(6, 10), (7, 10), (7, 11), (7, 9), (6, 11)], [(12, 12), (13, 12), (12, 13), (13, 11)], [(2, 12), (2, 13), (3, 12)], [(9, 2), (9, 1)]]
    for g in groups:
        for i, p in enumerate(g):
            img.putpixel(p, hi if i == 0 else (mid if i % 2 else lo))
    return img

KYBER = {"green": hexc("3ee85a"), "blue": hexc("3d8bff"), "red": hexc("ff3030"), "purple": hexc("b14dff")}

# ------------------------------------------------------------------ items
def crystal_item(color):
    img = new()
    hi = shade(color, 1.5); mid = color; lo = shade(color, 0.6); dk = shade(color, 0.35)
    rows = [
        "......h.........",
        ".....hhm........",
        ".....hmm........",
        "....hhmml.......",
        "....hmmml.......",
        "...hhmmmll......",
        "...hmmmmll......",
        "...hmmmmll.h....",
        "..hhmmmmlll hm..",
        "..hmmmmmllldmm..",
        "..hmmmmmlll.ml..",
        "..hmmmmmlll.dl..",
        "...hmmmmll..d...",
        "....hmmml.......",
        ".....dddd.......",
        "................",
    ]
    draw_rows(img, rows, {"h": hi, "m": mid, "l": lo, "d": dk, ".": None, " ": None})
    return outline(img, shade(color, 0.2))

def lightsaber_hilt_item():
    img = new()
    met = hexc("c9ccd1"); dk = hexc("5a5e66"); blk = hexc("1d1f24"); red = hexc("d4202a"); gold = hexc("c8a040")
    # diagonal hilt from bottom-left to top-right
    for i in range(11):
        x, y = 3 + i, 13 - i
        for t in (-1, 0, 1):
            c = met if t == 0 else (dk if t == 1 else hexc("e6e8ec"))
            img.putpixel((x + (t if t > 0 else 0), y + (0 if t > 0 else t)), c)
    for i in (3, 5, 7):
        img.putpixel((3 + i, 13 - i), blk); img.putpixel((3 + i + 1, 13 - i), blk)
    img.putpixel((11, 5), red); img.putpixel((12, 3), gold); img.putpixel((13, 2), gold)
    return outline(img, hexc("15161a"))

def trash_cube():
    img = new()
    base = hexc("6b5a3e")
    noise_fill(img, (2, 2, 14, 14), base, 0.25)
    junk = [hexc("8fae3a"), hexc("c23b3b"), hexc("dcdcdc"), hexc("3a6bb0"), hexc("b0893a"), hexc("2f2f2f")]
    for _ in range(30):
        img.putpixel((rnd.randrange(3, 13), rnd.randrange(3, 13)), rnd.choice(junk))
    for i in range(2, 14):
        img.putpixel((i, 2), shade(base, 1.3)); img.putpixel((2, i), shade(base, 1.2))
        img.putpixel((i, 13), shade(base, 0.6)); img.putpixel((13, i), shade(base, 0.7))
    # strapping wire
    for i in range(2, 14):
        img.putpixel((i, 7), hexc("9a9a9a")); img.putpixel((7, i), hexc("9a9a9a"))
    return outline(img, hexc("2a2118"))

def nugget(color):
    img = new()
    rows = ["................",
            "................",
            "................",
            "................",
            "................",
            "......hh........",
            ".....hmmm.......",
            ".....mmmll..hh..",
            "......mll..hmml.",
            "...........mmll.",
            "....hhm.....ll..",
            "...hmmml........",
            "...mmmll........",
            "....lll.........",
            "................",
            "................"]
    draw_rows(img, rows, {"h": shade(color, 1.4), "m": color, "l": shade(color, 0.65), ".": None})
    return outline(img, shade(color, 0.3))

def ingot(color):
    img = new()
    rows = ["................",
            "................",
            "................",
            "................",
            "................",
            "........hhhhh...",
            "......hhmmmmmh..",
            "....hhmmmmmmml..",
            "..hhmmmmmmmmll..",
            "..hmmmmmmmmll...",
            "..mmmmmmmmll....",
            "..lmmmmmmll.....",
            "...llllll.......",
            "................",
            "................",
            "................"]
    draw_rows(img, rows, {"h": shade(color, 1.45), "m": color, "l": shade(color, 0.6), ".": None})
    return outline(img, shade(color, 0.25))

NEOPLAS = hexc("38d6c8")

def veggie(body, leaf, shape):
    img = new()
    if shape == "beet":
        rows = ["......ll.l......",
                ".....l.ll.l.....",
                "......lll.......",
                ".......l........",
                "......bbb.......",
                ".....bbbbb......",
                "....bhbbbbb.....",
                "....bhbbbbd.....",
                "....bbbbbbd.....",
                ".....bbbbd......",
                "......bbd.......",
                ".......d........",
                ".......d........",
                "................",
                "................",
                "................"]
    elif shape == "yam":
        rows = ["................",
                "................",
                "..........l.....",
                ".........ll.....",
                "......bbbb......",
                "....bhbbbbbb....",
                "...bhbbbbbbbd...",
                "..bhbbbbbbbbd...",
                "..bbbbbbbbbbd...",
                "...bbbbbbbbd....",
                "....bbbbbbd.....",
                "......dddd......",
                "................",
                "................",
                "................",
                "................"]
    else:  # cabbage
        rows = ["................",
                "................",
                "......llll......",
                "....lllbblll....",
                "...llbbhbbbll...",
                "..llbbhbbbbbll..",
                "..lbbhbbbbbbbl..",
                "..lbbbbbbbbbbl..",
                "..lbbbbbbbbbdl..",
                "..llbbbbbbbdll..",
                "...llbbbbddll...",
                "....llldddll....",
                "......llll......",
                "................",
                "................",
                "................"]
    draw_rows(img, rows, {"b": body, "h": shade(body, 1.4), "d": shade(body, 0.6), "l": leaf, ".": None})
    # sea sparkle
    img.putpixel((13, 3), hexc("bff4ff")); img.putpixel((2, 14), hexc("bff4ff"))
    return outline(img, hexc("10202a"))

def bowl_food(fill_colors, top=None):
    img = new()
    bowl = hexc("8a5a2b")
    for y in range(9, 14):
        w = 6 - (y - 9)
        for x in range(8 - w - 1, 8 + w + 1):
            img.putpixel((x, y), shade(bowl, 1.15 if x < 8 else 0.85))
    for y in range(6, 9):
        for x in range(2, 14):
            img.putpixel((x, y), rnd.choice(fill_colors))
    for x in range(1, 15):
        img.putpixel((x, 9), shade(bowl, 1.3))
    return outline(img, hexc("2a1a0a"))

def baked_potato_cheesy():
    img = new()
    pot = hexc("b58a4a"); cheese = hexc("ffd23f"); kelp = hexc("2f7d3a")
    rows = ["................",
            "................",
            "................",
            "................",
            "....cccccc......",
            "..cccccccccc....",
            ".ckccccckcccc...",
            ".pcccccccccccp..",
            ".pppcppcpccppp..",
            ".ppppppppppppd..",
            "..pppppppppppd..",
            "...ppppppppdd...",
            ".....dddddd.....",
            "................",
            "................",
            "................"]
    draw_rows(img, rows, {"c": cheese, "k": kelp, "p": pot, "d": shade(pot, 0.6), ".": None})
    img.putpixel((3, 5), hexc("fff4a0")); img.putpixel((7, 4), hexc("fff4a0"))
    return outline(img, hexc("2a1a0a"))

def bottle(liquid, cap=None):
    img = new()
    glass = hexc("d9f2ff", 150); gdk = hexc("9cc4d6", 200)
    rows = ["................",
            "......cccc......",
            "......gggg......",
            ".......gg.......",
            ".......gg.......",
            "......gllg......",
            ".....glllLg.....",
            "....gllllllg....",
            "....glllllLg....",
            "....glllllLg....",
            "....glllllLg....",
            "....gllllLLg....",
            ".....gLLLLg.....",
            "......gggg......",
            "................",
            "................"]
    draw_rows(img, rows, {"g": gdk, "l": liquid, "L": shade(liquid, 0.7), "c": cap or hexc("8a5a2b"), ".": None})
    img.putpixel((6, 7), hexc("ffffff", 200)); img.putpixel((6, 8), hexc("ffffff", 160))
    return outline(img, hexc("1a2a30"))

def microphone():
    img = new()
    grille = hexc("a9adb5"); gdk = hexc("6c7079"); body = hexc("1e1f24"); red = hexc("e62b1e")
    rows = ["...........ggg..",
            "..........gGgGg.",
            "..........GgGgG.",
            "..........gGgGg.",
            "...........ggg..",
            "..........rr....",
            ".........bb.....",
            "........bb......",
            ".......bb.......",
            "......bb........",
            ".....bb.........",
            "....bb..........",
            "...bb...........",
            "..bb............",
            "................",
            "................"]
    draw_rows(img, rows, {"g": grille, "G": gdk, "b": body, "r": red, ".": None})
    return outline(img, hexc("0a0a0a"))

def hand():
    img = new()
    skin = hexc("e8b48a"); sdk = hexc("b9835c"); cuff = hexc("2f3a5a")
    rows = ["................",
            "......s.s.......",
            ".....ssssss.....",
            "....sssssss.s...",
            "....ssssssssss..",
            "....sssssssss...",
            "....ssssssss....",
            "....sssssssd....",
            ".....sssssdd....",
            ".....sssssd.....",
            ".....ccccc......",
            ".....ccccc......",
            ".....ccccc......",
            "................",
            "................",
            "................"]
    draw_rows(img, rows, {"s": skin, "d": sdk, "c": cuff, ".": None})
    # little star = "good job"
    px(img, [(1, 2), (2, 1), (2, 2), (2, 3), (3, 2)], hexc("ffe14a"))
    return outline(img, hexc("3a2210"))

def blackberry_phone():
    img = new()
    body = hexc("2a2c31"); scr = hexc("5ac8ff"); key = hexc("8a8f99")
    for y in range(2, 15):
        for x in range(4, 12):
            img.putpixel((x, y), body)
    for y in range(3, 8):
        for x in range(5, 11):
            img.putpixel((x, y), scr if (x + y) % 5 else hexc("a8e6ff"))
    for y in (9, 11, 13):
        for x in (5, 7, 9):
            img.putpixel((x, y), key)
    img.putpixel((10, 2), hexc("ff3a3a"))  # blinking LED
    return outline(img, hexc("0a0a0a"))

def clock_item():
    img = new()
    rim = hexc("d4a72c"); face = hexc("f4f0e0"); hand = hexc("222222")
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 6.6: img.putpixel((x, y), face)
            elif d < 7.6: img.putpixel((x, y), rim if x + y < 16 else shade(rim, 0.7))
    for i in range(4): img.putpixel((7, 7 - i), hand)
    for i in range(4): img.putpixel((8 + i, 8), hexc("c02020"))
    img.putpixel((7, 8), hand)
    # circular arrow = reset
    px(img, [(2, 1), (3, 1), (1, 2), (1, 3)], hexc("36c24a"))
    return outline(img, hexc("2a1a00"))

def maglev():
    img = new()
    rail = hexc("9aa3ad"); mag = hexc("d83a3a"); blue = hexc("3a6bd8"); spark = hexc("aef3ff")
    for i in range(12):
        img.putpixel((2 + i, 13 - i), rail)
        img.putpixel((3 + i, 13 - i), shade(rail, 0.7))
    for i in range(0, 12, 3):
        img.putpixel((2 + i, 12 - i), mag); img.putpixel((3 + i, 11 - i), blue)
    # moon
    for y in range(0, 6):
        for x in range(10, 16):
            if math.hypot(x - 12.5, y - 2.5) < 2.7: img.putpixel((x, y), hexc("f2f0d8"))
    img.putpixel((12, 2), hexc("c8c4a8")); img.putpixel((13, 3), hexc("c8c4a8"))
    px(img, [(1, 9), (4, 6), (7, 3)], spark)
    return outline(img, hexc("15161a"))

def soda_stream():
    img = new()
    body = hexc("eaeaea"); dk = hexc("9a9a9a"); blk = hexc("222222"); bot = hexc("8fd3ff", 200)
    rows = ["................",
            "....kkkkkk......",
            "...kwwwwwwk.....",
            "...kwkkkkwk.....",
            "...kw....wk.....",
            "...kw.bb.wk.....",
            "...kw.bb.wk.....",
            "...kw.bb.wk.....",
            "...kw.bb.wk.....",
            "...kw.bb.wk.....",
            "...kwwbbwwk.....",
            "..kwwwwwwwwk....",
            "..kddddddddk....",
            "..kkkkkkkkkk....",
            "................",
            "................"]
    draw_rows(img, rows, {"k": blk, "w": body, "d": dk, "b": bot, ".": None})
    return img

def book(color):
    img = new()
    cov = color; page = hexc("f3ead2")
    rows = ["................",
            "................",
            "...cccccccccc...",
            "...cCCCCCCCCcp..",
            "...cCCCCCCCCcp..",
            "...cCCyCCCCCcp..",
            "...cCyyyCCCCcp..",
            "...cCCyCCCCCcp..",
            "...cCCCCCCCCcp..",
            "...cCCCCCCCCcp..",
            "...cCCCCCCCCcp..",
            "...cCCCCCCCCcp..",
            "...ccccccccccp..",
            "....pppppppppp..",
            "................",
            "................"]
    draw_rows(img, rows, {"c": shade(cov, 0.6), "C": cov, "p": page, "y": hexc("ffe14a"), ".": None})
    return outline(img, hexc("1a1008"))

def invitation():
    img = new()
    card = hexc("f6f6f6"); red = hexc("e62b1e"); blk = hexc("111111")
    for y in range(3, 13):
        for x in range(1, 15):
            img.putpixel((x, y), card if y > 4 else blk)
    # "TEDx"-ish red block letters (abstract)
    for x in (3, 4, 5): img.putpixel((x, 7), red)
    img.putpixel((4, 8), red); img.putpixel((4, 9), red)
    for y in (7, 8, 9): img.putpixel((7, y), red)
    img.putpixel((8, 7), red); img.putpixel((8, 8), red); img.putpixel((8, 9), red)
    for y in (7, 8, 9): img.putpixel((10, y), red)
    img.putpixel((11, 7), red); img.putpixel((11, 9), red); img.putpixel((12, 8), red)
    for x in range(3, 13): img.putpixel((x, 11), hexc("bbbbbb"))
    return outline(img, hexc("333333"))

def core():
    img = new()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 6.5:
                ang = math.atan2(y - 7.5, x - 7.5)
                t = (math.sin(ang * 3 + d) + 1) / 2
                img.putpixel((x, y), mix(hexc("38d6c8"), hexc("ff4fd8"), t))
    for y in range(5, 11):
        for x in range(5, 11):
            if math.hypot(x - 7.5, y - 7.5) < 2.4: img.putpixel((x, y), hexc("fffbe0"))
    return outline(img, hexc("1a0a2a"))

def goo():
    img = new()
    g = hexc("9a4fd8")
    rows = ["................",
            "................",
            "................",
            "................",
            "......hhh.......",
            ".....hgggg......",
            "....hggggggg....",
            "...hgggggggggd..",
            "...ggggggggggd..",
            "..hgghggggggdd..",
            "..ggggggggggd...",
            "...gggggggddd...",
            "....dddddddd....",
            "................",
            "................",
            "................"]
    draw_rows(img, rows, {"h": shade(g, 1.5), "g": g, "d": shade(g, 0.6), ".": None})
    return outline(img, shade(g, 0.3))

def fruit():
    """Elephant fruit: a round, grey-violet fruit with a trunk-like curly stem and two ear leaves."""
    img = new()
    body = hexc("8f8fb8"); hi = hexc("c9c9ee"); dk = hexc("5c5c80"); leaf = hexc("4caf50")
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 9)
            if d < 5.8:
                img.putpixel((x, y), hi if (x < 6 and y < 8) else (dk if x + y > 20 else body))
    px(img, [(7, 3), (7, 2), (8, 1), (9, 1), (10, 2)], hexc("6b4a2a"))
    px(img, [(4, 3), (5, 3), (4, 4), (3, 4), (10, 3), (11, 3), (11, 4), (12, 4)], leaf)
    return outline(img, hexc("1a1a2a"))

def peanut():
    img = new()
    shell = hexc("d9a760"); dk = hexc("a8763a"); gold = hexc("ffe14a")
    rows = ["................",
            "..........ss....",
            ".........ssss...",
            "........sSsSss..",
            "........sssssd..",
            ".........sssd...",
            "........ssdd....",
            ".......ssd......",
            "......sssss.....",
            ".....sSsSsss....",
            "....sssssssd....",
            "....ssssssdd....",
            ".....ssssdd.....",
            "......dddd......",
            "................",
            "................"]
    draw_rows(img, rows, {"s": shell, "S": dk, "d": dk, ".": None})
    px(img, [(2, 2), (3, 1), (3, 3), (4, 2), (3, 2)], gold)
    return outline(img, hexc("3a2410"))

def wrench():
    img = new()
    met = hexc("b8bec8"); dk = hexc("6c7480"); red = hexc("c0392b")
    for i in range(9):
        img.putpixel((3 + i, 12 - i), red); img.putpixel((4 + i, 12 - i), shade(red, 0.7))
    rows = ["..........mmm...",
            ".........m..m...",
            ".........m...m..",
            "..........mmmm..",
            "...........dd...",]
    draw_rows(img, rows, {"m": met, "d": dk, ".": None}, 0, 1)
    px(img, [(2, 13), (3, 13), (2, 12)], dk)
    return outline(img, hexc("15161a"))

def armor_icon(kind):
    img = new()
    a = NEOPLAS; hi = shade(a, 1.35); dk = shade(a, 0.6); tie = hexc("e62b1e"); suit = hexc("2a3242")
    if kind == "helmet":
        rows = ["................",
                "................",
                "....hhhhhhhh....",
                "...haaaaaaaad...",
                "..haaaaaaaaaad..",
                "..haaddddddaad..",
                "..had......dad..",
                "..had......dad..",
                "..dd........dd..",
                "................"]
    elif kind == "chestplate":
        rows = ["................",
                "..hhh......hhh..",
                "..haah....haad..",
                "..haaasssstaad..",
                "...haassrrsaad..",
                "....haasrrsad...",
                "....haasrrsad...",
                "....haassssad...",
                "....haaaaaaad...",
                "....haaaaaaad...",
                "....haaaaaaad...",
                "....dddddddddd..",
                "................"]
    elif kind == "leggings":
        rows = ["................",
                "....hhhhhhhh....",
                "....haaaaaad....",
                "....haaaaaad....",
                "....haad.had....",
                "....haad.had....",
                "....haad.had....",
                "....haad.had....",
                "....haad.had....",
                "....haad.had....",
                "....dddd.ddd....",
                "................"]
    else:
        rows = ["................",
                "................",
                "................",
                "................",
                "................",
                "...had....had...",
                "...had....had...",
                "...had....had...",
                "..haad...haad...",
                "..haaad..haaad..",
                "..ddddd..ddddd..",
                "................"]
    draw_rows(img, rows, {"h": hi, "a": a, "d": dk, "s": suit, "r": tie, "t": suit, ".": None}, 0, 2)
    return outline(img, shade(a, 0.25))

def spawn_egg(base, spots):
    img = new()
    rows = ["................",
            "......xxxx......",
            ".....xbbbbx.....",
            "....xbbbbbbx....",
            "....xbbbbbbx....",
            "...xbbbbbbbbx...",
            "...xbbbbbbbbx...",
            "..xbbbbbbbbbbx..",
            "..xbbbbbbbbbbx..",
            "..xbbbbbbbbbbx..",
            "..xbbbbbbbbbbx..",
            "..xbbbbbbbbbbx..",
            "...xbbbbbbbbx...",
            "....xbbbbbbx....",
            ".....xxxxxx.....",
            "................"]
    draw_rows(img, rows, {"x": shade(base, 0.4), "b": base, ".": None})
    r = random.Random(sum(base[:3]))
    for _ in range(9):
        x, y = r.randrange(4, 12), r.randrange(3, 13)
        if img.getpixel((x, y))[3] and img.getpixel((x, y)) != shade(base, 0.4):
            img.putpixel((x, y), spots)
    img.putpixel((6, 4), shade(base, 1.4)); img.putpixel((5, 5), shade(base, 1.4))
    return img

# ------------------------------------------------------------------ blocks
def neoplas_block():
    img = new()
    noise_fill(img, (0, 0, 16, 16), NEOPLAS, 0.06)
    for i in range(16):
        img.putpixel((i, 0), shade(NEOPLAS, 1.3)); img.putpixel((0, i), shade(NEOPLAS, 1.2))
        img.putpixel((i, 15), shade(NEOPLAS, 0.6)); img.putpixel((15, i), shade(NEOPLAS, 0.7))
    for (x, y) in ((4, 4), (11, 4), (4, 11), (11, 11)):
        img.putpixel((x, y), hexc("eafffd"))
    for i in range(3, 13):
        img.putpixel((i, 7), shade(NEOPLAS, 0.85)); img.putpixel((7, i), shade(NEOPLAS, 0.85))
    return img

def trash_block():
    img = new()
    noise_fill(img, (0, 0, 16, 16), hexc("6b5a3e"), 0.25)
    junk = [hexc("8fae3a"), hexc("c23b3b"), hexc("dcdcdc"), hexc("3a6bb0"), hexc("b0893a"), hexc("2f2f2f")]
    for _ in range(45):
        img.putpixel((rnd.randrange(16), rnd.randrange(16)), rnd.choice(junk))
    for i in range(16):
        img.putpixel((i, 5), hexc("9a9a9a")); img.putpixel((i, 10), hexc("9a9a9a"))
    return img

PIPE = hexc("2fb34a")

def pipe_side():
    img = new()
    for x in range(16):
        t = x / 15
        c = shade(PIPE, 1.35 - 0.75 * abs(t - 0.3) * 1.6) if x not in (0, 15) else shade(PIPE, 0.45)
        for y in range(16):
            img.putpixel((x, y), c)
    # rim band at top 4px, slightly wider look via darker separator
    for x in range(16):
        img.putpixel((x, 4), shade(PIPE, 0.4))
        for y in range(0, 4):
            img.putpixel((x, y), shade(img.getpixel((x, y + 6)), 1.08))
    img.putpixel((3, 1), hexc("d8ffe0")); img.putpixel((3, 2), hexc("b8f5c4"))
    return img

def pipe_top():
    img = new()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d > 6: img.putpixel((x, y), shade(PIPE, 1.2 if x < 8 else 0.9))
            elif d > 5: img.putpixel((x, y), shade(PIPE, 0.6))
            else: img.putpixel((x, y), hexc("0a0f0b") if d < 4 else hexc("15201a"))
    return img

def pipe_bottom():
    img = new(); noise_fill(img, (0, 0, 16, 16), shade(PIPE, 0.7), 0.05)
    return img

def station_side():
    img = new()
    noise_fill(img, (0, 0, 16, 16), hexc("5d6470"), 0.06)
    for i in range(16):
        img.putpixel((i, 0), hexc("8a929e")); img.putpixel((i, 15), hexc("3a3f48"))
        img.putpixel((0, i), hexc("7a828e")); img.putpixel((15, i), hexc("3a3f48"))
    for y in range(4, 12):
        for x in range(4, 12):
            img.putpixel((x, y), hexc("1d1f24"))
    for y in range(5, 11):
        img.putpixel((7, y), hexc("ff3030")); img.putpixel((8, y), hexc("ff6a6a"))
    return img

def station_top():
    img = new()
    noise_fill(img, (0, 0, 16, 16), hexc("6b7380"), 0.06)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 5: img.putpixel((x, y), hexc("2a0505"))
            if d < 3: img.putpixel((x, y), hexc("ff2020"))
            if d < 1.6: img.putpixel((x, y), hexc("ffb0b0"))
    return img

def station_bottom():
    img = new(); noise_fill(img, (0, 0, 16, 16), hexc("3a3f48"), 0.05)
    return img

# ------------------------------------------------------------------ lightsaber parts (block-atlas textures used by 3D item models)
def saber_blade(color):
    img = new()
    core = hexc("ffffff"); glow = shade(color, 1.25); edge = color
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5)
            img.putpixel((x, y), core if d < 3 else (mix(core, glow, (d - 3) / 3) if d < 6 else edge))
    return img

def saber_hilt_tex():
    img = new()
    noise_fill(img, (0, 0, 16, 16), hexc("b8bcc4"), 0.05)
    for y in range(0, 16, 3):
        for x in range(16):
            img.putpixel((x, y), hexc("2a2c31"))
    for y in range(16):
        img.putpixel((0, y), hexc("8a8f99")); img.putpixel((15, y), hexc("5a5e66"))
    img.putpixel((7, 7), hexc("d4202a")); img.putpixel((8, 7), hexc("d4202a"))
    return img

def saber_emitter_tex():
    img = new()
    noise_fill(img, (0, 0, 16, 16), hexc("2a2c31"), 0.08)
    for x in range(16):
        img.putpixel((x, 0), hexc("c8a040")); img.putpixel((x, 15), hexc("c8a040"))
    return img

# ------------------------------------------------------------------ entity textures
def box_uv(img, u, v, w, h, d, faces):
    """Paints a Minecraft box UV layout. faces: dict of top,bottom,right,front,left,back -> fn(img, x0, y0, fw, fh)."""
    layout = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }
    for name, (x0, y0, fw, fh) in layout.items():
        fn = faces.get(name, faces.get("default"))
        if fn:
            fn(img, x0, y0, fw, fh)

def solid(c, var=0.06, seed=None):
    def f(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), c, var, seed)
    return f

def sam_hyde():
    """Legacy 64x32-style humanoid skin on a 64x64 canvas: tall guy, dark side-part hair, light stubble,
    navy blazer over a white shirt, khakis, brown shoes, conference lanyard."""
    img = new(64, 64)
    skin = hexc("e2b08c"); skin_dk = hexc("c48f6c"); hair = hexc("3a2a1e"); hair_hi = hexc("54402e")
    blazer = hexc("26304a"); blazer_dk = hexc("1b2236"); shirt = hexc("f2f2ee"); pants = hexc("b59f74"); pants_dk = hexc("8e7b56")
    shoe = hexc("4a2f1c"); lanyard = hexc("e62b1e"); eye = hexc("3b5a7a"); white = hexc("ffffff")
    # head 8x8x8 at (0,0)
    def head_front(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), skin, 0.04)
        for x in range(x0, x0 + w):
            img.putpixel((x, y0), hair); img.putpixel((x, y0 + 1), hair)
        for x in range(x0, x0 + 5): img.putpixel((x, y0 + 2), hair_hi if x % 2 else hair)
        img.putpixel((x0, y0 + 3), hair); img.putpixel((x0 + 7, y0 + 3), hair)
        # eyes
        img.putpixel((x0 + 1, y0 + 4), white); img.putpixel((x0 + 2, y0 + 4), eye)
        img.putpixel((x0 + 5, y0 + 4), eye); img.putpixel((x0 + 6, y0 + 4), white)
        for x in (1, 2, 5, 6): img.putpixel((x0 + x, y0 + 3), hair)  # brows
        img.putpixel((x0 + 3, y0 + 5), skin_dk); img.putpixel((x0 + 4, y0 + 5), skin_dk)  # nose
        for x in range(2, 6): img.putpixel((x0 + x, y0 + 6), hexc("a0604a"))  # smirk
        img.putpixel((x0 + 6, y0 + 6), skin_dk)
        for x in range(1, 7): img.putpixel((x0 + x, y0 + 7), shade(skin, 0.85))  # stubble jaw
    def head_side(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), skin, 0.04)
        for y in range(y0, y0 + 3):
            for x in range(x0, x0 + w): img.putpixel((x, y), hair)
        for y in range(y0 + 3, y0 + 6): img.putpixel((x0 + (w - 1 if w else 0), y), hair)
        img.putpixel((x0 + 3, y0 + 4), skin_dk)  # ear
    def head_back(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), hair, 0.08)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + h - 1), skin)
    box_uv(img, 0, 0, 8, 8, 8, {"top": solid(hair, 0.1), "bottom": solid(skin), "front": head_front,
                               "right": head_side, "left": head_side, "back": head_back})
    # body 8x12x4 at (16,16)
    def body_front(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), blazer, 0.05)
        for y in range(y0, y0 + 9):
            for x in range(x0 + 3, x0 + 5): img.putpixel((x, y), shirt)
        img.putpixel((x0 + 2, y0), shirt); img.putpixel((x0 + 5, y0), shirt)
        for y in range(y0 + 1, y0 + 7):  # lanyard
            img.putpixel((x0 + 2, y), lanyard); img.putpixel((x0 + 5, y), lanyard)
        for x in range(x0 + 2, x0 + 6):  # badge
            for y in range(y0 + 7, y0 + 9): img.putpixel((x, y), white)
        img.putpixel((x0 + 3, y0 + 7), lanyard)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + h - 2), hexc("3a2a1e"))  # belt
        img.putpixel((x0 + 4, y0 + h - 2), hexc("c8a040"))
        for x in range(x0, x0 + w): img.putpixel((x, y0 + h - 1), pants)
        for y in range(y0 + 2, y0 + 10): img.putpixel((x0 + 1, y), blazer_dk)  # lapel shadows
    box_uv(img, 16, 16, 8, 12, 4, {"default": solid(blazer, 0.05), "front": body_front, "top": solid(blazer)})
    # arm 4x12x4 at (40,16): blazer sleeve, cuff, hand
    def arm_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), blazer, 0.05)
        for x in range(x0, x0 + w):
            img.putpixel((x, y0 + h - 4), shirt)
            for y in range(y0 + h - 3, y0 + h): img.putpixel((x, y), skin)
    box_uv(img, 40, 16, 4, 12, 4, {"default": arm_face, "top": solid(blazer), "bottom": solid(skin)})
    # leg 4x12x4 at (0,16)
    def leg_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), pants, 0.05)
        for y in range(y0, y0 + h - 2): img.putpixel((x0 + w - 1, y), pants_dk)
        for x in range(x0, x0 + w):
            for y in range(y0 + h - 2, y0 + h): img.putpixel((x, y), shoe)
    box_uv(img, 0, 16, 4, 12, 4, {"default": leg_face, "top": solid(pants), "bottom": solid(shoe)})
    return img

def depth_growler():
    img = new(64, 64)
    skin = hexc("7d8c84"); skin_dk = hexc("5a675f"); rag = hexc("4a3d2e"); rag_dk = hexc("33291e")
    eye = hexc("d8ff3a"); helmet = hexc("c9a227"); lamp = hexc("fff6b0"); neo = NEOPLAS
    def head_front(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), skin, 0.08)
        for x in range(x0, x0 + w):
            img.putpixel((x, y0), helmet); img.putpixel((x, y0 + 1), helmet)
        img.putpixel((x0 + 3, y0), lamp); img.putpixel((x0 + 4, y0), lamp); img.putpixel((x0 + 3, y0 + 1), lamp); img.putpixel((x0 + 4, y0 + 1), lamp)
        img.putpixel((x0 + 1, y0 + 4), eye); img.putpixel((x0 + 2, y0 + 4), eye)
        img.putpixel((x0 + 5, y0 + 4), eye); img.putpixel((x0 + 6, y0 + 4), eye)
        for x in range(x0 + 1, x0 + 7): img.putpixel((x, y0 + 3), skin_dk)
        for x in range(x0 + 2, x0 + 6): img.putpixel((x, y0 + 6), hexc("2a1a14"))
        img.putpixel((x0 + 2, y0 + 7), hexc("e8e2c8")); img.putpixel((x0 + 5, y0 + 7), hexc("e8e2c8"))
    def head_other(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), skin, 0.08)
        for x in range(x0, x0 + w):
            img.putpixel((x, y0), helmet); img.putpixel((x, y0 + 1), helmet)
    box_uv(img, 0, 0, 8, 8, 8, {"top": solid(helmet, 0.05), "front": head_front, "default": head_other})
    def body_front(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), rag, 0.15)
        for y in range(y0, y0 + h, 3):
            img.putpixel((x0 + (y % 5), y), rag_dk)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + 8), hexc("2a2014"))
        img.putpixel((x0 + 2, y0 + 9), neo); img.putpixel((x0 + 5, y0 + 10), neo)  # neoplas pouch
    box_uv(img, 16, 16, 8, 12, 4, {"default": solid(rag, 0.15), "front": body_front})
    def arm_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), skin, 0.1)
        for x in range(x0, x0 + w):
            for y in range(y0, y0 + 4): img.putpixel((x, y), rag)
    box_uv(img, 40, 16, 4, 12, 4, {"default": arm_face})
    def leg_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), rag_dk, 0.12)
        for x in range(x0, x0 + w):
            for y in range(y0 + h - 3, y0 + h): img.putpixel((x, y), hexc("1e1a16"))
    box_uv(img, 0, 16, 4, 12, 4, {"default": leg_face})
    return img

def elephant_mario():
    """Original design: grey elephant, teal knit cap with a gold pipe badge, rust-orange work overalls, tool belt."""
    img = new(128, 128)
    grey = hexc("9a9aa6"); grey_dk = hexc("767684"); grey_hi = hexc("b6b6c2"); pink = hexc("e8a6b4")
    cap = hexc("1f9e9a"); cap_dk = hexc("157572"); badge = hexc("e8c547")
    over = hexc("c4622d"); over_dk = hexc("8f4520"); shirt = hexc("f0e8d0"); belt = hexc("3b2a1a"); white = hexc("ffffff"); black = hexc("111111")
    # body 16x14x10 at (0,0): shirt shoulders, overalls with bib + buttons + tool belt
    def body_front(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), over, 0.06)
        for y in range(y0, y0 + 4):
            for x in range(x0, x0 + w):
                if x < x0 + 3 or x >= x0 + w - 3: img.putpixel((x, y), shirt)
        for y in range(y0, y0 + 4):  # straps
            img.putpixel((x0 + 3, y), over_dk); img.putpixel((x0 + w - 4, y), over_dk)
        img.putpixel((x0 + 4, y0 + 4), badge); img.putpixel((x0 + w - 5, y0 + 4), badge)  # buttons
        for x in range(x0 + 6, x0 + 10):  # bib pocket
            img.putpixel((x, y0 + 6), over_dk)
        img.putpixel((x0 + 6, y0 + 7), over_dk); img.putpixel((x0 + 9, y0 + 7), over_dk)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + 10), belt)
        img.putpixel((x0 + 7, y0 + 10), badge); img.putpixel((x0 + 8, y0 + 10), badge)
        img.putpixel((x0 + 12, y0 + 11), hexc("9aa3ad")); img.putpixel((x0 + 12, y0 + 12), hexc("9aa3ad"))  # wrench on belt
    def body_back(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), over, 0.06)
        for y in range(y0, y0 + 5):
            img.putpixel((x0 + 4 + (y - y0), y), over_dk); img.putpixel((x0 + w - 5 - (y - y0), y), over_dk)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + 10), belt)
    def body_side(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), over, 0.06)
        for y in range(y0, y0 + 4):
            for x in range(x0, x0 + w): img.putpixel((x, y), shirt)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + 10), belt)
    box_uv(img, 0, 0, 16, 14, 10, {"front": body_front, "back": body_back, "right": body_side, "left": body_side,
                                   "top": solid(shirt), "bottom": solid(over_dk)})
    # head 12x11x11 at (0,24)
    def head_front(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), grey, 0.05)
        # eyes: big friendly ovals
        for (ex) in (2, 8):
            for dy in range(3):
                for dx in range(2):
                    img.putpixel((x0 + ex + dx, y0 + 3 + dy), white)
            img.putpixel((x0 + ex + 1, y0 + 4), black); img.putpixel((x0 + ex + 1, y0 + 5), black)
            img.putpixel((x0 + ex, y0 + 2), grey_dk); img.putpixel((x0 + ex + 1, y0 + 2), grey_dk)
        # cheeks
        img.putpixel((x0 + 1, y0 + 7), pink); img.putpixel((x0 + 10, y0 + 7), pink)
        for x in range(x0 + 4, x0 + 8): img.putpixel((x, y0 + 9), grey_dk)  # mouth line
        for x in range(x0, x0 + w): img.putpixel((x, y0 + h - 1), grey_dk)
    box_uv(img, 0, 24, 12, 11, 11, {"front": head_front, "default": solid(grey, 0.05), "top": solid(grey_hi, 0.04)})
    # cap 13x3x12 at (0,48) and brim 13x1x4 at (0,64)
    def cap_front(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), cap, 0.05)
        for x in range(x0, x0 + w, 2): img.putpixel((x, y0 + h - 1), cap_dk)  # knit rib
        cx = x0 + w // 2
        img.putpixel((cx - 1, y0), badge); img.putpixel((cx, y0), badge); img.putpixel((cx, y0 + 1), badge); img.putpixel((cx - 1, y0 + 1), badge)
    box_uv(img, 0, 48, 13, 3, 12, {"front": cap_front, "default": solid(cap, 0.05), "top": solid(shade(cap, 1.1), 0.08)})
    box_uv(img, 0, 64, 13, 1, 4, {"default": solid(cap_dk, 0.04)})
    # legs 6x10x6 at (56,0): overalls + big grey feet
    def leg_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), over, 0.06)
        for x in range(x0, x0 + w):
            img.putpixel((x, y0 + h - 4), over_dk)
            for y in range(y0 + h - 3, y0 + h): img.putpixel((x, y), grey_dk)
        if w == 6:
            for x in range(x0 + 1, x0 + w - 1, 2): img.putpixel((x, y0 + h - 1), hexc("d8d8d0"))  # toenails
    box_uv(img, 56, 0, 6, 10, 6, {"default": leg_face, "top": solid(over), "bottom": solid(grey_dk)})
    # arms 5x13x5 at (80,0): rolled shirt sleeves then grey arm
    def arm_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), grey, 0.05)
        for x in range(x0, x0 + w):
            for y in range(y0, y0 + 4): img.putpixel((x, y), shirt)
            img.putpixel((x, y0 + 4), hexc("d8d0b8"))
    box_uv(img, 80, 0, 5, 13, 5, {"default": arm_face, "top": solid(shirt), "bottom": solid(grey_dk)})
    # ears 1x10x8 at (56,16)
    def ear_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), grey, 0.05)
        if w >= 8:
            for y in range(y0 + 2, y0 + h - 2):
                for x in range(x0 + 2, x0 + w - 2): img.putpixel((x, y), pink)
    box_uv(img, 56, 16, 1, 10, 8, {"default": ear_face})
    # trunk pieces
    def trunk_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), grey, 0.05)
        for y in range(y0 + 1, y0 + h, 2):
            for x in range(x0, x0 + w): img.putpixel((x, y), grey_dk)
    box_uv(img, 100, 0, 4, 6, 4, {"default": trunk_face})
    box_uv(img, 100, 10, 3, 6, 3, {"default": trunk_face, "bottom": solid(pink)})
    # tusks
    box_uv(img, 116, 0, 1, 1, 3, {"default": solid(hexc("f4f0e0"), 0.02)})
    return img

def goober():
    img = new(64, 32)
    goo = hexc("8a4fd0"); goo_dk = hexc("5e2f99"); goo_hi = hexc("b98af0"); white = hexc("ffffff"); black = hexc("1a0a24")
    brow = hexc("2a0a44"); foot = hexc("4a2a1a")
    def body_front(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), goo, 0.06)
        # angry brows + eyes
        img.putpixel((x0 + 2, y0 + 1), brow); img.putpixel((x0 + 3, y0 + 2), brow)
        img.putpixel((x0 + 7, y0 + 1), brow); img.putpixel((x0 + 6, y0 + 2), brow)
        for (ex) in (2, 6):
            img.putpixel((x0 + ex, y0 + 3), white); img.putpixel((x0 + ex + 1, y0 + 3), white)
            img.putpixel((x0 + ex, y0 + 4), white); img.putpixel((x0 + ex + 1, y0 + 4), black if ex == 2 else white)
            if ex == 6: img.putpixel((x0 + ex, y0 + 4), black)
        # grumpy mouth with one tooth
        for x in range(x0 + 3, x0 + 7): img.putpixel((x, y0 + 6), black)
        img.putpixel((x0 + 4, y0 + 6), white)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + h - 1), goo_dk)
    def body_other(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), goo, 0.06)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + h - 1), goo_dk)
        img.putpixel((x0 + 1, y0 + 1), goo_hi)
    box_uv(img, 0, 0, 10, 8, 8, {"front": body_front, "default": body_other, "top": solid(goo_hi, 0.08)})
    box_uv(img, 36, 0, 4, 3, 4, {"default": solid(goo_hi, 0.08)})
    box_uv(img, 0, 16, 4, 3, 5, {"default": solid(foot, 0.08)})
    return img

def hyde_armor_layer(leggings=False):
    """64x32 humanoid armor layer: neoplas plates over a navy suit, red tie, glowing visor."""
    img = new(64, 32)
    a = NEOPLAS; a_dk = shade(a, 0.6); a_hi = shade(a, 1.35); suit = hexc("2a3242"); tie = hexc("e62b1e"); visor = hexc("ff4fd8")
    def plate(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), a, 0.05)
        for x in range(x0, x0 + w):
            img.putpixel((x, y0), a_hi); img.putpixel((x, y0 + h - 1), a_dk)
        for y in range(y0, y0 + h):
            img.putpixel((x0, y), a_hi if y % 4 else a_dk); img.putpixel((x0 + w - 1, y), a_dk)
    if not leggings:
        def helm_front(img, x0, y0, w, h):
            plate(img, x0, y0, w, h)
            for x in range(x0 + 1, x0 + w - 1):
                img.putpixel((x, y0 + 4), visor); img.putpixel((x, y0 + 3), shade(visor, 0.7))
            for y in range(y0 + 5, y0 + h): 
                for x in range(x0 + 2, x0 + w - 2): img.putpixel((x, y), (0, 0, 0, 0))
        box_uv(img, 0, 0, 8, 8, 8, {"default": plate, "front": helm_front, "bottom": lambda *a_: None})
        def chest_front(img, x0, y0, w, h):
            plate(img, x0, y0, w, h)
            for y in range(y0, y0 + 9):
                for x in range(x0 + 3, x0 + 5): img.putpixel((x, y), suit)
            for y in range(y0 + 1, y0 + 8): img.putpixel((x0 + 3, y), tie); img.putpixel((x0 + 4, y), tie)
            img.putpixel((x0 + 3, y0 + 8), tie)
        box_uv(img, 16, 16, 8, 12, 4, {"default": plate, "front": chest_front})
        box_uv(img, 40, 16, 4, 12, 4, {"default": plate})
        box_uv(img, 0, 16, 4, 12, 4, {"default": plate})  # boots use the leg box of layer 1
    else:
        def leg_plate(img, x0, y0, w, h):
            noise_fill(img, (x0, y0, x0 + w, y0 + h), suit, 0.05)
            for y in range(y0, y0 + h, 3):
                for x in range(x0, x0 + w): img.putpixel((x, y), a)
        box_uv(img, 16, 16, 8, 12, 4, {"default": leg_plate})
        box_uv(img, 0, 16, 4, 12, 4, {"default": leg_plate})
    return img

def main():
    # blocks
    save(kyber_ore(stone(11), KYBER["green"]), "block/green_kyber_ore.png")
    save(kyber_ore(deepslate(12), KYBER["blue"]), "block/deepslate_blue_kyber_ore.png")
    save(kyber_ore(netherrack(13), KYBER["red"]), "block/nether_red_kyber_ore.png")
    save(kyber_ore(end_stone(14), KYBER["purple"]), "block/end_purple_kyber_ore.png")
    save(neoplas_block(), "block/neoplas_block.png")
    save(trash_block(), "block/trash_cube_block.png")
    save(pipe_side(), "block/warp_pipe_side.png")
    save(pipe_top(), "block/warp_pipe_top.png")
    save(pipe_bottom(), "block/warp_pipe_bottom.png")
    save(station_side(), "block/charging_station_side.png")
    save(station_top(), "block/charging_station_top.png")
    save(station_bottom(), "block/charging_station_bottom.png")
    # lightsaber model parts
    for name, c in KYBER.items():
        save(saber_blade(c), f"item/lightsaber_blade_{name}.png")
    save(saber_hilt_tex(), "item/lightsaber_hilt_metal.png")
    save(saber_emitter_tex(), "item/lightsaber_emitter.png")
    # items
    for name, c in KYBER.items():
        save(crystal_item(c), f"item/{name}_kyber_crystal.png")
    save(lightsaber_hilt_item(), "item/lightsaber_hilt.png")
    save(trash_cube(), "item/trash_cube.png")
    save(nugget(NEOPLAS), "item/neoplas_nugget.png")
    save(ingot(NEOPLAS), "item/neoplas_ingot.png")
    save(core(), "item/paradigm_shift_core.png")
    save(veggie(hexc("8a1f4a"), hexc("2f9e6a"), "beet"), "item/sea_beet.png")
    save(veggie(hexc("c8743a"), hexc("2f9e6a"), "yam"), "item/sea_yam.png")
    save(veggie(hexc("6fcf8a"), hexc("2f8e9e"), "cabbage"), "item/sea_cabbage.png")
    save(bowl_food([hexc("6fcf8a"), hexc("8a1f4a"), hexc("c8743a"), hexc("2f9e6a"), hexc("bff4ff")]), "item/sea_salad.png")
    save(baked_potato_cheesy(), "item/sea_cheesy_baked_potato.png")
    save(bottle(hexc("4a2c14"), hexc("f2e6c8")), "item/bulletproof_coffee.png")
    save(bottle(hexc("ff6a8a"), hexc("3a6bd8")), "item/soda.png")
    save(soda_stream(), "item/soda_stream.png")
    save(microphone(), "item/ted_microphone.png")
    save(hand(), "item/pat_on_the_back.png")
    save(blackberry_phone(), "item/mind_upload_blackberry.png")
    save(clock_item(), "item/reset_the_clock.png")
    save(maglev(), "item/maglev_moon_launcher.png")
    save(book(hexc("2f5fa8")), "item/einstein_parable.png")
    save(invitation(), "item/tedx_invitation.png")
    save(goo(), "item/goober_goo.png")
    save(fruit(), "item/elephant_fruit.png")
    save(peanut(), "item/super_peanut.png")
    save(wrench(), "item/pipe_wrench.png")
    for k in ("helmet", "chestplate", "leggings", "boots"):
        save(armor_icon(k), f"item/hyde_{k}.png")
    save(spawn_egg(hexc("26304a"), hexc("e62b1e")), "item/sam_hyde_spawn_egg.png")
    save(spawn_egg(hexc("9a9aa6"), hexc("1f9e9a")), "item/elephant_mario_spawn_egg.png")
    save(spawn_egg(hexc("8a4fd0"), hexc("2a0a44")), "item/goober_spawn_egg.png")
    save(spawn_egg(hexc("7d8c84"), hexc("d8ff3a")), "item/depth_growler_spawn_egg.png")
    # entities
    save(sam_hyde(), "entity/sam_hyde.png")
    save(depth_growler(), "entity/depth_growler.png")
    save(elephant_mario(), "entity/elephant_mario.png")
    save(goober(), "entity/goober.png")
    save(hyde_armor_layer(False), "entity/equipment/humanoid/hyde.png")
    save(hyde_armor_layer(True), "entity/equipment/humanoid_leggings/hyde.png")
    print("textures written to", os.path.abspath(ROOT))

# ================================================================== v1.1 additions
import json as _json

def save_anim(frames, path, frametime=2, interpolate=True):
    """Stacks frames vertically and writes the .mcmeta animation file next to the png."""
    w, h = frames[0].size
    sheet = Image.new("RGBA", (w, h * len(frames)), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        sheet.paste(f, (0, i * h))
    save(sheet, path)
    with open(os.path.join(ROOT, path) + ".mcmeta", "w") as fh:
        _json.dump({"animation": {"frametime": frametime, "interpolate": interpolate}}, fh)

# ---- kyber crystal overlay: clusters of hexagonal shards that pulse and twinkle (transparent elsewhere)
CLUSTERS = [  # (base x, base y, shard heights, lean)
    (2, 6, [4, 6, 3], 0), (10, 4, [3, 5, 4], 1), (5, 14, [5, 7, 4, 3], 0), (12, 14, [4, 3], -1), (8, 9, [2, 3], 0),
]

def crystal_overlay(color, phase):
    img = new()
    glow = (math.sin(phase * 2 * math.pi) + 1) / 2  # 0..1 pulse
    dark = shade(color, 0.55 + 0.15 * glow); mid = shade(color, 0.95 + 0.25 * glow)
    light = shade(color, 1.35 + 0.3 * glow); white = mix(color, hexc("ffffff"), 0.75 + 0.2 * glow)
    for (bx, by, heights, lean) in CLUSTERS:
        for i, hgt in enumerate(heights):
            x = bx + i - len(heights) // 2
            for k in range(hgt):
                yy = by - k
                xx = x + (lean if k > hgt // 2 else 0)
                if 0 <= xx < 16 and 0 <= yy < 16:
                    c = light if k == hgt - 1 else (mid if (i + k) % 2 == 0 else dark)
                    img.putpixel((xx, yy), c)
            # bright tip
            tx = x + (lean if hgt - 1 > hgt // 2 else 0); ty = by - hgt + 1
            if 0 <= tx < 16 and 0 <= ty < 16:
                img.putpixel((tx, ty), white)
    # travelling twinkle
    stars = [(2, 1), (10, 1), (5, 8), (13, 10), (8, 6), (3, 12)]
    sx, sy = stars[int(phase * len(stars)) % len(stars)]
    if glow > 0.3:
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
            if 0 <= sx + dx < 16 and 0 <= sy + dy < 16:
                img.putpixel((sx + dx, sy + dy), hexc("ffffff") if (dx, dy) == (0, 0) else light)
    return img

# ---- lightsaber blade: hot white core with flickering streaks, and a soft translucent halo
def blade_core_frames(color, n=8):
    frames = []
    r = random.Random(sum(color[:3]))
    for f in range(n):
        img = new()
        flick = 0.9 + 0.1 * math.sin(f / n * 2 * math.pi)
        for y in range(16):
            streak = r.random() < 0.18
            for x in range(16):
                d = abs(x - 7.5) / 7.5
                if d < 0.45:
                    c = mix(hexc("ffffff"), color, 0.08 if not streak else 0.25)
                elif d < 0.8:
                    c = mix(hexc("ffffff"), color, (d - 0.45) / 0.35 * 0.85)
                else:
                    c = shade(color, 1.15)
                img.putpixel((x, y), shade(c, flick))
        frames.append(img)
    return frames

def blade_halo_frames(color, n=8):
    frames = []
    for f in range(n):
        img = new()
        pulse = 0.75 + 0.25 * math.sin(f / n * 2 * math.pi)
        for y in range(16):
            for x in range(16):
                d = abs(x - 7.5) / 7.5
                a = int(max(0, 1 - d) ** 1.4 * 150 * pulse) + 20
                img.putpixel((x, y), shade(color, 1.25)[:3] + (min(255, a),))
        frames.append(img)
    return frames

# ---- Sam Hyde, 2070 Paradigm Shift look (full 64x64 player-skin layout)
def paint_box(img, u, v, w, h, d, faces):
    box_uv(img, u, v, w, h, d, faces)

def sam_hyde_2070():
    img = new(64, 64)
    skin = hexc("e7b896"); skin_dk = hexc("c99677"); beard = hexc("8a6240"); beard_dk = hexc("6e4c31")
    beanie = hexc("6e1f2a"); beanie_dk = hexc("531620"); beanie_hi = hexc("8a2a37")
    glass = hexc("c9b27a"); eye = hexc("4a6a8a"); white = hexc("f4f4f4"); lens = hexc("d8e6f0")
    shirt = hexc("8c2a33"); shirt_dk = hexc("6c1e26"); shirt_hi = hexc("a83a44")
    gold = hexc("b8933a"); gold_hi = hexc("e2c46a"); gold_dk = hexc("7a5e22"); stud = hexc("2a2418")
    jeans = hexc("2b3140"); jeans_dk = hexc("1f2430"); shoe = hexc("1b1b1b")

    def face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), skin, 0.03, 1)
        for x in range(x0, x0 + w):   # beanie covers the top of the head
            img.putpixel((x, y0), beanie); img.putpixel((x, y0 + 1), beanie_dk)
        # wire glasses
        for x in (1, 2, 3, 4, 5, 6):
            img.putpixel((x0 + x, y0 + 3), glass)
        img.putpixel((x0 + 1, y0 + 4), glass); img.putpixel((x0 + 3, y0 + 4), glass)
        img.putpixel((x0 + 4, y0 + 4), glass); img.putpixel((x0 + 6, y0 + 4), glass)
        img.putpixel((x0 + 2, y0 + 4), eye); img.putpixel((x0 + 5, y0 + 4), eye)
        img.putpixel((x0 + 0, y0 + 3), glass); img.putpixel((x0 + 7, y0 + 3), glass)
        img.putpixel((x0 + 2, y0 + 2), beard_dk); img.putpixel((x0 + 5, y0 + 2), beard_dk)  # brows
        img.putpixel((x0 + 3, y0 + 5), skin_dk); img.putpixel((x0 + 4, y0 + 5), skin_dk)  # nose
        # mustache + goatee + stubble jaw
        for x in (2, 3, 4, 5): img.putpixel((x0 + x, y0 + 6), beard)
        img.putpixel((x0 + 3, y0 + 6), hexc("9a4a40")); img.putpixel((x0 + 4, y0 + 6), hexc("9a4a40"))  # lips peek
        img.putpixel((x0 + 1, y0 + 6), shade(skin, 0.9)); img.putpixel((x0 + 6, y0 + 6), shade(skin, 0.9))
        for x in range(1, 7): img.putpixel((x0 + x, y0 + 7), beard if 2 <= x <= 5 else shade(skin, 0.85))
        img.putpixel((x0 + 3, y0 + 7), beard_dk); img.putpixel((x0 + 4, y0 + 7), beard_dk)
    def head_side(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), skin, 0.03, 2)
        for x in range(x0, x0 + w):
            img.putpixel((x, y0), beanie); img.putpixel((x, y0 + 1), beanie_dk)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + 3), glass if x - x0 >= w - 3 or x - x0 < 3 else skin)
        img.putpixel((x0 + w // 2, y0 + 4), skin_dk)  # ear
        for y in range(y0 + 5, y0 + h): img.putpixel((x0 + (0 if x0 % 2 else w - 1), y), shade(skin, 0.88))
    def head_back(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), hexc("4a3322"), 0.08, 3)
        for x in range(x0, x0 + w):
            for y in range(y0, y0 + 3): img.putpixel((x, y), beanie)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + h - 1), skin)
    box_uv(img, 0, 0, 8, 8, 8, {"front": face, "right": head_side, "left": head_side, "back": head_back,
                               "top": solid(beanie, 0.06, 4), "bottom": solid(skin)})
    # hat overlay = the knit beanie: ribbed cuff around the head + slouch on top
    def beanie_band(img, x0, y0, w, h):
        for x in range(x0, x0 + w):
            img.putpixel((x, y0), beanie_hi if x % 2 else beanie)
            img.putpixel((x, y0 + 1), beanie if x % 2 else beanie_dk)
            img.putpixel((x, y0 + 2), beanie_dk if x % 2 else shade(beanie_dk, 0.85))  # folded cuff
    box_uv(img, 32, 0, 8, 8, 8, {"front": beanie_band, "right": beanie_band, "left": beanie_band, "back": beanie_band,
                                "top": solid(beanie_hi, 0.08, 5)})
    # body (base layer): burgundy long-sleeve shirt
    def shirt_face(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), shirt, 0.06, 6)
        for x in range(x0, x0 + w): img.putpixel((x, y0 + h - 1), jeans)
    box_uv(img, 16, 16, 8, 12, 4, {"default": shirt_face})
    # jacket overlay: gold studded breastplate (front + back), shirt shows at the sides
    def plate(img, x0, y0, w, h, back=False):
        for y in range(y0, y0 + h - 2):
            for x in range(x0, x0 + w):
                gx, gy = x - x0, y - y0
                c = gold_hi if gx in (1, 2) and not back else (gold_dk if gx == w - 1 else gold)
                if gy == 0 or gy == h - 3: c = gold_dk
                img.putpixel((x, y), c)
        # rows of dark studs, offset every other row
        for gy in range(1, h - 3, 2):
            for gx in range((gy // 2) % 2, w, 2):
                img.putpixel((x0 + gx, y0 + gy), stud)
                if gy + 1 < h - 3 and gx + 1 < w: img.putpixel((x0 + gx, y0 + gy + 1), gold_hi)
        if not back:  # collar opening showing the shirt
            img.putpixel((x0 + 3, y0), shirt); img.putpixel((x0 + 4, y0), shirt); img.putpixel((x0 + 3, y0 + 1), shirt_dk); img.putpixel((x0 + 4, y0 + 1), shirt_dk)
    def plate_side(img, x0, y0, w, h):
        for y in range(y0, y0 + h - 2):
            for x in range(x0, x0 + w): img.putpixel((x, y), gold_dk if y % 3 else stud)
    box_uv(img, 16, 32, 8, 12, 4, {"front": plate, "back": lambda i, a, b, c, d: plate(i, a, b, c, d, True),
                                  "right": plate_side, "left": plate_side, "top": solid(gold, 0.05, 7)})
    # arms: burgundy sleeves, hands; sleeve overlays carry studded gold pauldrons on the shoulders
    def arm(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), shirt, 0.07, 8)
        for y in range(y0, y0 + h - 2, 3): img.putpixel((x0 + (y % w), y), shirt_dk)  # folds
        for x in range(x0, x0 + w):
            for y in range(y0 + h - 3, y0 + h): img.putpixel((x, y), skin)
    def pauldron(img, x0, y0, w, h):
        for y in range(y0, y0 + 4):
            for x in range(x0, x0 + w):
                img.putpixel((x, y), stud if (x + y) % 3 == 0 else (gold_hi if y == y0 else gold))
    for (u, v) in ((40, 16), (32, 48)):
        box_uv(img, u, v, 4, 12, 4, {"default": arm, "top": solid(shirt), "bottom": solid(skin)})
    for (u, v) in ((40, 32), (48, 48)):
        box_uv(img, u, v, 4, 12, 4, {"default": pauldron, "top": solid(gold, 0.05, 9), "bottom": lambda *a: None})
    # legs: dark jeans + black shoes
    def leg(img, x0, y0, w, h):
        noise_fill(img, (x0, y0, x0 + w, y0 + h), jeans, 0.06, 10)
        for y in range(y0, y0 + h - 2): img.putpixel((x0 + w - 1, y), jeans_dk)
        for x in range(x0, x0 + w):
            for y in range(y0 + h - 2, y0 + h): img.putpixel((x, y), shoe)
    for (u, v) in ((0, 16), (16, 48)):
        box_uv(img, u, v, 4, 12, 4, {"default": leg, "top": solid(jeans), "bottom": solid(shoe)})
    return img

def hyde_armor_2070(leggings=False):
    """Hyde's Armour = the gold studded breastplate + maroon knit beanie from the talk."""
    img = new(64, 32)
    gold = hexc("b8933a"); gold_hi = hexc("e2c46a"); gold_dk = hexc("7a5e22"); stud = hexc("2a2418")
    beanie = hexc("6e1f2a"); beanie_dk = hexc("531620"); beanie_hi = hexc("8a2a37")
    def studded(img, x0, y0, w, h):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                gx, gy = x - x0, y - y0
                c = gold_dk if gy in (0, h - 1) or gx == w - 1 else (gold_hi if gx == 0 else gold)
                if gy % 2 == 1 and (gx + gy // 2) % 2 == 0 and 0 < gy < h - 1: c = stud
                img.putpixel((x, y), c)
    if not leggings:
        def beanie_face(img, x0, y0, w, h):
            for y in range(y0, y0 + h):
                for x in range(x0, x0 + w):
                    gy = y - y0
                    if gy < 4: img.putpixel((x, y), beanie_hi if (x % 2 and gy == 3) else (beanie if gy < 3 else beanie_dk))
        box_uv(img, 0, 0, 8, 8, 8, {"default": beanie_face, "top": solid(beanie_hi, 0.08, 11), "bottom": lambda *a: None})
        box_uv(img, 16, 16, 8, 12, 4, {"default": studded})
        box_uv(img, 40, 16, 4, 12, 4, {"default": lambda i, x0, y0, w, h: studded(i, x0, y0, w, min(h, 5))})
        box_uv(img, 0, 16, 4, 12, 4, {"default": lambda i, x0, y0, w, h: studded(i, x0, y0 + max(0, h - 4), w, min(h, 4))})
    else:
        box_uv(img, 16, 16, 8, 12, 4, {"default": lambda i, x0, y0, w, h: studded(i, x0, y0, w, min(h, 5))})
        box_uv(img, 0, 16, 4, 12, 4, {"default": studded})
    return img

def goobers_item():
    """A little carton of chocolate-covered peanuts with a few spilling out (original generic packaging)."""
    img = new()
    box = hexc("e8d24a"); box_dk = hexc("b89a2a"); choc = hexc("5a3218"); choc_hi = hexc("8a5a34"); choc_dk = hexc("3a1e0c")
    for y in range(2, 11):
        for x in range(3, 11):
            img.putpixel((x, y), box if x < 9 else box_dk)
    for x in range(3, 11): img.putpixel((x, 2), hexc("f6e88a"))
    for x in range(4, 9): img.putpixel((x, 6), hexc("c0392b"))  # plain red stripe
    for (cx, cy) in ((11, 12), (7, 13), (3, 12), (12, 9)):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1), (-1, 0), (0, -1)):
            x, y = cx + dx, cy + dy
            if 0 <= x < 16 and 0 <= y < 16: img.putpixel((x, y), choc)
        img.putpixel((cx, cy - 1 if cy > 0 else cy), choc_hi); img.putpixel((cx + 1, cy + 1), choc_dk)
    return outline(img, hexc("2a1408"))

def peanut_item():
    img = new()
    shell = hexc("d9b27a"); dk = hexc("a8824a")
    rows = ["................",
            "................",
            "................",
            ".........sss....",
            "........sSsSs...",
            "........ssssd...",
            ".........ssd....",
            "........ssd.....",
            ".......sssss....",
            "......sSsSsss...",
            "......sssssd....",
            ".......sssdd....",
            "........ddd.....",
            "................",
            "................",
            "................"]
    draw_rows(img, rows, {"s": shell, "S": dk, "d": dk, ".": None})
    return outline(img, hexc("3a2410"))

def main_v11():
    # Sam Hyde 2070 + Hyde's Armour
    save(sam_hyde_2070(), "entity/sam_hyde.png")
    save(hyde_armor_2070(False), "entity/equipment/humanoid/hyde.png")
    save(hyde_armor_2070(True), "entity/equipment/humanoid_leggings/hyde.png")
    save(goober(), "entity/depth_grovler.png")
    # ores: glowing, pulsing crystal overlays (base stone comes from vanilla textures)
    for name, c in KYBER.items():
        save_anim([crystal_overlay(c, f / 12) for f in range(12)], f"block/{name}_kyber_crystals.png", 2)
    # lightsaber glow
    for name, c in KYBER.items():
        save_anim(blade_core_frames(c), f"item/lightsaber_blade_{name}.png", 1)
        save_anim(blade_halo_frames(c), f"item/lightsaber_halo_{name}.png", 2)
    save(goobers_item(), "item/goobers.png")
    save(peanut_item(), "item/peanut.png")
    save(spawn_egg(hexc("8a4fd0"), hexc("c9b27a")), "item/depth_grovler_spawn_egg.png")
    # retire v1.0 files
    for old in ("entity/goober.png", "entity/depth_growler.png", "item/goober_goo.png", "item/goober_spawn_egg.png",
                "item/depth_growler_spawn_egg.png", "block/green_kyber_ore.png", "block/deepslate_blue_kyber_ore.png",
                "block/nether_red_kyber_ore.png", "block/end_purple_kyber_ore.png"):
        pth = os.path.join(ROOT, old)
        if os.path.exists(pth): os.remove(pth)

if __name__ == "__main__":
    main()
    main_v11()
