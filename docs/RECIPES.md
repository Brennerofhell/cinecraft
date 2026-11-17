# CineCraft – Rezepte, Loot und Tags

Stand 2026-11-17 · erzeugt von `tools/generate_data.py` (Datei bearbeiten, Skript laufen lassen, nicht die JSONs von Hand).
**Im Spiel ungetestet.** Die Rezepte sind Platzhalter-Balancing aus Vanilla-Zutaten; Komponenten verketten sich
(`optical_glass` → `precision_lens` → Objektiv/Kamera, `camera_circuit` + `image_sensor` → digitale Kamera).

- **54 Crafting-Rezepte + 2 Entwicklungs-Rezepte** (`data/cinecraft/recipe/`): alle 8 Blöcke und alle 46 Items.
- **Loot-Tabellen** (`data/cinecraft/loot_table/blocks/`): jeder Block droppt sich selbst.
- **Mining-Tags** (`data/minecraft/tags/block/mineable/`): Leinwand und Filmregal mit Axt, übrige Blöcke mit Spitzhacke.
- Format 1.21.1: Ordner `recipe` / `loot_table` / `tags/block` im Singular, Ergebnis als `{"id", "count"}`.

`·` im Muster = leerer Slot.

| Ergebnis | Menge | Art | Muster | Zutaten |
|---|---|---|---|---|
| `analog_camera` | 1 | geformt | ILI/I3I/I1I | `1`=optical_glass, `3`=shutter_mechanism, `I`=iron_ingot, `L`=leather |
| `audio_recorder` | 1 | geformt | III/IzI/I4I | `4`=camera_circuit, `I`=iron_ingot, `z`=note_block |
| `autofocus_module` | 1 | geformt | I4I/I2I/IRI | `2`=precision_lens, `4`=camera_circuit, `I`=iron_ingot, `R`=redstone |
| `battery_grip` | 1 | geformt | I^I/I^I/ILI | `I`=iron_ingot, `L`=leather, `^`=camera_battery |
| `camera_backpack` | 1 | geformt | LLL/LHL/LLL | `H`=iron_block, `L`=leather |
| `camera_battery` | 1 | geformt | III/RCR/III | `C`=copper_ingot, `I`=iron_ingot, `R`=redstone |
| `camera_cage` | 1 | geformt | IKI/K·K/IKI | `I`=iron_ingot, `K`=stick |
| `camera_circuit` | 1 | geformt | RCR/CDC/RCR | `C`=copper_ingot, `D`=gold_ingot, `R`=redstone |
| `camera_flash` | 1 | geformt | IgI/IRI/III | `I`=iron_ingot, `R`=redstone, `g`=glowstone_dust |
| `camera_strap` | 1 | geformt | LSL/L·L/LSL | `L`=leather, `S`=string |
| `clapperboard` | 1 | geformt | XrX/kkk/kkk | `X`=white_dye, `k`=black_wool, `r`=red_dye |
| `darkroom_enlarger` | 1 | geformt | ·1·/·I·/WIW | `1`=optical_glass, `I`=iron_ingot, `W`=oak_planks |
| `darkroom_safelight` | 1 | geformt | III/IrI/IOI | `I`=iron_ingot, `O`=redstone_torch, `r`=red_dye |
| `darkroom_tongs` | 1 | geformt | I·I/·I·/K·K | `I`=iron_ingot, `K`=stick |
| `developer_bottle` | 1 | formlos |  | glass_bottle, ink_sac, redstone |
| `developer_station` | 1 | geformt | III/#c$/IHI | `#`=developer_bottle, `$`=fixer_bottle, `H`=iron_block, `I`=iron_ingot, `c`=cauldron |
| `developer_tray` | 1 | geformt | I·I/III | `I`=iron_ingot |
| `developing_tank` | 1 | geformt | I·I/IPI/III | `I`=iron_ingot, `P`=glass_pane |
| `digital_camera` | 1 | geformt | I4I/526/III | `2`=precision_lens, `4`=camera_circuit, `5`=image_sensor, `6`=image_processor, `I`=iron_ingot |
| `directors_megaphone` | 1 | geformt | ··I/·IX/IXr | `I`=iron_ingot, `X`=white_dye, `r`=red_dye |
| `film_cartridge` | 1 | geformt | III/I9I/III | `9`=film_roll, `I`=iron_ingot |
| `film_reel` | 1 | geformt | ·I·/I9I/·I· | `9`=film_roll, `I`=iron_ingot |
| `film_roll` | 2 | geformt | TBT/TST/TBT | `B`=black_dye, `S`=string, `T`=paper |
| `film_shelf` | 1 | geformt | WWW/9·9/WWW | `9`=film_roll, `W`=oak_planks |
| `fixer_bottle` | 1 | formlos |  | glass_bottle, ink_sac, quartz |
| `follow_focus` | 1 | geformt | III/IlI/·I· | `I`=iron_ingot, `l`=lever |
| `image_processor` | 1 | geformt | D4D/4A4/D4D | `4`=camera_circuit, `A`=amethyst_shard, `D`=gold_ingot |
| `image_sensor` | 1 | geformt | QDQ/D4D/QDQ | `4`=camera_circuit, `D`=gold_ingot, `Q`=quartz |
| `lens` | 1 | geformt | I1I/I2I/III | `1`=optical_glass, `2`=precision_lens, `I`=iron_ingot |
| `lens_cap` | 1 | geformt | ·L·/LIL/·L· | `I`=iron_ingot, `L`=leather |
| `lens_hood` | 1 | geformt | BIB/I·I/BIB | `B`=black_dye, `I`=iron_ingot |
| `memory_card_small` | 2 | geformt | nn·/I4I/III | `4`=camera_circuit, `I`=iron_ingot, `n`=gold_nugget |
| `mist_filter` | 1 | geformt | ·N·/T1T/·N· | `1`=optical_glass, `N`=iron_nugget, `T`=paper |
| `nd_filter` | 1 | geformt | ·N·/B1B/·N· | `1`=optical_glass, `B`=black_dye, `N`=iron_nugget |
| `negative_strip` | 1 | formlos |  | film_roll, paper |
| `optical_glass` | 2 | formlos |  | glass, quartz |
| `photo_paper` | 4 | geformt | TTT/TXT/TTT | `T`=paper, `X`=white_dye |
| `photo_print` | 1 | formlos |  | photo_paper, developer_bottle |
| `polarizing_filter` | 1 | geformt | ·N·/A1A/·N· | `1`=optical_glass, `A`=amethyst_shard, `N`=iron_nugget |
| `precision_lens` | 1 | formlos |  | optical_glass, optical_glass, iron_ingot, gold_nugget |
| `projector` | 1 | geformt | III/9Q1/III | `1`=optical_glass, `9`=film_roll, `I`=iron_ingot, `Q`=quartz |
| `remote_shutter` | 1 | geformt | ·R·/IuI/III | `I`=iron_ingot, `R`=redstone, `u`=stone_button |
| `screen` | 1 | geformt | www/www/KKK | `K`=stick, `w`=white_wool |
| `shotgun_microphone` | 1 | geformt | ··I/·I·/BIK | `B`=black_dye, `I`=iron_ingot, `K`=stick |
| `shutter_mechanism` | 1 | geformt | IRI/ICI/·I· | `C`=copper_ingot, `I`=iron_ingot, `R`=redstone |
| `stabilization_module` | 1 | geformt | IsI/R4R/IsI | `4`=camera_circuit, `I`=iron_ingot, `R`=redstone, `s`=slime_ball |
| `star_filter` | 1 | geformt | ·N·/i1i/·N· | `1`=optical_glass, `N`=iron_nugget, `i`=iron_bars |
| `timecode_generator` | 1 | geformt | IRI/m4e/III | `4`=camera_circuit, `I`=iron_ingot, `R`=redstone, `e`=repeater, `m`=comparator |
| `tripod` | 1 | geformt | ·I·/I·I/K·K | `I`=iron_ingot, `K`=stick |
| `tripod_head` | 1 | geformt | ·I·/IlI/·I· | `I`=iron_ingot, `l`=lever |
| `uv_filter` | 1 | geformt | ·N·/N1N/·N· | `1`=optical_glass, `N`=iron_nugget |
| `variable_nd_filter` | 1 | formlos |  | nd_filter, polarizing_filter, black_dye |
| `video_tape` | 1 | geformt | III/BCB/III | `B`=black_dye, `C`=copper_ingot, `I`=iron_ingot |
| `wireless_microphone` | 1 | geformt | ·R·/NIN/·B· | `B`=black_dye, `I`=iron_ingot, `N`=iron_nugget, `R`=redstone |

