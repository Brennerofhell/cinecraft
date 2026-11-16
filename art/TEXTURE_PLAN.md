# CineCraft – Texturplan (Phase A: Texturen vor Modellen)

Namespace: `cinecraft` · Minecraft 1.21.1 · NeoForge 21.1.252
Stil: Vanilla-nah, pixelig, mehr Detailtiefe (32x32 / 64x64), gedämpfte Paletten, 1px-Lichtkanten, keine PBR.

## Konventionen
- **Icons** (`textures/item/<name>.png`, 32x32, transparent): Inventar-/GUI-Symbol, dunkle 1px-Outline.
- **Modell-Sheets** (`textures/item/<name>_model.png` bzw. `textures/block/<name>.png`): Material-Atlas mit
  benannten Rechtecken. Die Rechtecke stehen in `art/uv_layouts/<name>.json` und werden später in Blockbench
  per UV-Zuweisung verwendet. Nicht belegte Fläche bleibt transparent.
- Quelle (editierbar): `art/generate_textures.py` (Seed-deterministisch). Vorschau: `art/previews/`.
- Bestehende Dateien werden nie überschrieben (nur mit `--force`).

## Materialpalette
Chrom/gebürsteter Stahl · dunkles Metall · schwarzes Leatherette (Pebble) · Gummi (geriffelt) · Glas (blau/violett
entspiegelt, Rotglas, Bernstein) · Messing · Kunststoff schwarz/creme · Holz dunkel/hell · Papierlabel · Filmemulsion.

## Assets

| # | Asset | Datei | Grösse | Hauptmaterialien |
|---|---|---|---|---|
| 1 | Analoge 35-mm-Kamera (Icon) | item/analog_camera.png | 32 | Leatherette, Chrom, Glas, rote Taste |
| 1 | Analoge 35-mm-Kamera (Modell) | item/analog_camera_model.png | 64 | Chrom, Leder, Bajonett, Sucher, Rückspulknopf, Gummigriff |
| 2 | Filmrolle (Icon) | item/film_roll.png | 32 | Emulsion, Spule |
| 2 | Filmrolle (Modell) | item/film_roll_model.png | 32 | gewickelter Film, Flansch, Perforation |
| 3 | Filmkassette (Icon) | item/film_cartridge.png | 32 | Metalldose, gelbes Label |
| 3 | Filmkassette (Modell) | item/film_cartridge_model.png | 32 | Dose, Deckel, Label, Filzschlitz |
| 4 | Objektiv (Icon) | item/lens.png | 32 | Gummiring, Chrom, Skala |
| 4 | Objektiv (Modell) | item/lens_model.png | 64 | Gummi, Skalenring, Glas, Bajonett, Streulichtblende |
| 5 | Stativ | block/tripod.png | 64 | Stahlrohr, Gummifüsse, Kopf, Wasserwaage |
| 6 | Projektor | block/projector.png | 64 | Lackblech, Lüftung, Linse, Filmspule, Lampenglut |
| 7 | Filmklappe (Icon) | item/clapperboard.png | 32 | Schiefertafel, Streifen |
| 7 | Filmklappe (Modell) | item/clapperboard_model.png | 64 | Tafel, Klappstreifen, Scharnier |
| 8 | Dunkelkammer: Vergrösserer | block/darkroom_enlarger.png | 64 | Holzbrett, Säule, Balgen, Easel |
| 8 | Dunkelkammer: Dunkelkammerlampe | block/darkroom_safelight.png | 32 | Gehäuse, Rotglas |
| 8 | Dunkelkammer: Entwicklerschale | block/developer_tray.png | 32 | Kunststoffschale, Flüssigkeit, Zange |
| 9 | Entwicklerstation | block/developer_station.png | 64 | Edelstahl, Tanks, Etiketten, Rohre |
| 10 | Filmregal | block/film_shelf.png | 64 | Holz, Winkel, Filmdosen, Schachteln |

Stand 2026-11-16: `digital_camera`, `memory_card_small`, `screen` haben inzwischen Texturen (`textures/item/digital_camera.png`,
`memory_card_small.png`, `textures/block/screen.png`). Die digitale Kamera hat zusätzlich ein Modell-Sheet
(`digital_camera_model.png`, `art/generate_digital_camera_model.py`). 39 weitere Icons (16×16):
`art/generate_gear_icons.py`, Liste in `docs/GEAR_ITEMS.md`.

## Phasen
1. ✅ Plan (diese Datei)
2. ✅ Texturen generiert → `src/main/resources/assets/cinecraft/textures/`
3. ✅ Geprüft (Vorschau, UV-Layouts)
4. ✅ Modelle als Java-Block-JSON erzeugt (ohne Blockbench), validiert, reviewt – siehe `docs/MODELS.md`. Visuelle Prüfung im Spiel steht aus.
5. ✅ Gear-Icons (39×, 16×16) und digitale Kamera (Sheet + 3D-Modell) ergänzt – siehe `docs/GEAR_ITEMS.md`, `docs/MODELS.md`.
