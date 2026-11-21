# CineCraft – Design: was jedes Item künftig tun soll

Stand 2026-11-17 · **reine Planung, nichts davon ist gebaut.** Heute sind alle Items Platzhalter (siehe `README.md`).
Gesichert aus dem Exposure-Quellcode (Branch 1.21.1): Entwickeln ist das Rezept `exposure:film_developing`, es gibt das
serverseitige Event `FrameAddedEvent`, Filme tragen ihre Daten in Data Components. **Alles andere hier sind Ideen,
die erst gegen Exposure geprüft werden müssen** (Spalte "Prüfen").

## Grundsatz

CineCraft ergänzt Exposure und ersetzt es nicht. Wo Exposure schon etwas kann (Kamera, Film, Entwickeln, Fotos),
hängen wir uns über Rezepte, Events und Tags an. Eigene Spiellogik nur dort, wo Exposure nichts bietet
(Filmproduktion, Projektion, Dunkelkammer-Arbeitsblöcke).

## Stufen

| Stufe | Inhalt | Risiko | Stand |
|---|---|---|---|
| 1 | Rezepte, Drops, Tags, Entwickeln über `exposure:film_developing` | gering | gebaut, ungetestet |
| 2 | Entwicklerstation als Block mit Menü (Film, Entwickler, Fixierer → entwickelter Film) | mittel (GUI) | gebaut (kompiliert), ungetestet |
| 3 | Zubehör wirkt auf Exposure-Fotos (Filter, Blitz, Objektiv) | mittel (datengesteuert über Exposure-Tags) | Filter, Objektive (`lens`, `precision_lens`) und `camera_flash` als Daten angelegt, ungetestet |
| 4 | Filmproduktion: Klappe, Mikrofone, Rolle → Aufnahme | hoch (siehe `ARCHITECTURE.md`) | Idee |
| 5 | Projektion: Projektor + Leinwand zeigen einen Film | hoch (Rendering, Netzwerk) | Idee |

## Pro Gruppe

| Gruppe | Items / Blöcke | Künftige Funktion | Prüfen |
|---|---|---|---|
| Kameras | `analog_camera`, `digital_camera`, `lens` | Animationen laufen schon; Funktion bleibt bei Exposures Kamera, unsere Items dienen als Dekoration/Basis für Stufe 3 | Ob unsere Items Exposure-Kamera-Slots (Filter, Objektiv) füllen können |
| Filter | `uv_filter` … `star_filter` | Vorsatzfilter, die einem Foto einen Look geben | Ob Exposure Vorsätze per Datapack/API erweiterbar sind |
| Zubehör | `camera_flash`, `camera_strap`, `camera_battery`, `battery_grip`, `remote_shutter`, `lens_cap`, `lens_hood`, `camera_cage`, `camera_backpack`, `tripod_head` | Blitz/Auslöser/Halterung; `camera_backpack` als Lagerplatz für Film und Zubehör | Exposure-Blitzlogik; Data-Component-Inventar |
| Labor | `developer_bottle`, `fixer_bottle`, `developing_tank`, `darkroom_tongs`, `photo_paper`, `negative_strip`, `photo_print` | Flaschen entwickeln Film (Stufe 1, gebaut); Rest dient Stufe 2 und der Foto-Pipeline | Ob Exposure Abzüge/Papier-Rezepte erweiterbar sind |
| Labor-Blöcke | `developer_station`, `developer_tray`, `darkroom_enlarger`, `darkroom_safelight` | Station = Menü (Stufe 2); Rest Deko bzw. Rahmen für die Pipeline | – |
| Produktion | `shotgun_microphone`, `wireless_microphone`, `audio_recorder`, `directors_megaphone`, `film_reel`, `video_tape`, `timecode_generator`, `follow_focus`, `clapperboard` | Gerüst für die Filmaufnahme; Klappe markiert Takes | Gesamte Stufe 4 |
| Projektion | `projector`, `screen`, `tripod`, `film_shelf` | Projektor + Leinwand zeigen einen Film; Regal lagert Rollen | Gesamte Stufe 5 |
| Bauteile | `optical_glass` … `stabilization_module`, `image_sensor`, `image_processor`, `autofocus_module`, `camera_circuit`, `shutter_mechanism`, `precision_lens` | Reine Craft-Zwischenprodukte (Rezeptkette, `docs/RECIPES.md`) | – |

## Empfehlung zur Reihenfolge

1. **Test von Stufe 1 im Spiel** (Modelle laden, Rezepte, Entwickeln, Animationen). Ergebnisse bestimmen alles Weitere.
2. **Stufe 2** (Entwicklerstation mit Menü): überschaubar, nutzt vorhandene Exposure-Rezepte als Logik.
3. Vor Stufe 3 klären, was Exposure für Erweiterungen anbietet (Dokumentation und `docs/for_developers` im Repo lesen).
4. Stufe 4 und 5 sind eigene Großprojekte; Plan in `docs/ARCHITECTURE.md` (MJPEG-Container, Netzwerk, Wiedergabe).

## Offene Entscheidungen für dich

- Sollen die Zubehör-Items Pflicht werden (Foto braucht Filter) oder nur Bonus bleiben?
- Soll die Entwicklerstation Exposures Trank-Rezepte ersetzen oder daneben laufen?
- Wie weit soll die Filmproduktion gehen (nur Klappe/Take-Markierung oder echte Videoaufnahme)?
