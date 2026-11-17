#!/usr/bin/env python3
"""Generates data/: loot tables (blocks drop themselves), mineable tags and crafting recipes for all CineCraft
content. Vanilla ingredients only; CineCraft components chain into the cameras. Usage: python3 tools/generate_data.py"""
import json, shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data"
if DATA.exists():
    shutil.rmtree(DATA)
V = {  # vanilla shortcuts
    "I": "iron_ingot", "N": "iron_nugget", "G": "glass", "P": "glass_pane", "R": "redstone", "L": "leather",
    "Q": "quartz", "D": "gold_ingot", "n": "gold_nugget", "T": "paper", "S": "string", "B": "black_dye",
    "K": "stick", "C": "copper_ingot", "Z": "coal", "H": "iron_block", "W": "oak_planks", "A": "amethyst_shard",
    "E": "ender_pearl", "F": "flint", "l": "lever", "b": "bucket", "U": "glass_bottle", "O": "redstone_torch",
    "Y": "yellow_dye", "r": "red_dye", "X": "white_dye", "k": "black_wool", "w": "white_wool", "M": "map",
    "g": "glowstone_dust", "d": "diamond", "s": "slime_ball", "c": "cauldron", "p": "piston", "a": "clay_ball",
    "o": "ink_sac", "t": "tripwire_hook", "h": "hopper", "f": "furnace", "m": "comparator", "e": "repeater",
    "i": "iron_bars", "u": "stone_button", "j": "jukebox", "z": "note_block", "q": "spyglass",
}
OWN = {"1": "optical_glass", "2": "precision_lens", "3": "shutter_mechanism", "4": "camera_circuit",
       "5": "image_sensor", "6": "image_processor", "7": "autofocus_module", "8": "stabilization_module",
       "9": "film_roll", "!": "memory_card_small", "@": "photo_paper", "#": "developer_bottle",
       "$": "fixer_bottle", "%": "developing_tank", "^": "camera_battery", "&": "lens_cap", "v": "nd_filter", "y": "polarizing_filter"}


def item(c):
    return {"item": ("cinecraft:" + OWN[c]) if c in OWN else ("minecraft:" + V[c])}


def shaped(out, rows, count=1):
    keys = sorted({c for r in rows for c in r if c != " "})
    return {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": rows,
            "key": {k: item(k) for k in keys}, "result": {"id": "cinecraft:" + out, "count": count}}


def shapeless(out, chars, count=1):
    return {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [item(c) for c in chars],
            "result": {"id": "cinecraft:" + out, "count": count}}


