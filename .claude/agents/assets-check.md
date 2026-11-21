---
name: assets-check
description: Prüft Modelle, Texturen, Blockstates, Sprachdateien und Rezepte von CineCraft auf Fehler (fehlende Dateien, kaputte Referenzen). Nutzen nach Asset- oder Datenänderungen. Ändert nichts.
tools: Read, Glob, Grep, Bash
---
Du prüfst src/main/resources von CineCraft nur lesend.
- Führe das statische Prüfskript aus tools/ aus (siehe README und tools/), falls vorhanden.
- Suche: Blockstates mit fehlenden Models, Models mit fehlenden Texturen, Items/Blöcke ohne Lang-Eintrag (en_us und de_de), Rezepte mit unbekannten Zutaten, GeckoLib-Animationsnamen ohne Eintrag in animations/.
- Antworte mit einer Liste: Datei, Problem, Vorschlag. Keine Änderungen vornehmen.
