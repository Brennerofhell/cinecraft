# CineCraft – Modelle (Phase B): Dokumentation

Stand: 2026-11-16 · Minecraft 1.21.1 · NeoForge 21.1.252 · Namespace `cinecraft`

> **Status: nicht visuell geprüft.** Alle Modelle wurden aus den UV-Layouts per Skript/Agent erzeugt und nur
> strukturell validiert (JSON, UV-Grenzen, Texturreferenzen). Weder Blockbench-Rendering noch `runClient`
> wurden bisher ausgeführt. Der Blockbench-MCP (`.mcp.json`, `http://localhost:3000/bb-mcp`) wurde nicht
> verwendet, weil Blockbench nur ein aktives Projekt hat und parallele Agenten sich überschrieben hätten.

> **Nachtrag 2026-11-16 (nach dem ersten Stand dieses Dokuments):**
> - Blockbench-MCP wurde inzwischen verwendet: alle 13 Modelle plus `digital_camera` wurden in Blockbench geladen und
>   angesehen (Proportionen, UVs, Orientierung ohne sichtbare Fehler). Im Spiel weiterhin ungeprüft.
> - `analog_camera` (GUI `y=-35`) und `lens` (GUI `[25,-135,0]`) zeigen laut Blockbench-Display-Vorschau die
>   Vorderseite; der Verdacht aus Abschnitt 5 ("zeigen evtl. die Rückseite") hat sich in der Vorschau nicht bestätigt.
> - Neu: `digital_camera` hat ein 3D-Modell (`models/item/digital_camera.json`, Sheet `digital_camera_model.png`,
>   Skript `art/generate_digital_camera_model.py`); das flache Icon heißt jetzt `digital_camera_icon.json`.
> - Neu: `facing` (alle 8 Blöcke, Blockstates `facing=north|east|south|west`) und Hitboxen (`block/ShapedBlock`).
>   Damit sind die Punkte "Kein `facing`" und "Hitboxen/VoxelShapes fehlen" in Abschnitt 5 erledigt (Stativ-Kopf
>   ragt weiter über die Hitbox hinaus).
> - Neu: `projector` und `clapperboard` sind GeckoLib-Modelle mit Animation, siehe `docs/GECKOLIB.md`. Die
>   Java-Block-Modelle `projector.json` (Block) bleiben als Rückfall; `clapperboard_static.json` ist das alte Modell.
> - `memory_card_small` bleibt ein flaches Icon-Modell.
> - 39 weitere Items: `docs/GEAR_ITEMS.md`.

## 1. Ergebnis

13 Java-Block-Modelle, direkt als JSON geschrieben (Pfad `src/main/resources/assets/cinecraft/`):

| Asset | Typ | Modell | Textur |
|---|---|---|---|
| tripod | Block | models/block/tripod.json | block/tripod (64) |
| projector | Block | models/block/projector.json | block/projector (64) |
| darkroom_enlarger | Block | models/block/darkroom_enlarger.json | block/darkroom_enlarger (64) |
| darkroom_safelight | Block | models/block/darkroom_safelight.json | block/darkroom_safelight (32) |
| developer_tray | Block | models/block/developer_tray.json | block/developer_tray (32) |
| developer_station | Block | models/block/developer_station.json | block/developer_station (64) |
| film_shelf | Block | models/block/film_shelf.json | block/film_shelf (64) |
| screen | Block | models/block/screen.json | block/screen (32) |
| analog_camera | Item | models/item/analog_camera.json | item/analog_camera_model (64) |
| film_roll | Item | models/item/film_roll.json | item/film_roll_model (32) |
| film_cartridge | Item | models/item/film_cartridge.json | item/film_cartridge_model (32) |
| lens | Item | models/item/lens.json | item/lens_model (64) |
| clapperboard | Item | models/item/clapperboard.json | item/clapperboard_model (64) |

Zusätzlich:
- `blockstates/<block>.json` für alle 8 Blöcke (jeweils nur Variante `""`, siehe Abschnitt 5).
- `models/item/<block>.json` (parent `cinecraft:block/<block>`) für alle 8 Blöcke.
- `models/item/<item>_icon.json` (`minecraft:item/generated`, layer0 = 32px-Icon) für die 5 Items. **Noch nicht
  eingebunden** – die Items nutzen aktuell das 3D-Modell `models/item/<item>.json`.
- Registrierung: `ModBlocks`, `ModItems`, `ModCreativeTabs`, `lang/en_us.json` (11 neue Einträge). `./gradlew compileJava --offline` lief erfolgreich.

`digital_camera` und `memory_card_small` hatten ursprünglich kein Modell (siehe Nachtrag oben).

## 2. Vorgehen

1. Bestandsaufnahme: Texturen + UV-Layouts (`art/uv_layouts/*.json`, Regionen `x,y,w,h` in Texturpixeln) existierten bereits (Phase A).
2. **Welle A (27 Subagenten):** 13 Modellierer (je ein Asset), 13 Verdrahter (Blockstate, Item-Definition bzw. Icon-Modell), 1 Registrar (Java + Lang). Gemeinsamer Brief: `art/_backup_pixel_uv/BRIEF.md`. Jeder Agent schrieb nur seine eigenen Dateien.
3. **Welle B (26 Subagenten):** 13 Validatoren (Skript: JSON gültig, `texture_size` = Layout, UVs in 0..size und innerhalb einer Region, Texturreferenzen, `from/to` in -16..32, keine Null-Dicke) und 13 Reviewer (read-only: Proportionen, Orientierung, Z-Fighting, UV-Verzerrung, Display). Brief: `art/_backup_pixel_uv/BRIEF_B.md`.
4. Insgesamt 53 Subagenten. Das Limit lag bei 20 gleichzeitigen Agenten; 6 Reviewer wurden nachgestartet.
5. Eigenes Nacharbeiten (Abschnitt 3 und 4).