R = {}
# --- components
R["optical_glass"] = shapeless("optical_glass", "GQ", 2)
R["precision_lens"] = shapeless("precision_lens", "11In")
R["shutter_mechanism"] = shaped("shutter_mechanism", ["IRI", "ICI", " I "])
R["camera_circuit"] = shaped("camera_circuit", ["RCR", "CDC", "RCR"])
R["image_sensor"] = shaped("image_sensor", ["QDQ", "D4D", "QDQ"])
R["image_processor"] = shaped("image_processor", ["D4D", "4A4", "D4D"])
R["autofocus_module"] = shaped("autofocus_module", ["I4I", "I2I", "IRI"])
R["stabilization_module"] = shaped("stabilization_module", ["IsI", "R4R", "IsI"])
# --- cameras and lens
R["analog_camera"] = shaped("analog_camera", ["ILI", "I3I", "I1I"])
R["digital_camera"] = shaped("digital_camera", ["I4I", "526", "III"])
R["lens"] = shaped("lens", ["I1I", "I2I", "III"])
R["memory_card_small"] = shaped("memory_card_small", ["nn ", "I4I", "III"], 2)
R["film_roll"] = shaped("film_roll", ["TBT", "TST", "TBT"], 2)
R["film_cartridge"] = shaped("film_cartridge", ["III", "I9I", "III"])
R["clapperboard"] = shaped("clapperboard", ["XrX", "kkk", "kkk"])
# --- accessories
R["camera_flash"] = shaped("camera_flash", ["IgI", "IRI", "III"])
R["camera_strap"] = shaped("camera_strap", ["LSL", "L L", "LSL"])
R["camera_battery"] = shaped("camera_battery", ["III", "RCR", "III"])
R["battery_grip"] = shaped("battery_grip", ["I^I", "I^I", "ILI"])
R["remote_shutter"] = shaped("remote_shutter", [" R ", "IuI", "III"])
R["lens_cap"] = shaped("lens_cap", [" L ", "LIL", " L "])
R["lens_hood"] = shaped("lens_hood", ["BIB", "I I", "BIB"])
R["camera_cage"] = shaped("camera_cage", ["IKI", "K K", "IKI"])
R["camera_backpack"] = shaped("camera_backpack", ["LLL", "LHL", "LLL"])
R["tripod_head"] = shaped("tripod_head", [" I ", "IlI", " I "])
# --- filters
R["uv_filter"] = shaped("uv_filter", [" N ", "N1N", " N "])
R["polarizing_filter"] = shaped("polarizing_filter", [" N ", "A1A", " N "])
R["nd_filter"] = shaped("nd_filter", [" N ", "B1B", " N "])
R["variable_nd_filter"] = shapeless("variable_nd_filter", "vyB")
R["mist_filter"] = shaped("mist_filter", [" N ", "T1T", " N "])
R["star_filter"] = shaped("star_filter", [" N ", "i1i", " N "])
# --- lab
R["photo_paper"] = shaped("photo_paper", ["TTT", "TXT", "TTT"], 4)
R["developer_bottle"] = shapeless("developer_bottle", "UoR")
R["fixer_bottle"] = shapeless("fixer_bottle", "UoQ")
R["developing_tank"] = shaped("developing_tank", ["I I", "IPI", "III"])
R["negative_strip"] = shapeless("negative_strip", "9T")
R["photo_print"] = shapeless("photo_print", "@#")
R["darkroom_tongs"] = shaped("darkroom_tongs", ["I I", " I ", "K K"])
# --- production
R["shotgun_microphone"] = shaped("shotgun_microphone", ["  I", " I ", "BIK"])
R["wireless_microphone"] = shaped("wireless_microphone", [" R ", "NIN", " B "])
R["audio_recorder"] = shaped("audio_recorder", ["III", "IzI", "I4I"])
R["directors_megaphone"] = shaped("directors_megaphone", ["  I", " IX", "IXr"])
R["film_reel"] = shaped("film_reel", [" I ", "I9I", " I "])
R["video_tape"] = shaped("video_tape", ["III", "BCB", "III"])
R["timecode_generator"] = shaped("timecode_generator", ["IRI", "m4e", "III"])
R["follow_focus"] = shaped("follow_focus", ["III", "IlI", " I "])
# --- blocks
R["tripod"] = shaped("tripod", [" I ", "I I", "K K"])
R["screen"] = shaped("screen", ["www", "www", "KKK"])
R["projector"] = shaped("projector", ["III", "9Q1", "III"])
R["darkroom_enlarger"] = shaped("darkroom_enlarger", [" 1 ", " I ", "WIW"])
R["darkroom_safelight"] = shaped("darkroom_safelight", ["III", "IrI", "IOI"])
R["developer_tray"] = shaped("developer_tray", ["I I", "III"])
R["developer_station"] = shaped("developer_station", ["III", "#c$", "IHI"])
R["film_shelf"] = shaped("film_shelf", ["WWW", "9 9", "WWW"])

# --- film developing with CineCraft chemicals (recipe type of the Exposure mod, exposure:film_developing)
WATER = {"type": "neoforge:components", "items": "minecraft:potion",
         "components": {"minecraft:potion_contents": {"potion": "minecraft:water"}}}
DEV = {"item": "cinecraft:developer_bottle"}
FIX = {"item": "cinecraft:fixer_bottle"}
DEVELOPING = {
    "black_and_white_film_developing": ("exposure:black_and_white_film_rolls",
                                        "exposure:developed_black_and_white_film", [[DEV], [FIX]]),
    "color_film_developing": ("exposure:color_film_rolls", "exposure:developed_color_film",
                              [[DEV], [FIX], [WATER]]),
}

BLOCKS = ["tripod", "screen", "projector", "darkroom_enlarger", "darkroom_safelight", "developer_tray",
          "developer_station", "film_shelf"]
WOODEN = {"screen", "film_shelf"}


def write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n")


for name, rec in R.items():
    write(DATA / "cinecraft/recipe" / f"{name}.json", rec)
for name, (tag, result, ingredients) in DEVELOPING.items():
    write(DATA / "cinecraft/recipe" / f"{name}.json", {"type": "exposure:film_developing", "film": {"tag": tag},
                                                       "ingredients": ingredients, "result": {"id": result}})
for b in BLOCKS:
    write(DATA / "cinecraft/loot_table/blocks" / f"{b}.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
                   "entries": [{"type": "minecraft:item", "name": f"cinecraft:{b}"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
        "random_sequence": f"cinecraft:blocks/{b}"})
write(DATA / "minecraft/tags/block/mineable/pickaxe.json",
      {"replace": False, "values": [f"cinecraft:{b}" for b in BLOCKS if b not in WOODEN]})
write(DATA / "minecraft/tags/block/mineable/axe.json",
      {"replace": False, "values": [f"cinecraft:{b}" for b in BLOCKS if b in WOODEN]})
print(len(R) + len(DEVELOPING), "recipes,", len(BLOCKS), "loot tables")
