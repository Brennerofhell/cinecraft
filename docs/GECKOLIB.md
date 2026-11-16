# CineCraft – GeckoLib (animierte Modelle)

Stand: 2026-11-16 · GeckoLib 4.9.3 für NeoForge 1.21.1 · **im Spiel ungetestet** (nur `./gradlew compileJava`)

## Warum GeckoLib

Java-Block-JSON kennt keine Animationen, und Blockbench hat im Format "Java Block/Item" keinen Animationsmodus.
Animierte Modelle sind deshalb GeckoLib-Modelle (Blockbench-Format "GeckoLib Animated Model"). Die statischen
Java-Block-Modelle bleiben für alle übrigen Assets bestehen.

## Dateien und Pfade (GeckoLib 4)

| Zweck | Pfad (unter `assets/cinecraft/`) |
|---|---|
| Modell | `geo/block/projector.geo.json`, `geo/item/clapperboard.geo.json` |
| Animation | `animations/block/projector.animation.json`, `animations/item/clapperboard.animation.json` |
| Textur | `textures/block/projector.png`, `textures/item/clapperboard_model.png` |

Das Blockbench-Plugin schlägt `geckolib/models/...` vor; das ist die GeckoLib-5-Konvention. GeckoLib 4 liest
`geo/` und `animations/`. Beim Export deshalb in diese Pfade schreiben.

## Modelle

### Projektor (`projector`)
- Bones: `body` (Gehäuse, Linse, Lampe, Filmpfad), `reel_front`, `reel_back` (Pivot jeweils Spulenmitte), `crank`.
- Animation `animation.projector.run`: 2 s, Loop. Beide Spulen drehen um z (−360°), die Kurbel um x. Der Export
  schreibt die Kurbel mit −360 (Bedrock-Konvention).
- Code: `block/ProjectorBlock` (`BaseEntityBlock`, `RenderShape.ENTITYBLOCK_ANIMATED`, FACING),
  `block/ProjectorBlockEntity` (`GeoBlockEntity`, Controller `main` spielt `run` als Loop),
  `client/ProjectorRenderer` (`GeoBlockRenderer`), Registrierung in `registry/ModBlockEntities`,
  Renderer-Anmeldung in `client/ClientSetup`.
- Die Animation läuft immer; es gibt keinen Schalter (z. B. Redstone).

### Filmklappe (`clapperboard`)
- Bones: `board` (Tafel, Scharnier), `clapper_arm` (Pivot am Scharnier [1, 10.5, 7.5]).
- Animationen: `animation.clapperboard.idle` (Arm offen, 22,5°, Loop) und `animation.clapperboard.clap` (0,8 s,
  einmalig: zu, kurz halten, öffnen).
- Code: `item/ClapperboardItem` (`GeoItem`, Controller `main`, triggerbare Animation `clap` beim Rechtsklick,
  Server löst aus, `GeoItem.getOrAssignId`), `client/ClapperboardRenderer` (`GeoItemRenderer`, Textur
  `clapperboard_model.png`), Client-Anbindung über `initializeClient` (`IClientItemExtensions`).
- `models/item/clapperboard.json` ist `builtin/entity` (mit den alten Display-Werten),
  `clapperboard_static.json` das frühere Java-Block-Modell.

## Workflow: neues animiertes Modell mit dem Blockbench-MCP

1. Projekt vom Typ `geckolib_model` anlegen (`create_project`).
2. Java-Block-JSON laden: `Codecs.java_block.parse(json, pfad)`. **Danach UVs korrigieren:** das Plugin setzt
   `box_uv = true`, die Textur sieht dann zerhackt aus. Pro Cube `box_uv = false` setzen und die Face-UVs aus
   der JSON in Pixel umrechnen (`uv * texture_size / 16`).
3. Bones als Gruppen anlegen, Pivot setzen, Cubes zuordnen.
4. Animationen mit `create_animation` erstellen (Bones, Keyframes, Loop).
5. `geckolib_set_project_settings` (modid `cinecraft`, Typ Block oder Item, Modell-ID), `geckolib_validate_model`.
6. `geckolib_export_model` und `geckolib_export_animations` mit Zielpfad unter `geo/` bzw. `animations/`
   (`overwrite: true` bei Wiederholung).
7. Java: Block Entity oder Item (`GeoBlockEntity`/`GeoItem`) plus Renderer.

## Risiken und offene Punkte

- **Formatversion:** GeckoLib 4.9.3 kennt nur `1.12.0`, `1.14.0`, `1.21.0` (per `javap` am Jar geprüft). Das Plugin
  exportiert `1.21.110`; die beiden Geo-Dateien wurden von Hand auf `1.12.0` gesetzt. **Nach jedem Neuexport wieder
  anpassen.** Im Spiel weiterhin ungetestet.
- **GeckoLib-Version 4.9.3:** per Maven-Metadaten bestätigt, im Spiel nicht geprüft.
- **API-Änderung 4.9:** `GeoItem.makeRenderer` existiert nicht mehr; Items melden den Renderer über
  `initializeClient`.
- **Drehrichtung** der Animationen ist nur in der Blockbench-Vorschau gesehen.
- **Hand-Transforms** des Klappen-Items (`display` in `clapperboard.json`) sind geschätzt.
- Die Blockbench-Projekte `projector` und `clapperboard` wurden nicht als `.bbmodel` gespeichert. Das
  `clapperboard`-Projekt in Blockbench wurde später versehentlich überschrieben; die Exportdateien im Repo sind
  unversehrt. Neu aufbauen aus `geo/item/clapperboard.geo.json` (`from_geo_json`) ist möglich.
- Weitere Animationsideen (nicht gebaut): Kamera-Auslöser, Objektiv-Zoom, Entwicklerstation.
