"""Check packaged sound references, clip format/timing, translations, textures and Elephant Mario UVs."""
import json
from pathlib import Path
import re
import subprocess
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/hydemod'
errors = []


def check(condition, description):
    if not condition: errors.append(description)


def unique_object(pairs):
    obj = {}
    for key, value in pairs:
        if key in obj: raise ValueError('Duplicate JSON key: ' + key)
        obj[key] = value
    return obj


for path in RES.rglob('*.json'):
    try: json.loads(path.read_text(), object_pairs_hook=unique_object)
    except (ValueError, OSError) as exc: errors.append(f'{path}: {exc}')
lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
sounds = json.loads((ASSETS / 'sounds.json').read_text())
manifest = json.loads((ROOT / 'tools/sam_voice_clips.json').read_text())['clips']
registered = set(re.findall(r'register\("([^"]+)"\)', (ROOT / 'src/main/java/com/hydemod/registry/ModSounds.java').read_text()))
registered.update('sam.voice.' + name for name in manifest)
check(registered == set(sounds), f'Sound registry and sounds.json differ: {registered ^ set(sounds)}')
for event, spec in sounds.items():
    check(spec.get('subtitle') in lang, 'Missing subtitle: ' + event)
    for sound in spec['sounds']:
        name = sound if isinstance(sound, str) else sound['name']
        if not name.startswith('hydemod:'): continue
        path = ASSETS / ('sounds/' + name.split(':', 1)[1] + '.ogg')
        check(path.exists(), 'Missing audio: ' + str(path))
        if not path.exists(): continue
        info = json.loads(subprocess.check_output(['ffprobe', '-v', 'error', '-show_streams', '-show_format', '-of', 'json', str(path)], text=True))
        stream = info['streams'][0]
        check(stream['codec_name'] == 'vorbis', 'Sound must use Vorbis: ' + name)
        check(stream['channels'] == 1, 'Positional sound must be mono: ' + name)
        check(int(stream['sample_rate']) == 44100, 'Unexpected sample rate: ' + name)
        if event.startswith('sam.voice.'):
            key = event.removeprefix('sam.voice.')
            expected = manifest[key]['end'] - manifest[key]['start']
            check(abs(float(info['format']['duration']) - expected) < 0.1, 'Incorrect voice duration: ' + key)
for path in (ASSETS / 'models').rglob('*.json'):
    model = json.loads(path.read_text())
    for texture in model.get('textures', {}).values():
        if texture.startswith('hydemod:'):
            check((ASSETS / ('textures/' + texture.split(':', 1)[1] + '.png')).exists(), 'Missing texture: ' + texture)
for path in (ROOT / 'src/main/java').rglob('*.java'):
    source = path.read_text()
    for key in re.findall(r'Component\.translatable\("([^"]+)"', source):
        if key.startswith(('tooltip.hydemod.', 'message.hydemod.', 'title.hydemod.', 'call.hydemod.', 'chat.hydemod.')):
            check(key.endswith('.') or key in lang, 'Missing translation: ' + key)
    for texture in re.findall(r'HydeMod\.id\("(textures/[^\"]+\.png)"\)', source):
        check((ASSETS / texture).exists(), 'Missing renderer texture: ' + texture)
source = (ROOT / 'src/main/java/com/hydemod/client/ElephantMarioModel.java').read_text()
atlas = Image.open(ASSETS / 'textures/entity/elephant_mario.png')
rects = set()
number = r'(-?\d+(?:\.\d+)?)[Ff]?'
box = re.compile(r'\.texOffs\((\d+),\s*(\d+)\)(?:\.mirror\(\))?\.addBox\(' + r'\s*,\s*'.join([number] * 6) + r'\)')
for match in box.finditer(source):
    u, v = map(int, match.group(1, 2)); x, y, z, w, h, d = map(float, match.group(3, 4, 5, 6, 7, 8))
    rect = (u, v, u + int(2 * (w + d)), v + int(h + d))
    check(rect[2] <= atlas.width and rect[3] <= atlas.height, 'Elephant UV out of bounds: ' + str(rect))
    rects.add(rect)
rects = sorted(rects)
for i, a in enumerate(rects):
    for b in rects[i + 1:]:
        check(not (max(a[0], b[0]) < min(a[2], b[2]) and max(a[1], b[1]) < min(a[3], b[3])),
              f'Elephant UV overlap: {a} and {b}')
check(len(rects) >= 10, 'Elephant UV parser did not cover the model')
check(atlas.size == (128, 128), 'Elephant texture dimensions must match the model')
for path in (RES / 'data/hydemod/advancement').rglob('*.json'):
    advancement = json.loads(path.read_text())
    for field in ('title', 'description'):
        key = advancement.get('display', {}).get(field, {}).get('translate')
        if key: check(key in lang, 'Missing advancement translation: ' + key)
    for recipe in advancement.get('rewards', {}).get('recipes', []):
        check((RES / ('data/hydemod/recipe/' + recipe.split(':')[1] + '.json')).exists(), 'Missing recipe reward: ' + recipe)
if errors:
    raise SystemExit('\n'.join(errors))
print(f'Validated {len(sounds)} sound events, {len(manifest)} voice clips, textures, translations, advancements and {len(rects)} disjoint Elephant Mario UV regions.')
