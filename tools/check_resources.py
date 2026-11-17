#!/usr/bin/env python3
"""Static resource check: every model/blockstate/geo reference points to an existing file. Usage: python3 tools/check_resources.py"""
import json, sys
from pathlib import Path
A = Path(__file__).resolve().parent.parent / "src/main/resources/assets/cinecraft"
errs = []


def rl(s):
    return s.split(":", 1) if ":" in s else ["minecraft", s]


def need(kind, ref, src):
    ns, p = rl(ref)
    if ns != "cinecraft":
        return
    f = A / (kind + "/" + p + (".png" if kind == "textures" else ".json"))
    if not f.exists():
        errs.append(f"{src}: missing {f.relative_to(A)}")


def models(m, src):
    for t in m.get("textures", {}).values():
        if not t.startswith("#"):
            need("textures", t, src)
    if "parent" in m:
        need("models", m["parent"], src)
    for k in ("base",):
        if k in m:
            models(m[k], src)
    for v in m.get("perspectives", {}).values():
        models(v, src)


for f in sorted((A / "models").rglob("*.json")):
    models(json.loads(f.read_text()), f.relative_to(A))
for f in sorted((A / "blockstates").glob("*.json")):
    d = json.loads(f.read_text())
    for v in d.get("variants", {}).values():
        for e in (v if isinstance(v, list) else [v]):
            need("models", e["model"], f.name)
lang = {l: json.loads((A / f"lang/{l}.json").read_text()) for l in ("en_us", "de_de")}
names = {f.stem for f in (A / "models/item").glob("*.json") if not f.stem.endswith(("_3d", "_icon", "_static"))}
for n in sorted(names):
    for l, d in lang.items():
        if f"item.cinecraft.{n}" not in d and f"block.cinecraft.{n}" not in d:
            errs.append(f"lang {l}: no name for {n}")
for f in sorted((A / "geo").rglob("*.geo.json")):
    ident = json.loads(f.read_text())["minecraft:geometry"][0]["description"]["identifier"]
    exp = "geometry." + f.name[: -len(".geo.json")]
    if ident != exp:
        errs.append(f"{f.name}: identifier {ident} != {exp}")
    anim = A / "animations" / f.parent.name / (f.name[: -len(".geo.json")] + ".animation.json")
    if not anim.exists():
        errs.append(f"{f.name}: no animation file")
print("\n".join(errs) or "ok: all references resolve")
sys.exit(1 if errs else 0)
