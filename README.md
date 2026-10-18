# HydeMod 1.5

A Minecraft 26.2 mod for NeoForge 26.2.0.90, with two animated boss encounters, kyber lightsabers, linked warp pipes, and a collection of food, spells and adventure equipment. Requires Java 25.

## Play

Use `Play-HydeMod.bat` from the parent workspace's Windows distribution for automatic setup. For manual installation, build with `./gradlew build`, then put `build/libs/hydemod-1.5.0.jar` in the instance's `mods` folder. Clients and servers need the mod. JEI is optional; it is included in the Windows profile. The recipe book discovers mod recipes as you collect ingredients, and the HydeMod advancement tab tracks crafting, travel and boss victories.

- **Lightsabers:** craft a hilt and combine it with a kyber crystal. Right-click to ignite or retract. All four colors remain fully charged; attacking and holding the blade consume no energy. Old depleted sabers restore their stored charge when used or carried. Holstering retracts the blade. Charging stations remain compatible with existing worlds and FE cables.
- **Warp pipes:** craft two pipes and a Pipe Wrench. Click the first pipe with the wrench, then the second to create a two-way route, including across dimensions. Linked pipes glow and sparkle. Stand on a pipe and hold sneak for 0.6 seconds to travel; release sneak to cancel or before returning. The exit gives a small spring launch with slow falling. Sneak-click the exit with an empty hand to switch between launch and gentle arrival. Sneak-click with the wrench to unlink both ends. Blocked, wet or missing exits cannot teleport you into a wall; a blocked exit preserves the link. Relinking removes old reciprocal links.
- **Bosses:** use a TEDx Invitation for Sam Hyde or a Giant Goobers Box for Elephant Mario on clear, dry ground. Summoning fails without consuming the item if there is no room. Survival players must finish a nearby encounter before summoning another. Spawn eggs remain available for creative setups.

## Boss encounters

**Sam Hyde** has 360 health and two phases. His abilities announce a warning before they take effect. Sidestep trash volleys, turn away from him during “Close your eyes,” leave the maglev spark circle, and move out of the fixed trash-rain zone. Maglev provides a brief slow-falling effect. Grovlers are capped at five nearby. Attacks cycle through a varied selection, with recovery windows and an escalating second-phase theme. Deal at least 12 raw damage during his applause or clock-reset cast to interrupt it. His reset can only happen once, including after saving and reloading; his other healing has a 20-second cooldown. Victory drops his armor, microphone, core and other supplies.

**Elephant Mario** wears a red M cap and shirt, blue overalls with gold buttons, white gloves, and has a moustache, floppy ears and a curling trunk. His ground pound sends an expanding low shockwave: jump over it. Sidestep the dust trail before his charge; baiting him into a wall leaves him staggered. His snack heal can be interrupted with 10 raw damage and has a 20-second cooldown. At half health he grows stronger and chains three stomps, with time between their waves. Victory awards fruit, snacks and pipes.

Both encounters use synchronized attack animations, translated warnings, boss bars and victory effects. Combat does not destroy the arena. Sam's voice lines use real excerpts from the full “2070 Paradigm Shift” talk, with subtitles, positional mono audio and a playback cooldown that prevents overlapping lines. Boss music fades between phases and stays quiet enough for speech and attack cues.

## The Day Today newsroom

**Peter O'Hanraha-hanrahan** and **Chris Morris** are persistent, peaceful characters with original Minecraft skins and satirical dialogue. Craft a **Press Pass** using two paper, string and red dye. Use it on clear ground to call Peter; sneak-use to call Chris. The pass is reusable, works on Peaceful, and allows one of each reporter within 24 blocks in survival. Creative players can also use their spawn eggs.

Interact to cycle each character's four reports. When Chris stands within 12 blocks of Peter, he checks Peter's reporting. Give either reporter paper to receive an **Unverified Report**. Their bulletin progress survives saving and reloading.

| Item | Use |
| --- | --- |
| Stretched Twig of Peace | Applies Weakness and Slowness to hostiles within 10 blocks for 8 seconds; 15-second cooldown; double durability wear in the Nether |
| Satellite Infomometer | Measures your position, dimension, weather and nearby hostile count |
| News Microphone | Broadcasts your location to players within 32 blocks; 10-second cooldown |
| Fact Bomb | A consumable camera-flash effect that reveals hostiles within 16 blocks for 10 seconds |
| This Is Our War VHS | A collectible archive prop with an original synopsis |
| Unverified Report | A suspicious field report obtained by giving paper to either correspondent |

JEI shows the six newsroom crafting recipes. The **WAR!** sketch supplies the peace-twig, satellite-instrument, news-bomb and souvenir-video references; Peter's reporting character also draws on the economics segments. This content uses original, adapted dialogue and vanilla audio cues. No copied programme audio, video or complete transcript is included. [Reference notes](docs/day-today.md).

## Validate and develop

```sh
./gradlew build
./gradlew runGameTestServer
python3 tools/validate_assets.py
```

The Minecraft integration suite covers permanent lightsaber charge, holstering, healing interrupts, one-use resets, cancelable travel, clear and blocked exits, portal cooldowns, and persisted reset/pipe settings, and creative-tab item uniqueness. Asset validation checks sound registration, references, mono Vorbis format, voice durations, translations, advancement recipe rewards, and the model's texture coordinates. Run `./gradlew runClient` for visual and gameplay checks.

Asset generation uses Python 3, Pillow, and FFmpeg:

```sh
python3 tools/gen_data.py
python3 tools/gen_textures.py
python3 tools/gen_sounds.py
python3 tools/gen_boss_audio.py
python3 tools/gen_polish_audio.py
python3 tools/gen_day_today.py
python3 tools/import_sam_voice.py build/voice-source/full_talk.webm
python3 tools/validate_assets.py
```

`tools/sam_voice_clips.json` contains the 27 clip timings and source metadata. The full 19:23 audio recording is retained locally in `build/voice-source/full_talk.webm`; the jar includes only the short excerpts. To retrieve the source again with a current yt-dlp:

```sh
yt-dlp -f 'bestaudio/best' --write-info-json --write-auto-subs --sub-langs en-orig --sub-format vtt \
  -o 'build/voice-source/full_talk.%(ext)s' 'https://www.youtube.com/watch?v=4jRoatZizQ0'
```

See [CREDITS.md](CREDITS.md) for source attribution.
