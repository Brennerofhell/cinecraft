# CineCraft

NeoForge-Mod für Minecraft 1.21.1 (NeoForge 21.1.252, Java 21): Film- und Fotokameras, Dunkelkammer, Filmproduktion
und Kino-Projektion. Namespace `cinecraft`, Lizenz MIT.

> **Stand:** Assets, Modelle, Registrierung und Animationen sind vorhanden. **Es gibt noch keine Spiellogik**
> (Kamera, Entwickeln, Projektion). Alle Items sind Platzhalter ohne Verhalten. Im Spiel (`runClient`) wurde
> bisher **nichts** visuell geprüft; geprüft wurde nur per Blockbench-Vorschau und `./gradlew compileJava`.

## Bauen und starten

```
./gradlew compileJava     # kompiliert (zuletzt erfolgreich)
./gradlew runClient       # Spiel starten (noch nicht ausgeführt)
```

Abhängigkeit: GeckoLib 4.9.3 (`software.bernie.geckolib:geckolib-neoforge-1.21.1`, Maven: Cloudsmith, siehe
`build.gradle` / `gradle.properties`). `neoforge.mods.toml` verlangt GeckoLib als Pflicht-Mod, Create ist optional.

## Inhalt

| Bereich | Anzahl | Wo | Doku |
|---|---|---|---|
| Blöcke | 8 | `registry/ModBlocks`, `block/` | `docs/MODELS.md` |
| Basis-Items | 7 | `registry/ModItems` | `docs/MODELS.md` |
| Gear-Items (Zubehör, Filter, Labor, Produktion, Bauteile) | 39 | `registry/ModGearItems` | `docs/GEAR_ITEMS.md` |
| Animierte Modelle (GeckoLib) | 2 | `geo/`, `animations/`, `client/` | `docs/GECKOLIB.md` |
| Architektur-Plan (Phasen 1–10) | – | – | `docs/ARCHITECTURE.md` |

Alle Items und Blöcke stehen im Creative-Tab "CineCraft" (`ModCreativeTabs.MAIN`). Sprachdateien: `lang/en_us.json`,
`lang/de_de.json`.

## Projektstruktur

```
build.gradle, gradle.properties        Build, Versionen (geckolib_version, mod_license)
src/main/java/com/cinecraft/
  CineCraft.java                       Mod-Einstieg, Registries anmelden
  config/ModConfig.java                Server-/Client-Konfiguration
  registry/                            ModBlocks, ModItems, ModGearItems, ModBlockEntities, ModCreativeTabs
  block/                               ShapedBlock (facing + Hitbox), ProjectorBlock(+Entity)
  item/ClapperboardItem.java           GeckoLib-Item mit Klappen-Animation
  client/                              Renderer, ClientSetup (nur Client)
src/main/resources/
  META-INF/neoforge.mods.toml
  assets/cinecraft/
    textures/{item,block}/             Icons und Modell-Sheets (PNG)
    models/{item,block}/               Java-Block-Modelle (JSON)
    blockstates/                       je Block, facing=north|east|south|west
    geo/, animations/                  GeckoLib-Modelle und -Animationen
    lang/                              en_us, de_de
art/                                   Generatoren, UV-Layouts, Vorschauen, TEXTURE_PLAN.md
docs/                                  Dokumentation
```

## Blöcke

| Block | Modell | Besonderheit |
|---|---|---|
| `tripod`, `screen`, `darkroom_enlarger`, `darkroom_safelight`, `developer_tray`, `developer_station`, `film_shelf` | Java-Block-JSON | `ShapedBlock`: Blickrichtung und gedrehte Hitbox |
| `projector` | GeckoLib | `ProjectorBlock` + Block Entity, Spulen und Kurbel drehen sich dauerhaft |

**Blickrichtung:** Die Modellfront zeigt nach Norden und beim Platzieren zum Spieler
(`facing = Spieler-Blickrichtung.opposite`). Blockstates drehen per `y` 0/90/180/270.

**Hitboxen:** Aus den Modellgrenzen abgeleitet und auf den Block (0–16) begrenzt. `developer_station` und
`film_shelf` sind volle Blöcke. Das Stativ-Modell ragt bis y=29, die Hitbox endet bei y=16.

## Basis-Items

`analog_camera`, `digital_camera`, `lens`, `film_roll`, `film_cartridge`, `memory_card_small`, `clapperboard`.
Die Kameras, das Objektiv, die Filmrolle, die Filmkassette und die digitale Kamera haben 3D-Modelle
(`models/item/<name>_3d.json`) und Icons (`<name>_icon.json`); `<name>.json` verbindet beide per `neoforge:separate_transforms` (Inventar und Rahmen: Icon, sonst 3D). `memory_card_small` ist
ein flaches Icon. `clapperboard` ist ein GeckoLib-Item (`clapperboard_static.json` ist das alte statische Modell).

## Workflow Texturen und Modelle

1. **Texturen** erzeugen die Skripte in `art/` deterministisch (Pillow):
   - `generate_textures.py` – Icons und Modell-Sheets der ersten 13 Assets (+ `uv_layouts/*.json`).
   - `generate_digital_camera_model.py` – Sheet, UV-Layout und 3D-Modell der digitalen Kamera.
   - `generate_gear_icons.py` – die 39 Gear-Icons (16×16).
   Bestehende Dateien werden nie überschrieben, außer mit `--force`.
2. **Modelle** liegen als Java-Block-JSON mit **Vanilla-UVs (0–16)** vor. Blockbench liest `texture_size` und rechnet
   daraus Pixel-UVs, das Spiel ignoriert `texture_size`. Die Pixel-UV-Originale liegen in `art/_backup_pixel_uv/`.
3. **Blockbench-MCP** (`.mcp.json`, `http://localhost:3000/bb-mcp`): Modelle laden, ansehen, animieren, GeckoLib
   exportieren. Details in `docs/GECKOLIB.md`.

## Offene Punkte

- **Test im Spiel:** Alles ungeprüft: Projektor-Animation, Klappen-Animation, `facing`, Hitboxen, Display-Transforms
  (GUI, Hand), die 39 Gear-Items.
- **GeckoLib-Format:** Geo-Dateien tragen `format_version 1.12.0` (von Hand gesetzt, siehe `docs/GECKOLIB.md`). Nach
  Neuexport aus Blockbench wieder anpassen.
- **Icon-Modelle** sind für 5 Items eingebunden (`separate_transforms`), im Spiel ungeprüft.
- **Gameplay:** keine. Phasen 3–10 aus `docs/ARCHITECTURE.md` stehen aus.
- **Texturen:** Gear-Icons sind 16×16, die ersten Icons 32×32 (unterschiedliche Pixeldichte im Inventar).
- `digital_camera_model.json`-Skript schreibt einen ungültigen `display.hand`-Eintrag, der von Hand entfernt wurde.
