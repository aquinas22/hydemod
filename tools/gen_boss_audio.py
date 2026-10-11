"""Synthesizes HydeMod's original boss music and boss sound effects. Run: python3 tools/gen_boss_audio.py"""
import math, random, os, sys
sys.path.insert(0, os.path.dirname(__file__))
from gen_sounds import write_ogg, saw, LowPass, SR

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "hydemod", "sounds")
rnd = random.Random(7)

def note_hz(n):  # MIDI note -> Hz
    return 440.0 * 2 ** ((n - 69) / 12)

def square(ph, duty=0.5):
    return 1.0 if (ph % 1.0) < duty else -1.0

def tri(ph):
    p = ph % 1.0
    return 4 * p - 1 if p < 0.5 else 3 - 4 * p

def render_song(bpm, bars, bass, lead, arp, drums, lead_wave="square", swing=0.0):
    """Renders one seamless loop: the song is rendered one extra bar long, then the loop's opening is
    replaced by the rendered continuation so the last sample flows exactly into the first, and a
    DC-blocking filter removes the waveform's offset."""
    step = 60.0 / bpm / 4
    loop_n = int(step * bars * 16 * SR)
    raw = _render(bpm, bars + 1, bass, lead, arp, drums, lead_wave)
    head = int(0.5 * SR)
    out = raw[:loop_n]
    for i in range(head):  # continuation already contains the previous loop's ringing tails
        k = i / head
        out[i] = raw[loop_n + i] * (1 - k) + raw[i] * k if i > head // 2 else raw[loop_n + i]
    # DC blocker (one-pole high-pass at ~10 Hz), run twice around the loop so its state is periodic
    r = 0.9986; x1 = y1 = 0.0
    for _ in range(2):
        res = []
        for x in out:
            y = x - x1 + r * y1
            x1, y1 = x, y
            res.append(y)
    return res

def _render(bpm, bars, bass, lead, arp, drums, lead_wave):
    step = 60.0 / bpm / 4
    steps = bars * 16
    n = int(step * steps * SR)
    out = [0.0] * n
    def voice(pattern, wave, vol, octave=0, decay=6.0, cutoff=2500):
        lp = LowPass(cutoff)
        ph = 0.0; cur = None; t_on = 0.0
        for i in range(n):
            t = i / SR
            s = int(t / step) % len(pattern)
            pos = (t / step) % 1.0
            p = pattern[s]
            if pos * step < 1.0 / SR * 1.5 and p not in (None, "-"):
                cur = p; t_on = t
            if p is None and pos < 0.02:
                cur = None
            if cur is None:
                lp(0.0); continue
            f = note_hz(cur + octave)
            ph += f / SR
            env = math.exp(-(t - t_on) * decay)
            x = {"square": square(ph), "pulse": square(ph, 0.25), "saw": saw(ph), "tri": tri(ph)}[wave]
            out[i] += lp(x) * vol * (0.35 + 0.65 * env)
    voice(bass, "saw", 0.32, -12, 3.0, 700)
    voice(lead, lead_wave, 0.22, 0, 2.0, 3200)
    voice(arp, "pulse", 0.12, 12, 9.0, 4000)
    # drums
    for s in range(steps):
        t0 = int(s * step * SR)
        d = drums[s % len(drums)]
        if d in ("k", "b"):  # kick
            for i in range(int(0.18 * SR)):
                if t0 + i < n:
                    tt = i / SR
                    out[t0 + i] += math.sin(2 * math.pi * (50 + 120 * math.exp(-tt * 30)) * tt) * math.exp(-tt * 14) * 0.7
        if d in ("s", "b"):  # snare
            for i in range(int(0.15 * SR)):
                if t0 + i < n:
                    tt = i / SR
                    out[t0 + i] += (rnd.uniform(-1, 1) * 0.5 + math.sin(2 * math.pi * 190 * tt) * 0.3) * math.exp(-tt * 22) * 0.6
        if d == "h" or s % 2 == 0:  # hats
            for i in range(int(0.04 * SR)):
                if t0 + i < n:
                    out[t0 + i] += rnd.uniform(-1, 1) * math.exp(-i / SR * 90) * 0.12
    return out

def pat(s):
    """Parses a compact pattern: notes as MIDI ints separated by spaces, '.' = rest, '-' = hold."""
    res = []
    for tok in s.split():
        res.append(None if tok == "." else ("-" if tok == "-" else int(tok)))
    return res

