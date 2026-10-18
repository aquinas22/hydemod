"""Regenerate polish translations, progression, recipe book unlocks and the empty test arena."""
import gzip
import json
from pathlib import Path
import struct

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/hydemod'
DATA = RES / 'data/hydemod'


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n')


def inventory(items):
    return {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': items}]}}


def kill(entity):
    return {'trigger': 'minecraft:player_killed_entity', 'conditions': {'entity': [
        {'condition': 'minecraft:entity_properties', 'entity': 'this', 'predicate': {'minecraft:entity_type': entity}}]}}


def main():
    lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
    lang.update(json.loads(Path(__file__).with_name('polish_lang.json').read_text()))
    voices = json.loads(Path(__file__).with_name('sam_voice_clips.json').read_text())['clips']
    for name, clip in voices.items():
        lang['boss.hydemod.sam_hyde.' + name] = clip['text']
        lang['subtitles.hydemod.sam.voice.' + name] = 'Sam Hyde: ' + clip['text']
    for key in ('message.hydemod.lightsaber.no_power', 'message.hydemod.lightsaber.depleted'):
        lang.pop(key, None)
    advancements = [
        ('root', 'tedx_invitation', 'Welcome to 2070', 'Crystals, warp pipes and two very unusual bosses.', None,
         {'mod_item': inventory(['hydemod:' + p.stem for p in sorted((ASSETS / 'items').glob('*.json'))]),
          'sam': kill('hydemod:sam_hyde'), 'elephant': kill('hydemod:elephant_mario')}, [['mod_item', 'sam', 'elephant']], 'task', 0),
        ('warp_network', 'warp_pipe', 'Now You Are Thinking with Pipes', 'Craft a pair of pipes and link them with a Pipe Wrench.', 'root',
         {'pipe': inventory(['hydemod:warp_pipe'])}, [['pipe']], 'task', 0),
        ('first_warp', 'pipe_wrench', 'Here We Go!', 'Travel through a linked warp pipe.', 'warp_network',
         {'travel': {'trigger': 'minecraft:impossible'}}, [['travel']], 'goal', 25),
        ('lightsaber', 'blue_lightsaber', 'Unlimited Power', 'Build a permanently charged lightsaber. Right-click to ignite.', 'root',
         {'saber': inventory(['hydemod:' + c + '_lightsaber' for c in ('green','blue','red','purple')])}, [['saber']], 'task', 0),
        ('defeat_sam', 'paradigm_shift_core', 'Paradigm Shifted', 'Defeat Sam Hyde and claim the Paradigm Shift Core.', 'lightsaber',
         {'boss': kill('hydemod:sam_hyde')}, [['boss']], 'challenge', 100),
        ('defeat_elephant', 'elephant_fruit', 'Elephant in the Room', 'Defeat Elephant Mario and claim his snacks.', 'root',
         {'boss': kill('hydemod:elephant_mario')}, [['boss']], 'challenge', 100),
        ('both_bosses', 'super_peanut', 'The Final Session', 'Defeat both bosses.', 'defeat_sam',
         {'sam': kill('hydemod:sam_hyde'), 'elephant': kill('hydemod:elephant_mario')}, [['sam'], ['elephant']], 'challenge', 150),
    ]
    for name, icon, title, description, parent, criteria, requirements, frame, xp in advancements:
        prefix = 'advancements.hydemod.' + name
        lang[prefix + '.title'] = title
        lang[prefix + '.description'] = description
        display = {'icon': {'id': 'hydemod:' + icon}, 'title': {'translate': prefix + '.title'},
                   'description': {'translate': prefix + '.description'}, 'frame': frame,
                   'show_toast': parent is not None, 'announce_to_chat': frame == 'challenge'}
        if parent is None: display['background'] = 'minecraft:gui/advancements/backgrounds/adventure'
        advancement = {'display': display, 'criteria': criteria, 'requirements': requirements}
        if parent: advancement['parent'] = 'hydemod:' + parent
        if xp: advancement['rewards'] = {'experience': xp}
        write(DATA / f'advancement/{name}.json', advancement)
    for path in sorted((DATA / 'recipe').glob('*.json')):
        recipe = json.loads(path.read_text())
        ingredients = recipe.get('ingredients', list(recipe.get('key', {}).values()))
        if not ingredients: ingredients = [recipe.get('ingredient')]
        items = sorted({x for x in ingredients if isinstance(x, str) and not x.startswith('#')})
        if not items: continue
        recipe_id = 'hydemod:' + path.stem
        write(DATA / f'advancement/recipes/{path.stem}.json', {
            'parent': 'minecraft:recipes/root',
            'criteria': {'has_ingredient': inventory(items), 'has_recipe': {
                'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipe': recipe_id}}},
            'requirements': [['has_ingredient', 'has_recipe']], 'rewards': {'recipes': [recipe_id]}})
    write(ASSETS / 'lang/en_us.json', lang)
    def name(s):
        b = s.encode(); return struct.pack('>H', len(b)) + b
    def tag(t, n, payload): return bytes([t]) + name(n) + payload
    payload = tag(3, 'DataVersion', struct.pack('>i', 5000))
    payload += tag(9, 'size', bytes([3]) + struct.pack('>i', 3) + struct.pack('>iii', 16, 8, 16))
    payload += tag(9, 'palette', bytes([10]) + struct.pack('>i', 1) + tag(8, 'Name', name('minecraft:air')) + b'\0')
    payload += tag(9, 'blocks', bytes([10]) + struct.pack('>i', 0)) + tag(9, 'entities', bytes([10]) + struct.pack('>i', 0)) + b'\0'
    structure = DATA / 'structure/test_empty.nbt'
    structure.parent.mkdir(parents=True, exist_ok=True)
    structure.write_bytes(gzip.compress(bytes([10]) + name('') + payload, mtime=0))
    print('Generated progression, recipe unlocks, translations and the integration test arena.')


if __name__ == '__main__':
    main()
