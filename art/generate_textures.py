#!/usr/bin/env python3
"""CineCraft texture generator (Phase A). Deterministic (seeded). Requires Pillow.

Usage:  python3 art/generate_textures.py [--force] [--only name,name]
Writes  src/main/resources/assets/cinecraft/textures/{item,block}/*.png
        art/uv_layouts/<name>.json   (named rectangles for later Blockbench UV work)
        art/previews/<name>.png      (8x nearest-neighbour previews + contact sheet)
Existing PNGs are never overwritten without --force.
"""
import json, random, sys, zlib
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src/main/resources/assets/cinecraft/textures"
UVDIR = ROOT / "art/uv_layouts"
PREV = ROOT / "art/previews"

# ---------------------------------------------------------------- palette
CH = dict(hi=(238, 241, 246), light=(201, 206, 214), mid=(150, 157, 168), dark=(96, 103, 116), deep=(60, 66, 78))
OUT = (18, 16, 22)


class Cv:
    def __init__(self, w, h, seed):
        self.w, self.h = w, h
        self.im = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        self.px = self.im.load()
        self.rng = random.Random(seed)
        self.layout = {}

    def put(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[x, y] = tuple(c) + (255,) if len(c) == 3 else tuple(c)

    def get(self, x, y):
        return self.px[x, y]

    def rect(self, x, y, w, h, c):
        for j in range(y, y + h):
            for i in range(x, x + w):
                self.put(i, j, c)

    def region(self, name, x, y, w, h):
        self.layout[name] = dict(x=x, y=y, w=w, h=h)
        return (x, y, w, h)

    def ellipse(self, cx, cy, rx, ry, c):
        for j in range(int(cy - ry - 1), int(cy + ry + 2)):
            for i in range(int(cx - rx - 1), int(cx + rx + 2)):
                if ((i + .5 - cx) / rx) ** 2 + ((j + .5 - cy) / ry) ** 2 <= 1:
                    self.put(i, j, c)

    def outline(self, c=OUT):
        src = self.im.copy().load()
        for j in range(self.h):
            for i in range(self.w):
                if src[i, j][3] == 0:
                    for di, dj in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        a, b = i + di, j + dj
                        if 0 <= a < self.w and 0 <= b < self.h and src[a, b][3] > 0:
                            self.put(i, j, c)
                            break


def sh(c, d):
    return tuple(max(0, min(255, v + d)) for v in c)


def bevel(cv, r, light=22, dark=-28):
    x, y, w, h = r
    for i in range(x, x + w):
        cv.put(i, y, sh(cv.get(i, y)[:3], light))
        cv.put(i, y + h - 1, sh(cv.get(i, y + h - 1)[:3], dark))
    for j in range(y, y + h):
        cv.put(x, j, sh(cv.get(x, j)[:3], light // 2))
        cv.put(x + w - 1, j, sh(cv.get(x + w - 1, j)[:3], dark // 2))


# ---------------------------------------------------------------- material painters
def m_noise(cv, r, base, var=4):
    x, y, w, h = r
    for j in range(y, y + h):
        for i in range(x, x + w):
            cv.put(i, j, sh(base, cv.rng.randint(-var, var)))


def m_chrome(cv, r, tone="light", bev=True):
    x, y, w, h = r
    base = CH[tone]
    for j in range(y, y + h):
        row = cv.rng.randint(-7, 7)
        band = int(18 * (1 - abs((j - y) - h * 0.3) / max(1, h * 0.7)))  # highlight band
        for i in range(x, x + w):
            cv.put(i, j, sh(base, row + band + cv.rng.randint(-3, 3)))
    if bev:
        bevel(cv, r)


def m_leather(cv, r, base=(36, 36, 41)):
    x, y, w, h = r
    for j in range(y, y + h):
        for i in range(x, x + w):
            n = cv.rng.random()
            d = 12 if n < .14 else (-9 if n < .30 else 0)
            cv.put(i, j, sh(base, d + cv.rng.randint(-2, 2)))
    bevel(cv, r, 8, -10)


def m_rubber(cv, r, base=(27, 27, 32), ridge=2):
    x, y, w, h = r
    for j in range(y, y + h):
        for i in range(x, x + w):
            d = 7 if (i - x) % ridge == 0 else -3
            cv.put(i, j, sh(base, d + cv.rng.randint(-2, 2)))


def m_matte(cv, r, base=(30, 31, 36)):
    m_noise(cv, r, base, 3)
    bevel(cv, r, 10, -12)


def m_paint(cv, r, base, var=4):
    m_noise(cv, r, base, var)
    bevel(cv, r, 16, -22)


def m_wood(cv, r, base=(120, 86, 52)):
    x, y, w, h = r
    for j in range(y, y + h):
        row = cv.rng.randint(-8, 8) if (j - y) % 3 == 0 else None
        if row is not None:
            cur = row
        for i in range(x, x + w):
            cv.put(i, j, sh(base, cur + cv.rng.randint(-3, 3)))
    for _ in range(max(1, w * h // 160)):  # knots
        kx, ky = cv.rng.randint(x + 1, x + w - 2), cv.rng.randint(y + 1, y + h - 2)
        cv.put(kx, ky, sh(base, -38)); cv.put(kx + 1, ky, sh(base, -22))
    bevel(cv, r, 12, -22)


def m_brass(cv, r):
    m_noise(cv, r, (188, 148, 66), 6)
    bevel(cv, r, 24, -34)


def m_plastic(cv, r, base):
    m_noise(cv, r, base, 3)
    bevel(cv, r, 14, -20)


def m_glass(cv, r, tint, shine=True):
    x, y, w, h = r
    cx, cy = x + w / 2, y + h / 2
    mr = max(w, h) / 2
    for j in range(y, y + h):
        for i in range(x, x + w):
            d = (((i + .5 - cx) ** 2 + (j + .5 - cy) ** 2) ** .5) / mr
            cv.put(i, j, sh(tint, int(34 - 70 * min(1, d)) + cv.rng.randint(-2, 2)))
    if shine and w >= 6:
        cv.put(x + w // 4, y + h // 4, (235, 242, 255)); cv.put(x + w // 4 + 1, y + h // 4, (190, 205, 235))
        cv.put(x + w // 4, y + h // 4 + 1, (190, 205, 235))


def m_paper(cv, r, base=(232, 222, 196)):
    m_noise(cv, r, base, 3)


def m_film(cv, r, perf=True, horizontal=True):
    x, y, w, h = r
    m_noise(cv, r, (36, 26, 22), 3)
    if perf and horizontal and h >= 6:
        for i in range(x + 1, x + w - 1, 4):
            cv.put(i, y + 1, (222, 214, 190)); cv.put(i + 1, y + 1, (222, 214, 190))
            cv.put(i, y + h - 2, (222, 214, 190)); cv.put(i + 1, y + h - 2, (222, 214, 190))


def m_disc(cv, r, fn):
    """Paint a circle inscribed in r; fn(dist01) -> colour."""
    x, y, w, h = r
    cx, cy, rr = x + w / 2, y + h / 2, min(w, h) / 2
    for j in range(y, y + h):
        for i in range(x, x + w):
            d = (((i + .5 - cx) ** 2 + (j + .5 - cy) ** 2) ** .5) / rr
            if d <= 1:
                cv.put(i, j, fn(d))


# ---------------------------------------------------------------- shared detail helpers
def ticks(cv, x, y, w, step, h=2, c=(25, 25, 30), long_every=4):
    for k, i in enumerate(range(x, x + w, step)):
        for j in range(h + (1 if k % long_every == 0 else 0)):
            cv.put(i, y + j, c)


def stripes_diag(cv, r, a, b, wd=4, flip=False):
    x, y, w, h = r
    for j in range(h):
        for i in range(w):
            k = ((i + (w - j if flip else j)) // wd) % 2
            cv.put(x + i, y + j, a if k == 0 else b)
    bevel(cv, r, 8, -10)


def knurl(cv, r, tone="light"):
    m_chrome(cv, r, tone)
    x, y, w, h = r
    for i in range(x + 1, x + w - 1, 2):
        for j in range(y + 1, y + h - 1):
            cv.put(i, j, sh(cv.get(i, j)[:3], -26))


def vents(cv, r, base, slot=(14, 16, 18), step=3, vertical=False):
    x, y, w, h = r
    if vertical:
        for i in range(x + 2, x + w - 2, step):
            cv.rect(i, y + 2, 1, h - 4, slot)
    else:
        for j in range(y + 2, y + h - 2, step):
            cv.rect(x + 2, j, w - 4, 1, slot)


def bayonet(cv, r):
    x, y, w, h = r
    m_chrome(cv, r, "mid")
    cx, cy, rr = x + w / 2, y + h / 2, min(w, h) / 2
    m_disc(cv, r, lambda d: (14, 14, 18) if d < .55 else (CH["light"] if d < .72 else CH["dark"]))
    for ang in range(0, 360, 90):  # lugs
        import math
        a = math.radians(ang + 45)
        for t in (0.62, 0.7, 0.78):
            cv.put(int(cx + math.cos(a) * rr * t), int(cy + math.sin(a) * rr * t), CH["hi"])
    cv.put(int(cx - rr * .8), int(cy), (190, 40, 40))  # red index dot


# ---------------------------------------------------------------- icons (32x32, transparent, outlined)
def icon_analog_camera(cv):
    m_chrome(cv, (3, 9, 26, 5)); m_leather(cv, (3, 14, 26, 11)); m_leather(cv, (3, 14, 5, 11), (48, 46, 50))
    m_chrome(cv, (11, 5, 10, 5), "hi"); m_glass(cv, (14, 6, 4, 3), (40, 50, 90), False)
    knurl(cv, (4, 6, 5, 3)); cv.rect(23, 5, 3, 3, (206, 52, 48)); cv.put(23, 5, (255, 130, 120))
    m_chrome(cv, (3, 24, 26, 2), "dark")
    cv.ellipse(16, 18, 7, 7, (22, 22, 26)); cv.ellipse(16, 18, 6, 6, CH["mid"]); cv.ellipse(16, 18, 5, 5, (22, 22, 28))
    m_disc(cv, (11, 13, 10, 10), lambda d: sh((52, 70, 150), int(40 - 80 * d)) if d > .35 else (120, 70, 160))
    cv.put(13, 15, (240, 245, 255)); cv.put(14, 15, (200, 215, 245)); cv.put(13, 16, (200, 215, 245))
    cv.rect(24, 11, 3, 2, (248, 226, 160))  # flash/lamp window
    cv.outline()


def icon_film_roll(cv):
    m_disc(cv, (3, 3, 26, 26), lambda d: sh((44, 30, 24), int(14 * (1 - d) - 6 * d)) if d > .38 else sh(CH["light"], int(-30 * (1 - d))))
    for r_ in (0.55, 0.72, 0.88):
        import math
        for a in range(0, 360, 6):
            cv.put(int(16 + math.cos(math.radians(a)) * 13 * r_), int(16 + math.sin(math.radians(a)) * 13 * r_), (60, 42, 32))
    for a in range(0, 360, 60):
        import math
        cv.put(int(16 + math.cos(math.radians(a)) * 8), int(16 + math.sin(math.radians(a)) * 8), (226, 214, 188))
    cv.ellipse(16, 16, 4, 4, CH["mid"]); cv.ellipse(16, 16, 2, 2, (18, 18, 22)); cv.put(14, 14, CH["hi"])
    cv.rect(24, 22, 6, 4, (36, 26, 22)); cv.put(25, 23, (222, 214, 190)); cv.put(28, 23, (222, 214, 190))
    cv.outline()


def icon_film_cartridge(cv):
    m_chrome(cv, (9, 8, 14, 19), "light")
    m_chrome(cv, (11, 4, 10, 4), "dark"); m_chrome(cv, (13, 2, 6, 3), "mid")
    cv.rect(9, 13, 14, 9, (246, 200, 40)); cv.rect(9, 15, 14, 2, (206, 40, 36)); cv.rect(11, 19, 8, 1, (40, 34, 30)); cv.rect(11, 21, 5, 1, (40, 34, 30))
    for j in range(13, 22): cv.put(9, j, (255, 226, 110)); cv.put(22, j, (190, 144, 20))
    m_film(cv, (22, 22, 7, 3), perf=False); cv.put(24, 22, (222, 214, 190)); cv.put(27, 22, (222, 214, 190))
    cv.put(10, 9, CH["hi"]); cv.put(10, 10, CH["hi"])
    cv.outline()


def icon_lens(cv):
    m_rubber(cv, (7, 9, 10, 14), (27, 27, 32), 2)             # focus ring
    m_chrome(cv, (17, 10, 5, 12), "light"); ticks(cv, 17, 11, 5, 1, 0)
    for j in range(11, 22, 2): cv.put(19, j, (25, 25, 30))
    m_chrome(cv, (22, 11, 5, 10), "mid"); cv.rect(23, 12, 3, 1, (190, 40, 40))
    m_matte(cv, (3, 8, 5, 16), (24, 24, 28))                   # hood
    m_glass(cv, (3, 10, 3, 12), (60, 70, 150), False)
    cv.put(4, 12, (230, 238, 255)); cv.put(4, 13, (190, 205, 240))
    cv.outline()


def icon_clapperboard(cv):
    m_matte(cv, (4, 12, 24, 16), (28, 30, 34)); bevel(cv, (4, 12, 24, 16), 14, -14)
    for k in range(3):
        cv.rect(7, 16 + k * 4, 18 - k * 3, 1, (226, 226, 218))
    cv.rect(7, 25, 6, 1, (226, 226, 218))
    stripes_diag(cv, (3, 5, 26, 5), (24, 24, 28), (236, 236, 228), 3)
    stripes_diag(cv, (4, 10, 24, 3), (236, 236, 228), (24, 24, 28), 3, True)
    cv.rect(3, 9, 3, 2, (186, 146, 66)); cv.outline()


def icon_digital_camera(cv):
    m_matte(cv, (3, 9, 26, 15), (44, 48, 56)); m_chrome(cv, (3, 8, 26, 3), "dark")
    m_rubber(cv, (3, 12, 5, 12), (30, 32, 38), 2)
    cv.ellipse(18, 17, 6, 6, (16, 16, 20)); cv.ellipse(18, 17, 5, 5, CH["mid"]); m_disc(cv, (14, 13, 8, 8), lambda d: sh((50, 60, 140), int(36 - 76 * d)))
    cv.put(16, 15, (240, 245, 255)); cv.rect(24, 5, 3, 3, (206, 52, 48)); cv.rect(5, 6, 4, 2, CH["light"])
    cv.rect(23, 11, 4, 2, (90, 200, 120)); cv.outline()


def icon_memory_card(cv):
    m_plastic(cv, (9, 4, 14, 24), (36, 56, 120)); cv.rect(9, 4, 4, 4, (0, 0, 0, 0))  # clipped corner
    for k in range(5): cv.rect(11 + k * 2, 23, 1, 4, (226, 188, 80))
    cv.rect(11, 10, 10, 6, (230, 232, 238)); cv.rect(12, 12, 8, 1, (200, 50, 50)); cv.rect(12, 14, 5, 1, (60, 70, 90))
    cv.put(22, 6, (150, 170, 220)); cv.outline()


ICONS = {
    "analog_camera": icon_analog_camera, "film_roll": icon_film_roll, "film_cartridge": icon_film_cartridge,
    "lens": icon_lens, "clapperboard": icon_clapperboard,
    "digital_camera": icon_digital_camera, "memory_card_small": icon_memory_card,
}


# ---------------------------------------------------------------- model sheets / block atlases
def sheet_analog_camera_model(cv):
    R = cv.region
    m_chrome(cv, R("top_plate", 0, 0, 24, 10)); ticks(cv, 2, 3, 20, 3)
    m_chrome(cv, R("bottom_plate", 24, 0, 24, 10), "dark")
    m_leather(cv, R("leather_front", 0, 10, 24, 14))
    r = R("leather_back", 24, 10, 24, 14); m_leather(cv, r); cv.rect(34, 14, 4, 4, (14, 14, 18))
    m_leather(cv, R("side_left", 0, 24, 10, 14)); m_leather(cv, R("side_right", 10, 24, 10, 14))
    for rr in ("side_left", "side_right"): cv.rect(cv.layout[rr]["x"] + 2, 26, 6, 1, CH["dark"])
    m_rubber(cv, R("grip", 20, 24, 8, 14))
    m_chrome(cv, R("prism", 28, 24, 16, 8), "hi")
    r = R("viewfinder", 44, 24, 12, 6); m_noise(cv, r, (18, 20, 28), 2); bevel(cv, r, 18, -10)
    cv.rect(47, 26, 6, 2, (60, 80, 120))
    bayonet(cv, R("lens_mount", 0, 38, 16, 16))
    r = R("shutter_button", 16, 38, 6, 6); m_paint(cv, r, (200, 48, 44), 5)
    knurl(cv, R("rewind_knob", 22, 38, 10, 6))
    r = R("dial", 32, 38, 8, 8); m_chrome(cv, r); ticks(cv, 33, 39, 6, 2, 1)
    m_chrome(cv, R("strap_lug", 40, 38, 4, 4), "dark")
    r = R("film_window", 44, 32, 12, 6); m_noise(cv, r, (22, 24, 30), 2); cv.rect(46, 34, 8, 2, (236, 198, 70))
    r = R("flash_window", 44, 38, 8, 6); m_glass(cv, r, (240, 224, 170), False)
    m_brass(cv, R("accent_brass", 16, 44, 8, 4))


def sheet_film_roll_model(cv):
    R = cv.region
    r = R("roll_side", 0, 0, 16, 16)
    m_disc(cv, r, lambda d: sh((44, 30, 24), int(10 * (1 - d))) if d > .3 else sh(CH["light"], -20))
    for rr in (.45, .6, .75, .9):
        import math
        for a in range(0, 360, 8): cv.put(int(8 + math.cos(math.radians(a)) * 8 * rr), int(8 + math.sin(math.radians(a)) * 8 * rr), (62, 44, 34))
    cv.ellipse(8, 8, 2, 2, (16, 16, 20))
    r = R("flange_face", 16, 0, 16, 16)
    m_disc(cv, r, lambda d: sh(CH["light"], int(14 - 40 * d)))
    cv.ellipse(24, 8, 3, 3, CH["dark"]); cv.ellipse(24, 8, 1, 1, (14, 14, 18))
    for a in (0, 120, 240):
        import math
        cv.put(int(24 + math.cos(math.radians(a)) * 5.5), int(8 + math.sin(math.radians(a)) * 5.5), (24, 24, 30))
    m_film(cv, R("wound_strip", 0, 16, 32, 8));
    for j in range(17, 23, 2):
        for i in range(0, 32): cv.put(i, j, sh(cv.get(i, j)[:3], 5))
    m_chrome(cv, R("hub_strip", 0, 24, 16, 6), "mid")
    m_film(cv, R("leader", 16, 24, 16, 6))


def sheet_film_cartridge_model(cv):
    R = cv.region
    m_chrome(cv, R("can_body", 0, 0, 32, 12), "light")
    r = R("label", 0, 12, 32, 10); m_paint(cv, r, (246, 200, 40), 3)
    cv.rect(0, 14, 32, 2, (206, 40, 36)); cv.rect(2, 18, 14, 1, (40, 34, 30)); cv.rect(2, 20, 9, 1, (40, 34, 30)); cv.rect(22, 17, 6, 4, (28, 28, 32))
    m_disc(cv, R("cap_top", 0, 22, 10, 10), lambda d: sh(CH["mid"], int(18 - 40 * d))); cv.ellipse(5, 27, 1.5, 1.5, (20, 20, 24))
    m_disc(cv, R("cap_bottom", 10, 22, 10, 10), lambda d: sh(CH["dark"], int(10 - 30 * d)))
    r = R("felt_slot", 20, 22, 12, 4); m_noise(cv, r, (14, 14, 16), 2)
    m_film(cv, R("leader", 20, 26, 12, 6), perf=True)


def sheet_lens_model(cv):
    R = cv.region
    m_rubber(cv, R("rubber_grip", 0, 0, 32, 12), (27, 27, 32), 3)
    r = R("scale_ring", 0, 12, 32, 8); m_chrome(cv, r, "light"); ticks(cv, 1, 13, 30, 2, 2); cv.rect(0, 17, 32, 1, (190, 40, 40)); ticks(cv, 1, 18, 30, 4, 1, (25, 25, 30), 2)
    r = R("hood", 32, 0, 32, 12); m_matte(cv, r, (24, 25, 29)); vents(cv, r, None, (16, 16, 20), 2, True)
    m_glass(cv, R("front_glass", 0, 20, 16, 16), (62, 70, 150))
    m_glass(cv, R("rear_glass", 16, 20, 16, 16), (92, 56, 140))
    m_chrome(cv, R("mount", 32, 12, 32, 8), "mid"); cv.rect(40, 14, 4, 2, (190, 40, 40))
    bayonet(cv, R("mount_face", 0, 36, 16, 16))
    m_matte(cv, R("barrel_black", 16, 36, 24, 8), (24, 24, 28))
    r = R("contacts", 40, 36, 8, 4); cv.rect(*r, (24, 24, 28))
    for i in range(41, 47, 2): cv.rect(i, 37, 1, 2, (214, 168, 70))
    r = R("label_ring", 32, 20, 32, 6); m_chrome(cv, r, "light")
    for i in range(34, 62, 3): cv.put(i, 22, (30, 30, 36))


def sheet_clapperboard_model(cv):
    R = cv.region
    r = R("slate", 0, 0, 32, 24); m_matte(cv, r, (30, 32, 36)); bevel(cv, r, 16, -16)
    for k in range(4): cv.rect(2, 4 + k * 5, 28, 1, (214, 214, 206))
    cv.rect(16, 4, 1, 16, (214, 214, 206)); cv.rect(2, 22, 20, 1, (226, 226, 218))
    stripes_diag(cv, R("clapper_arm", 32, 0, 32, 8), (24, 24, 28), (236, 236, 228), 4)
    stripes_diag(cv, R("clapper_bottom", 32, 8, 32, 8), (236, 236, 228), (24, 24, 28), 4, True)
    m_brass(cv, R("hinge", 0, 24, 8, 8))
    m_wood(cv, R("edge_wood", 8, 24, 24, 4), (96, 66, 42))
    r = R("chalk_text", 32, 16, 32, 16); m_matte(cv, r, (28, 30, 34))
    for k in range(5): cv.rect(34, 18 + k * 3, 14 + (k * 7) % 11, 1, (208, 208, 200))


def sheet_tripod(cv):
    R = cv.region
    for name, x, tone in (("leg_tube_a", 0, "light"), ("leg_tube_b", 8, "mid")):
        r = R(name, x, 0, 8, 32); m_chrome(cv, r, tone, False)
        for j in range(32): cv.put(x + 2, j, CH["hi"]); cv.put(x + 6, j, sh(CH[tone], -34)); cv.put(x + 7, j, sh(CH[tone], -50))
    r = R("leg_lock", 16, 0, 8, 12); m_matte(cv, r, (28, 28, 32)); cv.rect(18, 3, 4, 4, CH["dark"]); cv.put(19, 4, CH["hi"])
    m_rubber(cv, R("foot", 24, 0, 8, 8), (24, 24, 28), 2)
    r = R("head_top", 32, 0, 16, 16); m_matte(cv, r, (30, 31, 36)); cv.ellipse(40, 8, 3, 3, CH["mid"]); cv.ellipse(40, 8, 1.2, 1.2, (16, 16, 20))
    m_matte(cv, R("head_side", 32, 16, 16, 8), (36, 37, 42));
    r = R("quickrelease", 48, 0, 16, 8); m_chrome(cv, r, "light"); ticks(cv, 50, 3, 12, 3, 2)
    r = R("level", 48, 8, 16, 6); m_noise(cv, r, (130, 190, 120), 6); cv.rect(55, 10, 3, 2, (230, 250, 220)); bevel(cv, r, 20, -26)
    m_rubber(cv, R("handle", 0, 32, 8, 32), (26, 26, 30), 2)
    m_matte(cv, R("handle_cap", 8, 32, 8, 8), (36, 36, 40))
    m_chrome(cv, R("hinge", 16, 32, 16, 8), "mid")
    m_chrome(cv, R("centre_column", 32, 32, 8, 32), "light")
    for j in range(34, 62, 4): cv.rect(33, j, 6, 1, sh(CH["light"], -40))
    r = R("rubber_ring", 40, 32, 16, 6); m_rubber(cv, r, (24, 24, 28), 2)


def sheet_projector(cv):
    R = cv.region
    base = (62, 74, 70)
    r = R("body_top", 0, 0, 24, 16); m_paint(cv, r, base); vents(cv, r, base, (20, 24, 24), 3)
    r = R("body_front", 24, 0, 24, 16); m_paint(cv, r, base); cv.ellipse(36, 8, 5, 5, (16, 18, 20)); cv.rect(26, 2, 3, 3, (200, 60, 50))
    r = R("body_side", 0, 16, 24, 16); m_paint(cv, r, base); vents(cv, (2, 18, 20, 12), base, (18, 22, 22), 2, True)
    r = R("body_back", 24, 16, 24, 16); m_paint(cv, r, base); cv.rect(28, 20, 4, 4, (20, 22, 22)); cv.rect(36, 20, 8, 3, CH["dark"]); cv.rect(28, 26, 16, 2, (180, 150, 60))
    r = R("lens_barrel", 48, 0, 16, 16); m_chrome(cv, r, "light"); ticks(cv, 49, 4, 14, 2, 3); cv.rect(48, 10, 16, 2, CH["dark"])
    m_glass(cv, R("lens_glass", 48, 16, 16, 16), (190, 160, 90))
    m_chrome(cv, R("reel_arm", 0, 32, 8, 24), "mid")
    r = R("reel", 8, 32, 24, 24)
    m_disc(cv, r, lambda d: sh(CH["light"], int(-34 * d)) if d > .55 else (34, 24, 20))
    import math
    for a in range(0, 360, 60):
        for t in range(4, 10): cv.put(int(20 + math.cos(math.radians(a)) * t), int(44 + math.sin(math.radians(a)) * t), (14, 14, 18))
    cv.ellipse(20, 44, 2, 2, (14, 14, 18))
    r = R("lamp_glow", 32, 32, 16, 8)
    for j in range(8):
        for i in range(16): cv.put(32 + i, 32 + j, sh((255, 170, 50), int(-60 * ((abs(i - 7.5) / 8) ** 2 + (abs(j - 3.5) / 4) ** 2) + cv.rng.randint(-6, 6))))
    m_rubber(cv, R("foot", 48, 32, 16, 8), (26, 26, 30), 2)
    m_film(cv, R("film_path", 32, 40, 16, 6))
    r = R("crank", 48, 40, 8, 8); m_brass(cv, r)
    r = R("label_plate", 56, 40, 8, 6); m_chrome(cv, r, "mid"); cv.rect(57, 42, 6, 1, (30, 30, 36))


def sheet_darkroom_enlarger(cv):
    R = cv.region
    m_wood(cv, R("baseboard", 0, 0, 32, 16), (132, 96, 58))
    m_chrome(cv, R("column", 32, 0, 8, 32), "mid")
    r = R("head_housing", 40, 0, 24, 16); m_paint(cv, r, (34, 36, 42)); vents(cv, r, None, (16, 16, 20), 3)
    r = R("bellows", 0, 16, 16, 16); m_matte(cv, r, (28, 28, 32))
    for j in range(16, 32, 3): cv.rect(0, j, 16, 1, (46, 46, 52)); cv.rect(0, j + 1, 16, 1, (18, 18, 22))
    m_chrome(cv, R("lens_cone", 16, 16, 16, 16), "light"); m_glass(cv, (20, 20, 8, 8), (70, 80, 150))
    r = R("easel", 0, 32, 32, 16); m_paint(cv, r, (30, 32, 38)); cv.rect(4, 36, 24, 8, (238, 232, 214)); cv.rect(4, 36, 24, 1, (200, 196, 180))
    m_plastic(cv, R("knob", 32, 32, 8, 8), (28, 28, 32))
    m_chrome(cv, R("arm", 40, 32, 16, 6), "mid")
    m_wood(cv, R("baseboard_edge", 0, 48, 32, 4), (110, 78, 46))


def sheet_darkroom_safelight(cv):
    R = cv.region
    r = R("housing", 0, 0, 16, 16); m_paint(cv, r, (30, 32, 36)); vents(cv, r, None, (14, 14, 18), 3, True)
    r = R("red_glass", 16, 0, 16, 16)
    for j in range(16):
        for i in range(16): cv.put(16 + i, j, sh((225, 28, 22), int(30 - 60 * (((i - 7.5) / 8) ** 2 + ((j - 7.5) / 8) ** 2) ** .5 + cv.rng.randint(-3, 3))))
    bevel(cv, r, 24, -30)
    m_chrome(cv, R("base", 0, 16, 16, 8), "dark"); m_chrome(cv, R("mount", 16, 16, 16, 8), "mid")


def sheet_developer_tray(cv):
    R = cv.region
    m_plastic(cv, R("tray_inner", 0, 0, 16, 16), (226, 228, 230))
    r = R("liquid", 0, 20, 16, 12)
    for j in range(12):
        for i in range(16): cv.put(i, 20 + j, sh((176, 120, 40), cv.rng.randint(-5, 5) + (10 if j < 2 else 0)))
    m_plastic(cv, R("tray_outer", 16, 0, 16, 16), (186, 190, 194))
    m_plastic(cv, R("rim", 0, 16, 32, 4), (240, 242, 244))
    r = R("tongs", 16, 20, 16, 6); m_chrome(cv, r, "mid"); cv.rect(17, 22, 14, 1, CH["dark"])


def sheet_developer_station(cv):
    R = cv.region
    r = R("shell_front", 0, 0, 32, 24); m_chrome(cv, r, "light"); cv.rect(2, 3, 28, 1, CH["dark"]); cv.rect(2, 20, 28, 1, CH["dark"])
    for x in (4, 14, 24): cv.ellipse(x + 1, 11, 1.3, 1.3, CH["deep"])
    r = R("tank", 32, 0, 16, 24)
    for j in range(24):
        for i in range(16):
            edge = abs(i - 7.5) / 8
            col = (110, 150, 190) if j < 6 else (176, 120, 40)
            cv.put(32 + i, j, sh(col, int(30 * (1 - edge) - 26 * edge) + cv.rng.randint(-3, 3)))
    cv.rect(35, 2, 1, 20, (230, 240, 250)); bevel(cv, r, 20, -26)
    r = R("pipes", 48, 0, 8, 24); m_brass(cv, r); cv.rect(51, 0, 1, 24, (240, 210, 130))
    r = R("label", 0, 24, 16, 8); m_paper(cv, r); cv.rect(1, 26, 14, 2, (206, 60, 50)); cv.rect(1, 29, 10, 1, (50, 50, 60))
    r = R("panel", 16, 24, 16, 8); m_matte(cv, r, (24, 26, 30)); cv.rect(18, 26, 2, 2, (90, 220, 120)); cv.rect(22, 26, 2, 2, (220, 70, 60)); cv.rect(26, 26, 3, 4, CH["mid"])
    r = R("top", 32, 24, 16, 12); m_chrome(cv, r, "mid")
    m_matte(cv, R("base", 0, 32, 32, 16), (30, 32, 36))
    m_rubber(cv, R("hose", 32, 36, 16, 6), (24, 24, 28), 2)
    m_chrome(cv, R("lid", 48, 24, 16, 12), "light")


def sheet_film_shelf(cv):
    R = cv.region
    m_wood(cv, R("wood_plank", 0, 0, 32, 12), (134, 96, 58))
    m_wood(cv, R("wood_side", 32, 0, 16, 12), (104, 72, 44))
    r = R("bracket", 48, 0, 16, 12); m_matte(cv, r, (34, 36, 42)); cv.rect(50, 5, 12, 2, CH["dark"])
    r = R("film_can_row", 0, 12, 32, 16); m_noise(cv, r, (40, 40, 46), 2)
    cols = [(246, 200, 40), (206, 56, 48), (60, 120, 200), (90, 170, 90)]
    for k in range(4):
        x = 1 + k * 8; m_chrome(cv, (x, 13, 6, 14), "light"); cv.rect(x, 17, 6, 6, cols[k]); cv.rect(x, 19, 6, 1, (30, 30, 34))
    r = R("boxes", 32, 12, 32, 16); m_noise(cv, r, (50, 46, 40), 2)
    bc = [(236, 190, 40), (210, 90, 40), (60, 100, 170), (230, 220, 190)]
    for k in range(4):
        x = 33 + k * 8; m_paint(cv, (x, 13, 7, 14), bc[k]); cv.rect(x + 1, 16, 5, 1, (30, 30, 36)); cv.rect(x + 1, 22, 5, 3, (240, 236, 220))
    m_wood(cv, R("back_panel", 0, 28, 32, 16), (96, 68, 42))
    for i in range(0, 32, 8): cv.rect(i, 28, 1, 16, (60, 40, 26))
    m_wood(cv, R("shelf_edge", 32, 28, 32, 4), (160, 120, 78))
    r = R("label_holder", 32, 32, 16, 6); m_brass(cv, r); cv.rect(34, 34, 12, 2, (236, 230, 208))


def sheet_screen(cv):
    R = cv.region
    r = R("canvas", 0, 0, 24, 24); m_noise(cv, r, (236, 236, 230), 3)
    for j in range(0, 24, 2):
        for i in range(24): cv.put(i, j, sh(cv.get(i, j)[:3], -3))
    m_matte(cv, R("frame", 24, 0, 8, 24), (20, 20, 24))
    m_matte(cv, R("frame_edge", 0, 24, 32, 8), (28, 28, 32))


SHEETS = {  # name -> (subdir, size, fn)
    "analog_camera_model": ("item", 64, sheet_analog_camera_model),
    "film_roll_model": ("item", 32, sheet_film_roll_model),
    "film_cartridge_model": ("item", 32, sheet_film_cartridge_model),
    "lens_model": ("item", 64, sheet_lens_model),
    "clapperboard_model": ("item", 64, sheet_clapperboard_model),
    "tripod": ("block", 64, sheet_tripod),
    "projector": ("block", 64, sheet_projector),
    "darkroom_enlarger": ("block", 64, sheet_darkroom_enlarger),
    "darkroom_safelight": ("block", 32, sheet_darkroom_safelight),
    "developer_tray": ("block", 32, sheet_developer_tray),
    "developer_station": ("block", 64, sheet_developer_station),
    "film_shelf": ("block", 64, sheet_film_shelf),
    "screen": ("block", 32, sheet_screen),
}


def seed_for(name):
    return zlib.crc32(name.encode())


def main():
    force = "--force" in sys.argv
    only = None
    for a in sys.argv[1:]:
        if a.startswith("--only="):
            only = set(a.split("=", 1)[1].split(","))
    PREV.mkdir(parents=True, exist_ok=True); UVDIR.mkdir(parents=True, exist_ok=True)
    jobs = [(n, "item", 32, f) for n, f in ICONS.items()] + [(n, d, s, f) for n, (d, s, f) in SHEETS.items()]
    done, skipped, previews = [], [], []
    for name, sub, size, fn in jobs:
        if only and name not in only:
            continue
        out = TEX / sub / f"{name}.png"
        if out.exists() and not force:
            skipped.append(f"{sub}/{name}.png"); continue
        cv = Cv(size, size, seed_for(name)); fn(cv)
        out.parent.mkdir(parents=True, exist_ok=True); cv.im.save(out, optimize=True)
        if cv.layout:
            (UVDIR / f"{name}.json").write_text(json.dumps(dict(texture=f"cinecraft:{sub}/{name}", size=[size, size], regions=cv.layout), indent=2))
        cv.im.resize((size * 8, size * 8), Image.NEAREST).save(PREV / f"{name}.png")
        previews.append((name, cv.im)); done.append(f"{sub}/{name}.png ({size}x{size})")
    print("written:", *done, sep="\n  ");
    if skipped: print("skipped (exist, use --force):", *skipped, sep="\n  ")


if __name__ == "__main__":
    main()
