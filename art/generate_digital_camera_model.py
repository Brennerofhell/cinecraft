#!/usr/bin/env python3
"""Digital camera: 64x64 model sheet + UV layout + Java-block model (vanilla UVs 0-16).
Usage: python3 art/generate_digital_camera_model.py [--force]
"""
import json, random, sys
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources/assets/cinecraft"
TEXP = RES / "textures/item/digital_camera_model.png"
MODP = RES / "models/item/digital_camera.json"
UVP = ROOT / "art/uv_layouts/digital_camera.json"
N = 64
rng = random.Random(7)

# name, from, to, base colour, row-y, x
CUBES = [
    ("body",   (2, 3, 5),  (14, 10, 9),  (44, 46, 54)),
    ("barrel", (5, 4, 2),  (11, 10, 5),  (30, 31, 37)),
    ("glass",  (6, 5, 1),  (10, 9, 2),   (24, 30, 90)),
    ("grip",   (11, 3, 3), (14, 10, 5),  (24, 24, 28)),
    ("evf",    (6, 10, 6), (10, 12, 8),  (36, 38, 44)),
    ("shutter", (11, 10, 6), (13, 11, 8), (190, 36, 36)),
    ("dial",   (3, 10, 6), (5, 11, 8),   (190, 195, 205)),
    ("screen", (3, 4, 9),  (11, 9, 10),  (22, 28, 40)),
]
ORDER_ROWS = [["body", "barrel", "glass"], ["grip", "evf", "shutter", "dial", "screen"]]

im = Image.new("RGBA", (N, N), (0, 0, 0, 0))
px = im.load()


def shade(c, d):
    return tuple(max(0, min(255, v + d)) for v in c)


def fill(x, y, w, h, c, noise=5):
    for j in range(y, y + h):
        for i in range(x, x + w):
            px[i, j] = shade(c, rng.randint(-noise, noise)) + (255,)


layout, cubes_out = {}, []
y = 0
for row in ORDER_ROWS:
    x, rowh = 0, 0
    for name in row:
        _, f, t, col = next(c for c in CUBES if c[0] == name)
        dx, dy, dz = (t[k] - f[k] for k in range(3))
        w, h = 2 * dz + 2 * dx, dz + dy
        r = dict(top=(dz, 0, dx, dz), bottom=(dz + dx, 0, dx, dz), west=(0, dz, dz, dy), north=(dz, dz, dx, dy),
                 east=(dz + dx, dz, dz, dy), south=(2 * dz + dx, dz, dx, dy))
        for face, (rx, ry, rw, rh) in r.items():
            c = col
            if face == "top":
                c = shade(col, 14)
            elif face == "bottom":
                c = shade(col, -14)
            fill(x + rx, y + ry, rw, rh, c)
        layout[name] = dict(x=x, y=y, w=w, h=h)
        faces = {}
        for face, (rx, ry, rw, rh) in r.items():
            u = [(x + rx) / 4, (y + ry) / 4, (x + rx + rw) / 4, (y + ry + rh) / 4]
            faces[face] = dict(uv=u, texture="#0")
        cubes_out.append(dict(name=name, **{"from": list(f), "to": list(t)}, faces=faces))
        x += w
        rowh = max(rowh, h)
    y += rowh

# ---- details
def reg(name, face):
    c = next(c for c in CUBES if c[0] == name)
    f, t = c[1], c[2]
    dx, dy, dz = (t[k] - f[k] for k in range(3))
    L = layout[name]
    rx, ry, rw, rh = dict(north=(dz, dz, dx, dy), south=(2 * dz + dx, dz, dx, dy), top=(dz, 0, dx, dz),
                          west=(0, dz, dz, dy), east=(dz + dx, dz, dz, dy))[face]
    return L["x"] + rx, L["y"] + ry, rw, rh


# glass front: concentric blue/violet
gx, gy, gw, gh = reg("glass", "north")
for j in range(gh):
    for i in range(gw):
        d = max(abs(i - (gw - 1) / 2), abs(j - (gh - 1) / 2))
        px[gx + i, gy + j] = ((40, 44, 70) if d > 1.4 else (30, 42, 130) if d > 0.6 else (70, 90, 190)) + (255,)
px[gx + 1, gy + 1] = (210, 220, 255, 255)
# screen back: glow + UI bars
sx, sy, sw, sh = reg("screen", "south")
fill(sx, sy, sw, sh, (30, 52, 78), 3)
for i in range(sw):
    px[sx + i, sy] = (12, 14, 18, 255)
    px[sx + i, sy + sh - 1] = (12, 14, 18, 255)
for i in range(1, 4):
    px[sx + i, sy + sh - 2] = (110, 200, 130, 255)
px[sx + sw - 2, sy + 1] = (220, 60, 60, 255)
# barrel front ring (barrel north face)
bx, by, bw, bh = reg("barrel", "north")
for j in range(bh):
    for i in range(bw):
        e = min(i, j, bw - 1 - i, bh - 1 - j)
        if e == 0:
            px[bx + i, by + j] = (150, 156, 168, 255)
        elif e == 1:
            px[bx + i, by + j] = (14, 14, 18, 255)
# body top silver trim line (top face edge toward front)
tx, ty, tw, th = reg("body", "top")
for i in range(tw):
    px[tx + i, ty] = (170, 176, 188, 255)
# body front brand strip
nx, ny, nw, nh = reg("body", "north")
for i in range(1, 4):
    px[nx + nw - 1 - i, ny + 1] = (200, 200, 205, 255)
# grip ridges
gx2, gy2, gw2, gh2 = reg("grip", "north")
for j in range(0, gh2, 2):
    for i in range(gw2):
        px[gx2 + i, gy2 + j] = shade(px[gx2 + i, gy2 + j][:3], 10) + (255,)

# ---- write
force = "--force" in sys.argv
if TEXP.exists() and not force:
    sys.exit(f"{TEXP} exists, use --force")
im.save(TEXP)
UVP.write_text(json.dumps(dict(texture="item/digital_camera_model", size=[N, N], regions=layout), indent=2))
disp = json.loads((RES / "models/item/analog_camera.json").read_text()).get("display", {})
MODP.write_text(json.dumps(dict(texture_size=[N, N],
                                textures={"0": "cinecraft:item/digital_camera_model",
                                          "particle": "cinecraft:item/digital_camera_model"},
                                elements=cubes_out, display=disp), indent=2))
print("ok", len(cubes_out), "cubes")
