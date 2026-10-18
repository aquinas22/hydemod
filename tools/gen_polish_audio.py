"""Original pipe chimes, warp sweeps and clear boss attack cues; no downloaded game sounds."""
import json
import math
from pathlib import Path
import random
import gen_sounds as synth

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/hydemod'
SR = synth.SR


def warp(rising):
    length = 0.65 if rising else 0.55
    rng = random.Random(2070 + int(rising))
    out = []
    phase = 0
    low = synth.LowPass(1700)
    for i in range(int(length * SR)):
        t = i / SR
        u = t / length
        freq = 130 * 2 ** (2.3 * u) if rising else 540 * 2 ** (-2.4 * u)
        phase += freq / SR
        envelope = math.sin(math.pi * u) ** 0.7
        bubbly = math.sin(2 * math.pi * 16 * t) * 0.15
        tone = 0.7 * math.sin(2 * math.pi * phase) + 0.15 * math.sin(4 * math.pi * phase)
        tone += rng.uniform(-1, 1) * 0.08 + bubbly
        out.append(low(tone) * envelope)
    return out


def chime(notes, spacing=0.12, length=0.8):
    out = [0.] * int(length * SR)
    for k, note in enumerate(notes):
        freq = 440 * 2 ** ((note - 69) / 12)
        start = int(k * spacing * SR)
        for i in range(len(out) - start):
            t = i / SR
            env = min(1, t * 180) * math.exp(-t * 8)
            out[start + i] += (math.sin(2 * math.pi * freq * t) + 0.18 * math.sin(2 * math.pi * freq * 2 * t)) * env
    return out


def main():
    sounds = json.loads((ASSETS / 'sounds.json').read_text())
    assets = {
        'pipe.enter': ('pipe', 'enter', warp(False)),
        'pipe.exit': ('pipe', 'exit', warp(True)),
        'pipe.link': ('pipe', 'link', chime([72, 76, 79])),
        'pipe.fail': ('pipe', 'fail', chime([53, 48], length=0.45)),
        'boss.telegraph': ('boss', 'telegraph', chime([76, 79], spacing=0.16, length=0.5)),
        'boss.stagger': ('boss', 'stagger', chime([55, 48, 43], spacing=0.07, length=0.7)),
    }
    for event, (folder, name, samples) in assets.items():
        synth.OUT = str(ASSETS / 'sounds' / folder)
        Path(synth.OUT).mkdir(parents=True, exist_ok=True)
        synth.write_ogg(name, samples)
        sounds[event] = {'subtitle': 'subtitles.hydemod.' + event,
                         'sounds': [{'name': f'hydemod:{folder}/{name}', 'volume': 0.75}]}
    for name in ('sam_hyde', 'elephant_mario'):
        event = f'music.{name}_phase_two'
        sounds[event] = {'subtitle': 'subtitles.hydemod.' + event,
                         'sounds': [{'name': f'hydemod:music/{name}_phase_two', 'stream': True}]}
    (ASSETS / 'sounds.json').write_text(json.dumps(sounds, indent=2) + '\n')
    print('Generated six original pipe and combat sounds.')


if __name__ == '__main__':
    main()
