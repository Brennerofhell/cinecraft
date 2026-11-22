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

## Digitalkamera: Speicherkarte

`cinecraft:memory_card_small` ist ein Exposure-Film (`MemoryCardItem extends FilmRollItem`, Farbe, Sensitivität 1.0,
kaum Rauschen). Die Digitalkamera fotografiert nur mit Karten, die Analogkamera nur mit echten Filmrollen (Sperre in `canTakePhoto`, nicht im Slot, weil Exposures Anhang-Menü die Original-Attachments braucht)
Fotos auf der Karte werden per Rezept `memory_card_readout` (Karte + Redstone) in einen
entwickelten Film umgewandelt, den Exposure normal anzeigt. Karte wird dabei verbraucht (Rezept-Logik von Exposure).
Ungetestet: Sperre im Anhang-Menü, Auslese-Rezept.

## Zubehör-Logik (Stand 2026-11-21, ungetestet)

| Item | Funktion | Umsetzung |
|---|---|---|
| `remote_shutter` | löst die erste bereite Kamera im Inventar aus, ohne Sucher (Abklingzeit 2 s) | `RemoteShutterItem`, Exposure `CameraItem.release` |
| `camera_strap` | solange ein Gurt im Inventar ist, lassen sich Kameras nicht wegwerfen | `StrapEvents` (`ItemTossEvent`), Tag `cinecraft:camera_straps` |

Offen laut Recherche: `lens_cap` und `camera_battery` brauchen ein Mixin auf `CameraItem.canTakePhoto` (kein Pre-Shot-Event),
`lens_hood` nur kosmetisch ohne Rendering-Mixin.

## Fahrplan (Stand 2026-11-21)

| Schritt | Inhalt | Stand |
|---|---|---|
| 0 | Im Spiel prüfen: Kamera, Filter, Modelle, Entwickeln | offen, braucht `runClient` |
| A | Filmregal nur für Film-Items, Zubehör anbinden (Akku, Gurt, Deckel, Blende), Doku | Regal erledigt, Zubehör offen |
| B | Filmaufnahme: `.ccf`-Format (`com.cinecraft.media`), Server-Speicher, Upload-Netzwerk, Film-ID als Data Component | Format gebaut, Rest offen |
| C | Projektion: Wiedergabe-Textur pro Projektion, mehrteilige Leinwand | offen |

Reihenfolge: 0, dann A, B, C. B und C lassen sich ohne Spieltest kaum prüfen.

## Offene Entscheidungen für dich

- Sollen die Zubehör-Items Pflicht werden (Foto braucht Filter) oder nur Bonus bleiben?
- Soll die Entwicklerstation Exposures Trank-Rezepte ersetzen oder daneben laufen?
- Wie weit soll die Filmproduktion gehen (nur Klappe/Take-Markierung oder echte Videoaufnahme)?

## Funktionsstand der Items (Stand 2026-11-21, alles ungetestet im Spiel)

| Item | Funktion |
|---|---|
| `camera_cage` | mit Kamera in der anderen Hand benutzen: Kamera erholt sich nach dem Foto doppelt so schnell |
| `battery_grip` | im Inventar: Akku lädt 200 statt 100 Fotos |
| `lens_hood` | als Filter im Filter-Slot: etwas mehr Kontrast (Daten, kein Code) |
| `camera_backpack` | Rechtsklick: 9 Slots für Foto-Ausrüstung (Inhalt im Item) |
| `tripod_head` | auf ein Stativ benutzen: dreht es (Schleichen: andere Richtung) |
| `directors_megaphone` | Rechtsklick ruft Action, Schleichen und Rechtsklick ruft Schnitt (Actionbar, 48 Blöcke) |