## 3. Wichtigste Erkenntnis: UV-Koordinaten

Die Modellierer schrieben UVs in **Texturpixeln** mit `texture_size`. Vanilla 1.21.1 ignoriert `texture_size`
und rechnet UVs immer im Bereich **0–16** (Blockbench liest `texture_size`, das Spiel nicht). Deshalb wurden
alle 13 Modelle mit `uv_vanilla = uv_pixel * 16 / texture_size` umgerechnet.

- Skript: `art/_backup_pixel_uv/fix_uv_conversion.py` (enthält auch die Einzelkorrekturen aus Abschnitt 4a).
- Originale mit Pixel-UVs: `art/_backup_pixel_uv/block/` und `art/_backup_pixel_uv/item/` (Stand vor der Umrechnung, **vor** den Korrekturen aus 4b).
- **Folge:** Der Validator-Lauf (Pixel-UVs vs. Layout) ist auf den aktuellen Dateien nicht mehr direkt anwendbar. Neue Validierung muss die Umrechnung zurückrechnen (`uv * texture_size / 16`) oder gegen die Backups laufen.
- Das Skript ist **nicht idempotent**: nicht ein zweites Mal auf bereits umgerechnete Dateien anwenden.

## 4. Korrekturen

### 4a. Aus Validatoren/Reviewern, per Skript angewandt
- projector: `render_type: minecraft:cutout` (runde Spulentextur mit transparenten Ecken).
- darkroom_safelight: `housing` north/south UVs getauscht (Rotglas jetzt Nord = Front).
- analog_camera: `viewfinder` north-Fläche entfernt (koplanar zu `prism`, Z-Fighting); `film_window` auf x=1.49 (war unter Griff/Body verborgen).
- lens: `front_glass` bis z=3 verlängert (schwebte); `grip` from z=3.01 (Z-Fighting mit Hood).
- tripod (Validator): Griff-UV east/west um 1 px aus der Region `handle` korrigiert.

### 4b. Aus Reviews, danach angewandt
- tripod: alle Beine auf ±28° (vorher 22,5°), viertes Bein nach Norden ergänzt (Elementgruppe kopiert).
- developer_station: `tank_a` auf y=18 gekürzt (ragte bis 24); `hose` bis x=12 verlängert (Lücke zu `pipe`).
- film_shelf: verdeckte Winkel `bracket_0`/`bracket_1` entfernt.
- clapperboard: `hinge` z 6.4..8.6 (Z-Fighting mit `slate`).
- film_cartridge: GUI-Scale 1.35 (vorher 0.95).
- developer_tray: `fixed`-Rotation entfernt, GUI Scale 1.1 und Translation [0,2,0] (Werte geschätzt).
- Blockstates von projector, darkroom_enlarger, developer_station, film_shelf auf Einzelvariante `""` umgestellt: der Registrar hat die Blöcke ohne `facing`-Property registriert, die ursprünglichen `facing=…`-Varianten hätten im Spiel nicht gematcht.

## 5. Offene Punkte (nicht geändert)

**Verhalten / Java**
- Kein `facing` bei den Blöcken. Sobald `HorizontalDirectionalBlock` o. Ä. genutzt wird: Blockstates wieder mit y-Rotation 0/90/180/270 versehen (Front der Modelle = Norden).
- Hitboxen/VoxelShapes fehlen. Modelle ragen über den Block hinaus: tripod (bis y=29, z=21.5), projector (Spulen bis y=17), developer_station (jetzt ≤ 18 bzw. Shell), film_shelf (`label_holder` z=-0.5).
- Icon-Modelle (`*_icon.json`) sind noch nicht per Item-Model-Override o. Ä. eingebunden.
- `digital_camera`, `memory_card_small`: keine Modelle.

**Im Spiel prüfen**
- GUI-Rotation von `analog_camera` (y=-35) und `lens` ([25,-135,0]): zeigen evtl. Rück- statt Frontseite (laut Reviewern vermutet, nicht verifiziert).
- `hand`/`thirdperson`-Transforms aller Items sind geschätzt; Objektiv/Front sollte vom Spieler weg zeigen.
- Alle Display-Werte generell.

**UV-Qualität (kosmetisch, Reviewer-Vorschläge)**
- Diverse gestauchte Flächen: film_roll `wound_film` Seiten (4-fach), film_cartridge `label`/`can_body`, safelight `mount`, projector `body` east/west, clapperboard `slate` east/west und Text, enlarger `lens_cone` down (soll Linse zeigen, UV ca. [22,22,25,25] in Pixeln) und `easel` up.
- film_roll: Flansch-Rand-UVs in transparenter Zeile 0.
- film_cartridge: Körper quadratisch statt ~1,5:1.
- Optional: verdeckte Gegenflächen entfernen (film_shelf, film_cartridge), `head`/`thirdperson_lefthand`-Display ergänzen, `screen` hat nur `gui`-Display.
- Hinweis: UV-Korrekturen müssen jetzt in Vanilla-Einheiten (0–16) formuliert werden.

## 6. Nächste Schritte

1. `./gradlew runClient`, Items/Blöcke aus dem Creative-Tab ansehen, falsche Modelle melden.
2. Danach gezielte Korrekturen pro Modell; bei Bedarf Modelle in Blockbench laden (Pixel-UVs aus `art/_backup_pixel_uv/` sind dafür praktischer).
3. `facing` + VoxelShapes in Java, Icon-Modelle einbinden.
4. Phase 3 der Architektur (`docs/ARCHITECTURE.md`): Kamera-Logik.