## Filmentwicklung mit CineCraft-Chemikalien

Zwei Rezepte vom Typ `exposure:film_developing` (Rezepttyp der Exposure-Mod, Film-Daten gehen automatisch auf den
entwickelten Film über). Sie gelten **zusätzlich** zu Exposures eigenen Trank-Rezepten, die weiter funktionieren.
Werkbank, Zutaten formlos:

| Rezept | Film (Tag) | Zutaten | Ergebnis |
|---|---|---|---|
| `black_and_white_film_developing` | `exposure:black_and_white_film_rolls` | `developer_bottle`, `fixer_bottle` | `exposure:developed_black_and_white_film` |
| `color_film_developing` | `exposure:color_film_rolls` | `developer_bottle`, `fixer_bottle`, Wasserflasche | `exposure:developed_color_film` |

`developer_bottle` und `fixer_bottle` geben beim Verbrauch eine leere Glasflasche zurück (`craftRemainder`).
Beides ist im Spiel ungetestet (offen: ob Exposure die Rückgabe der Flaschen beim Rezepttyp durchreicht).

## Entwicklerstation (Menü)

Rechtsklick auf die `developer_station` öffnet ein Menü: 3 Eingabe-Slots (Reihe in der Mitte) und ein Ergebnis-Slot.
Die Station fragt den Rezeptmanager mit einem 3×1-Crafting-Raster ab (`RecipeType.CRAFTING`). Exposures
`exposure:film_developing` ist ein Crafting-Rezept, also entwickelt die Station mit Exposures eigener Logik (Film-Daten
werden von Exposure übertragen); es gibt keinen eigenen Entwicklungscode. Es greifen auch alle anderen Rezepte mit
höchstens drei Zutaten, die in eine 3×1-Reihe passen. Die Eingaben bleiben im Block gespeichert und werden beim Abbauen
gedroppt. Code: `menu/DeveloperStationMenu`, `client/DeveloperStationScreen` (nutzt die Vanilla-Dispenser-Textur),
`registry/ModMenus`. Ungetestet, Slot-Positionen und Shift-Klick vor allem.