Noch ohne Funktion (brauchen die Videoaufnahme aus Stufe 4 oder die Abzug-Pipeline): `shotgun_microphone`, `wireless_microphone`,
`audio_recorder`, `film_reel`, `video_tape`, `photo_paper`, `negative_strip`, `photo_print`,
`developing_tank`, `darkroom_tongs`. Bauteile (`image_sensor` usw.) sind reine Craft-Zwischenprodukte.

## Stufe 4, Filmaufnahme (Stand 2026-11-21, MVP, ungetestet im Spiel)

- **Ausschalter:** Serverconfig `recording.enableRecording` (Standard `false`). Ohne sie lehnt der Server jeden Upload ab.
- **Ablauf:** Leeres `video_tape` in die Hand nehmen, Taste **K** (Steuerung, Kategorie CineCraft) startet und beendet die Aufnahme.
  Der Client greift Frames des Hauptbildes (`CAPTURE_FPS`, `CAPTURE_HEIGHT`, Standard 12 fps, 480p), kodiert sie als JPEG in einem
  Worker-Thread, packt sie in eine `.ccf`-Datei (`CcfFile`) und lädt sie in 16-KiB-Stücken hoch (16 Pakete pro Tick).
- **Server:** `FilmStore` prüft Größe, Reihenfolge, SHA-256 und Dateiformat, schreibt über Temp-Datei und legt unter
  `<Welt>/cinecraft/films/<UUID>.ccf` ab. Grenzen: `maxFilmSeconds`, `maxFilmBytes`, `totalStorageBytes`. Ein Upload pro Spieler.
- **Item:** Das Band speichert nur die Film-ID (`cinecraft:film_id`) und die Länge in Sekunden.
- **Aufnahme-Zubehör:** `timecode_generator` im Inventar brennt einen Timecode (HH:MM:SS:FF) unten links ins Bild, `follow_focus` legt Kinobalken (2,39:1) darüber. Beides wird im Worker-Thread beim Kodieren gezeichnet (ungetestet).
- **Klappe:** Klappt man während der Aufnahme (Rechtsklick mit `clapperboard`), blitzt das Bild kurz weiß auf und zeigt ca. eine Sekunde "SCENE x TAKE y" (ungetestet).
- **Noch offen:** Ton (Mikrofone, Audiorecorder) und vor allem die
  Wiedergabe (Stufe 5: Projektor und Leinwand zeigen den Film).

## Stufe 5, Wiedergabe (Stand 2026-11-21, MVP, ungetestet im Spiel)

- **Band einlegen:** Rechtsklick mit bespieltem `video_tape` auf den Projektor legt es ein (Schleichen + Rechtsklick mit leerer Hand
  nimmt es heraus, beim Abbauen fällt es heraus). Das Band liegt in der Block-Entity und wird zu den Clients synchronisiert.
- **Anzeige:** Ist der Projektor an, zeichnet `ProjectorRenderer` das Bild auf die erste `screen` in Blickrichtung (bis 12 Blöcke,
  feste Blöcke dazwischen blockieren). Die Leinwand muss dem Projektor zugewandt sein; ein Block zeigt das ganze Bild.
- **Laden:** `FilmCache` holt den Film stückweise vom Server (Anfrage pro Stück, daher keine Warteschlange auf dem Server) nach
  `<Spielordner>/cinecraft-cache/<UUID>.ccf`, `FilmPlayback` dekodiert das passende JPEG-Bild im Worker-Thread und lädt es in eine
  dynamische Textur. Der Film läuft in Schleife.
- **Große Leinwand:** Aneinander liegende `screen`-Blöcke einer Ebene bilden das größte volle Rechteck um den getroffenen Block (bis 16 breit/hoch); das Bild wird mit Rand eingepasst.
- **Synchron:** Die Filmposition ergibt sich aus der Weltzeit, alle Spieler sehen dieselbe Stelle. Ungenutzte Texturen werden nach 30 s freigegeben.
- **Offen:** Ton, Anzeige auf der Leinwand wenn der Projektor aus dem Bild ist (Renderdistanz).
