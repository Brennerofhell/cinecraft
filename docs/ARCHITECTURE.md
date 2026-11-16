# CineCraft – Phase 1: Machbarkeitsanalyse & Architektur

Status-Legende: ✅ verifiziert (im Projekt getestet) · ⚠️ Annahme, noch zu prüfen · ❌ Problem

## 1. Zielplattform

| Punkt | Wert | Status |
|---|---|---|
| Minecraft | 1.21.1 | ⚠️ |
| NeoForge | 21.1.252 | ⚠️ Verfügbarkeit im Maven `maven.neoforged.net` noch nicht per Build bestätigt |
| Java | 21 | ⚠️ lokale JDK-Version noch nicht geprüft |
| Build | Gradle + ModDevGradle (`net.neoforged.moddev`) | ⚠️ Plugin-Version beim ersten Build bestätigen |

Verifikation: `./gradlew build` und `./gradlew runServer`. Bis dieser Lauf erfolgreich war, gilt nichts davon als bestätigt.

## 2. Frame-Erfassung (kritischster Punkt)

Ansatz: Am Ende des Frames (Event `RenderFrameEvent.Post` bzw. nach dem Level-Rendering) das Haupt-`RenderTarget` lesen.

- Lesen im Render-Thread: `glReadPixels` bzw. `NativeImage.downloadTexture` ist synchron und stallt die GPU-Pipeline.
- Besser: Skalierung auf 480p per Blit in ein eigenes kleines `TextureTarget`, dann asynchrones Auslesen über Pixel Buffer Objects (PBO, doppelt gepuffert), Ergebnis ein Frame später.
- Anschliessend Weitergabe der Bytes an eine **begrenzte** `ArrayBlockingQueue` (Drop-Policy: ältestes/neuestes Frame verwerfen, nie unbegrenzt wachsen).
- Kamerasicht: Eigene Kamera-Perspektive erfordert zweiten Level-Render-Pass (teuer, Konflikte mit Shader-Mods wie Iris möglich). MVP: Aufnahme aus der Spielerperspektive.

Risiken: Iris/Oculus-Shader, Framebuffer-Wechsel. Dokumentierte Einschränkung für MVP.

## 3. Videokompression

| Option | Lizenz | Bewertung |
|---|---|---|
| Eigenes MJPEG-in-Container (JPEG per `javax.imageio`) | JDK (GPL+CE) | Keine Abhängigkeit, funktioniert überall, Dateien gross. **Empfehlung für MVP.** |
| Jaffree/FFmpeg-Prozess | Apache 2 / FFmpeg LGPL/GPL | Externes Binary, Auslieferung/Plattform kritisch ❌ für Modpack |
| JCodec (H.264 pure Java) | BSD | Langsam, aber ohne Native; Option für Phase 10 |
| Bytedeco javacv/ffmpeg | Apache 2 + FFmpeg | Native Libs pro Plattform, gross |

MVP-Entscheidung: **MJPEG-Frames in eigener Containerdatei** (`.ccf`: Header, Frameindex, JPEG-Frames). 480p@12 FPS, JPEG-Qualität ~0.6 ergibt grob 25–40 KB/Frame, also ~0.3–0.5 MB/s. ⚠️ Zahlen sind Schätzungen, werden in Phase 3 gemessen.

## 4. Wiedergabe

Dekodierung im Hintergrundthread, Upload in eine `DynamicTexture`, ein Textur-Objekt pro aktiver Projektion (nicht pro Leinwandblock). Leinwandblöcke rendern Ausschnitte (UV) derselben Textur. (Phase 5)

## 5. Netzwerk

NeoForge Payload API (`RegisterPayloadHandlersEvent`, `CustomPacketPayload`, `StreamCodec`). Chunk-Grösse deutlich unter dem Payload-Limit (Serverbound 32 KiB, Clientbound ~1 MiB laut Vanilla; ⚠️ gegen 1.21.1 prüfen). Plan: 16 KiB-Chunks, SHA-256-Prüfsumme, Rate-Limit pro Spieler, max. gleichzeitige Uploads. (Phase 6)

## 6. Speicher (serverseitig)

`<world>/cinecraft/` mit `films/`, `meta/`, `thumbs/`, `projects/`, `tmp/`. Dateinamen ausschliesslich aus UUIDs, kein Spieler-String im Pfad; Auflösung via `Path.normalize()` + `startsWith(root)`-Prüfung. Schreiben über tmp-Datei + atomares `Files.move`.

## 7. Items

Items speichern nur eine Film-ID (UUID) per `DataComponent`, nie Videodaten.

## 8. Create / Exposure

⚠️ Installierte Create-Version des Twitch-Modpacks unbekannt (Modpack nicht im Projektordner). Create wird nur als optionale Abhängigkeit (`type = "optional"`) in Phase 9 angebunden, in separaten Klassen, die nur bei geladenem Mod angefasst werden. Exposure wird nicht verwendet; Inkompatibilität nicht erwartet, aber ungetestet.

## 9. Client/Server-Trennung

Alles Clientseitige unter `com.cinecraft.client` mit `@EventBusSubscriber(value = Dist.CLIENT)`. Gemeinsamer Code referenziert keine Client-Klassen. Test: `runServer`.

## 10. Paketstruktur

```
com.cinecraft
 ├─ CineCraft            (Mod-Einstieg)
 ├─ registry/            ModItems, ModBlocks, ModCreativeTabs (später BlockEntities, DataComponents, Menus, Sounds)
 ├─ config/              ModConfig (Server/Client)
 ├─ network/             Payloads (ab Phase 4/6)
 ├─ media/               Film-IDs, Speicher (Phase 4)
 └─ client/              Capture, Playback, Screens, Renderer
```

## 11. Offene Punkte

1. Build mit NeoForge 21.1.252 bestätigen.
2. Create-Version / Modpack-Liste beschaffen.
3. Capture-Verfahren prototypisch messen (Frametime-Einfluss).