def sam_theme():
    # A-minor synthwave: dramatic "TED talk gone wrong" energy. 8 bars, loops cleanly.
    bass = pat("45 - 45 . 45 - 57 . 45 - 45 . 43 - 55 .  41 - 41 . 41 - 53 . 43 - 43 . 43 - 55 . " * 2)
    lead = pat("69 - - - 72 - 71 - 69 - 67 - 64 - - -  65 - - - 69 - 67 - 64 - 62 - 64 - - - "
               "69 - - - 72 - 76 - 74 - 72 - 71 - - -  72 - 71 - 69 - 67 - 69 - - - - - . . ")
    arp = pat("57 60 64 60 " * 4 + "55 59 62 59 " * 4 + "53 57 60 57 " * 4 + "55 59 62 59 " * 4)
    drums = list("k.h.s.h.k.k.s.hb")
    return render_song(132, 8, bass, lead, arp, drums, "square")

def elephant_theme():
    # Bouncy C-major stomp with a lumbering bass: a big friendly-but-furious elephant.
    bass = pat("36 . 43 . 36 . 43 . 41 . 48 . 41 . 48 .  43 . 50 . 43 . 50 . 36 . 43 . 36 - 38 40 " * 2)
    lead = pat("72 . 76 . 79 - 76 . 77 . 81 . 79 - - .  79 . 77 . 76 . 74 . 72 - 74 - 76 - - . "
               "72 . 76 . 79 - 84 . 83 . 81 . 79 - - .  77 . 76 . 74 . 71 . 72 - - - - - . . ")
    arp = pat("60 64 67 64 " * 4 + "65 69 72 69 " * 4 + "67 71 74 71 " * 4 + "60 64 67 64 " * 4)
    drums = list("k...s...k.k.s..s")
    return render_song(118, 8, bass, lead, arp, drums, "tri")

def trumpet():
    """Elephant trumpet: brassy rising-then-falling blare with growl."""
    n = int(SR * 1.3); out = []; ph = 0.0; lp = LowPass(1800)
    for i in range(n):
        u = i / n
        f = 420 + 380 * math.sin(math.pi * min(1, u * 1.6)) - 200 * max(0, u - 0.6)
        f *= 1 + 0.02 * math.sin(2 * math.pi * 28 * u * 1.3)  # vibrato
        ph += f / SR
        x = saw(ph) + 0.5 * saw(ph * 2.01) + 0.3 * rnd.uniform(-1, 1) * (1 - u)
        env = min(1, u * 12) * (1 - u) ** 0.6
        out.append(lp(x) * env)
    return out

def boom():
    n = int(SR * 1.4); out = []; lp = LowPass(300)
    for i in range(n):
        t = i / SR
        x = math.sin(2 * math.pi * (35 + 60 * math.exp(-t * 8)) * t) * math.exp(-t * 3)
        x += lp(rnd.uniform(-1, 1)) * 2.5 * math.exp(-t * 4)
        out.append(x)
    return out

def applause():
    n = int(SR * 2.5); out = [0.0] * n
    for _ in range(900):  # hundreds of individual claps
        start = int(rnd.uniform(0, 2.2) * SR)
        amp = rnd.uniform(0.2, 0.6) * (1 - start / n) ** 0.5
        bright = rnd.uniform(0.3, 0.9)
        prev = 0.0
        for i in range(int(0.03 * SR)):
            if start + i < n:
                w = rnd.uniform(-1, 1)
                prev = prev + bright * (w - prev)
                out[start + i] += prev * amp * math.exp(-i / SR * 160)
    fade = int(0.4 * SR)
    for i in range(fade):
        out[n - fade + i] *= 1 - i / fade
    return out

def sting(rising=True):
    """Dramatic orchestral-hit style sting: stacked detuned saws + noise swell + low hit."""
    n = int(SR * 2.0); out = []; phs = [0.0] * 6; lp = LowPass(2500)
    chord = [45, 52, 57, 60, 64, 69] if rising else [41, 48, 53, 56, 60, 65]
    for i in range(n):
        t = i / SR; u = i / n
        x = 0.0
        for k, nn in enumerate(chord):
            phs[k] += note_hz(nn) * (1 + 0.004 * (k - 2.5)) / SR
            x += saw(phs[k])
        x = lp(x / 3) * (min(1, t * 40) * math.exp(-t * 1.6))
        x += math.sin(2 * math.pi * 55 * t) * math.exp(-t * 5) * 1.2
        x += rnd.uniform(-1, 1) * 0.4 * math.exp(-t * 9)
        out.append(x)
    return out

if __name__ == "__main__":
    os.makedirs(os.path.join(OUT, "boss"), exist_ok=True)
    import gen_sounds
    gen_sounds.OUT = os.path.join(OUT, "boss")
    write_ogg("trumpet", trumpet())
    write_ogg("boom", boom())
    write_ogg("applause", applause())
    write_ogg("intro_sting", sting(True))
    write_ogg("phase_sting", sting(False))
    gen_sounds.OUT = os.path.join(OUT, "music")
    os.makedirs(gen_sounds.OUT, exist_ok=True)
    write_ogg("sam_hyde_theme", sam_theme())
    write_ogg("elephant_mario_theme", elephant_theme())
    print("boss audio written")
