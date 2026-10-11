"""Synthesizes HydeMod's original lightsaber sounds (pure Python -> WAV -> OGG via ffmpeg).
Run: python3 tools/gen_sounds.py"""
import math, random, struct, subprocess, os, tempfile, wave

SR = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "hydemod", "sounds", "lightsaber")
rnd = random.Random(2070)

def write_ogg(name, samples):
    peak = max(1e-9, max(abs(s) for s in samples))
    data = b"".join(struct.pack("<h", int(max(-1, min(1, s / peak * 0.92)) * 32767)) for s in samples)
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        path = tmp.name
    with wave.open(path, "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(data)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", path, "-c:a", "libvorbis", "-q:a", "5",
                    os.path.join(OUT, name + ".ogg")], check=True)
    os.remove(path)

def saw(phase):
    return 2.0 * (phase - math.floor(phase + 0.5))

class LowPass:
    def __init__(self, cutoff):
        self.set(cutoff); self.y = 0.0
    def set(self, cutoff):
        rc = 1.0 / (2 * math.pi * cutoff); self.a = (1.0 / SR) / (rc + 1.0 / SR)
    def __call__(self, x):
        self.y += self.a * (x - self.y); return self.y

def hum_voice(t, f, ph):
    """The core hum: two slightly detuned buzzy tones beating against each other plus a sub harmonic."""
    return (0.55 * saw(ph[0]) + 0.45 * saw(ph[1]) + 0.35 * math.sin(2 * math.pi * ph[2]))

def hum_loop(seconds=2.0):
    # Frequencies chosen so each completes a whole number of cycles in `seconds`: seamless loop.
    f1, f2, f3 = 91.0, 93.5, 45.5
    lp = LowPass(900); out = []
    n = int(SR * seconds)
    for i in range(n):
        t = i / SR
        wob = 1.0 + 0.08 * math.sin(2 * math.pi * 1.5 * t)  # 3 wobbles per loop
        x = (0.55 * saw(f1 * t) + 0.45 * saw(f2 * t) + 0.35 * math.sin(2 * math.pi * f3 * t)) * wob
        out.append(lp(x))
    # warm the filter by rendering twice so the loop point has no click
    lp2 = LowPass(900); first = []
    for i in range(n):
        t = i / SR
        wob = 1.0 + 0.08 * math.sin(2 * math.pi * 1.5 * t)
        first.append((0.55 * saw(f1 * t) + 0.45 * saw(f2 * t) + 0.35 * math.sin(2 * math.pi * f3 * t)) * wob)
    for x in first: lp2(x)
    return [lp2(x) for x in first]

def sweep(seconds, f_start, f_end, attack_burst=True, reverse_env=False):
    n = int(SR * seconds); out = []
    p1 = p2 = p3 = 0.0; lp = LowPass(400)
    for i in range(n):
        u = i / n
        k = u ** 0.6
        f = f_start + (f_end - f_start) * k
        p1 += f / SR; p2 += f * 1.027 / SR; p3 += f * 0.5 / SR
        lp.set(300 + 2500 * (1 - abs(0.35 - u)) * (0.4 + 0.6 * (1 - u if not reverse_env else u)))
        x = 0.55 * saw(p1) + 0.45 * saw(p2) + 0.35 * math.sin(2 * math.pi * p3)
        x = lp(x)
        if attack_burst:
            burst_t = u if not reverse_env else 1 - u
            x += rnd.uniform(-1, 1) * 0.9 * math.exp(-burst_t * 22)  # snap-hiss
            if rnd.random() < 0.004 * (1 - burst_t):
                x += rnd.uniform(-1, 1) * 1.2  # crackle
        env = min(1.0, u * 30) * (1 - u) ** 0.4 if not reverse_env else (1 - u) ** 1.6 * min(1.0, u * 60)
        out.append(x * env)
    return out

def swing(seconds, pitch, seed):
    r = random.Random(seed); n = int(SR * seconds); out = []
    p1 = p2 = p3 = 0.0; lp = LowPass(600)
    for i in range(n):
        u = i / n
        dop = 1.0 + 0.55 * math.exp(-((u - 0.4) / 0.18) ** 2)  # doppler pitch swell
        f = 92.0 * pitch * dop
        p1 += f / SR; p2 += f * 1.03 / SR; p3 += f * 0.5 / SR
        lp.set(500 + 3500 * (dop - 1) * 1.8)
        x = lp(0.55 * saw(p1) + 0.45 * saw(p2) + 0.4 * math.sin(2 * math.pi * p3))
        x += r.uniform(-1, 1) * 0.25 * (dop - 1)  # air rush
        env = math.sin(math.pi * u) ** 1.3
        out.append(x * env)
    return out

def clash(seconds=0.45):
    n = int(SR * seconds); out = []; ph = 0.0; lp = LowPass(5000)
    for i in range(n):
        u = i / n
        f = 520 * (1 - 0.5 * u)
        ph += f / SR
        zap = saw(ph) * math.exp(-u * 7)
        crackle = rnd.uniform(-1, 1) * (1.0 if rnd.random() < 0.12 else 0.25) * math.exp(-u * 5)
        out.append(lp(zap * 0.6 + crackle * 0.9) * min(1, u * 200))
    return out

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    write_ogg("hum", hum_loop(2.0))
    write_ogg("ignite", sweep(0.8, 30, 92, True, False))
    write_ogg("retract", sweep(0.6, 92, 25, True, True))
    for i, (secs, pitch) in enumerate([(0.55, 1.0), (0.45, 1.12), (0.65, 0.9)], 1):
        write_ogg(f"swing{i}", swing(secs, pitch, i))
    write_ogg("hit", clash())
    print("sounds written to", os.path.abspath(OUT))
