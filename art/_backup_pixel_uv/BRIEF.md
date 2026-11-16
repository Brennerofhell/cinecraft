# CineCraft Modell-Brief (gilt für alle Subagenten)

Projekt: /Users/danielt/Documents/programmieren/exposure revanced photography/cinecraft (NeoForge 1.21.1, Namespace `cinecraft`). Kein Git.
ASSETS = src/main/resources/assets/cinecraft
Texturen: ASSETS/textures/{block,item}/<name>.png ; UV-Regionen: art/uv_layouts/<layout>.json (Rechtecke x,y,w,h in Texturpixeln, Textur-Grösse im Feld "size"); Plan: art/TEXTURE_PLAN.md; Vorschau: art/previews/<name>.png.

Asset-Tabelle (layout = Name der JSON in art/uv_layouts; typ; Texturdatei):
- tripod: block, textures/block/tripod.png
- projector: block, textures/block/projector.png
- darkroom_enlarger: block, textures/block/darkroom_enlarger.png
- darkroom_safelight: block, textures/block/darkroom_safelight.png
- developer_tray: block, textures/block/developer_tray.png
- developer_station: block, textures/block/developer_station.png
- film_shelf: block, textures/block/film_shelf.png
- screen: block, textures/block/screen.png
- analog_camera_model: item, textures/item/analog_camera_model.png (Icon: analog_camera.png)
- film_roll_model: item, textures/item/film_roll_model.png (Icon: film_roll.png)
- film_cartridge_model: item, textures/item/film_cartridge_model.png (Icon: film_cartridge.png)
- lens_model: item, textures/item/lens_model.png (Icon: lens.png)
- clapperboard_model: item, textures/item/clapperboard_model.png (Icon: clapperboard.png)

Dateikonvention (jeder Agent schreibt NUR seine eigenen Dateien, nie fremde):
- Block-3D-Modell:  ASSETS/models/block/<name>.json  (name = Layout-Name; Items: ASSETS/models/item/<basis>.json mit basis = Layoutname ohne "_model", z.B. analog_camera)
- Blockstate:       ASSETS/blockstates/<name>.json
- Item-Definition Block-Item: ASSETS/models/item/<name>.json  -> {"parent":"cinecraft:block/<name>"}
- Texturreferenz im Modell: "cinecraft:block/<name>" bzw. "cinecraft:item/<layout>" – "texture_size":[w,h] aus Layout setzen.
- Java-Block-Modellformat 1.21.1: elements mit from/to (-16..32), faces mit uv [x1,y1,x2,y2] in TEXTURPIXELN bei texture_size (nicht 0-16!), "display"-Transforms für Items/Blöcke.
- Modelle müssen aus den benannten UV-Regionen gebaut sein (Region -> passende Cube-Fläche), realistisch proportioniert (Blockgrösse 16 Einheiten), Vanilla-JSON-valide.
- Keine Java-Dateien ändern ausser wenn Auftrag es ausdrücklich sagt. Keine bestehenden Texturen überschreiben.
- Antwort an den Aufrufer: max. 5 Zeilen (Datei(en), Auffälligkeiten).
