#!/usr/bin/env python3
"""CineCraft gear icons (16x16). Deterministic. Requires Pillow.

Usage: python3 art/generate_gear_icons.py [--force] [--only a,b]
Writes textures/item/<id>.png and models/item/<id>.json (item/generated).
Existing files are never overwritten without --force.
"""
import json, math, random, sys
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources/assets/cinecraft"
TEX, MOD = RES / "textures/item", RES / "models/item"
PREV = ROOT / "art/previews"
OUT = (18, 16, 22)

P = dict(
    k=OUT, K=(60, 66, 78), m=(110, 117, 130), M=(150, 157, 168), l=(201, 206, 214), h=(238, 241, 246),
    b=(36, 38, 44), B=(60, 62, 72), r=(200, 44, 44), R=(122, 26, 30), o=(228, 130, 42), O=(170, 88, 28),
    y=(240, 204, 68), Y=(176, 134, 32), g=(78, 176, 96), G=(36, 104, 58), u=(66, 106, 206), U=(32, 54, 124),
    c=(120, 206, 232), C=(70, 150, 190), p=(134, 74, 176), w=(238, 234, 222), W=(200, 196, 184),
    n=(124, 84, 50), N=(78, 50, 30), a=(224, 152, 42), A=(160, 100, 24), e=(54, 44, 40), x=(46, 128, 80),
    X=(26, 76, 50), t=(218, 174, 62), T=(150, 112, 38), z=(176, 206, 226), Z=(120, 150, 176), d=(90, 60, 130),
)


def shade(c, d):
    return tuple(max(0, min(255, v + d)) for v in c[:3])


class Cv:
    def __init__(self, seed=1):
        self.im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        self.px = self.im.load()
        self.rng = random.Random(seed)

    def col(self, c):
        return P[c] if isinstance(c, str) else c

    def put(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16 and c is not None:
            self.px[x, y] = tuple(self.col(c)) + (255,)

    def rect(self, x, y, w, h, c):
        for j in range(y, y + h):
            for i in range(x, x + w):
                self.put(i, j, c)

    def bevel(self, x, y, w, h, c, d=22):
        c = self.col(c)
        self.rect(x, y, w, h, c)
        for i in range(w):
            self.put(x + i, y, shade(c, d))
            self.put(x + i, y + h - 1, shade(c, -d))
        for j in range(h):
            self.put(x, y + j, shade(c, d))
            self.put(x + w - 1, y + j, shade(c, -d))

    def disc(self, cx, cy, r, c, lit=True):
        c = self.col(c)
        for j in range(16):
            for i in range(16):
                dx, dy = i + .5 - cx, j + .5 - cy
                if dx * dx + dy * dy <= r * r:
                    d = int(-(dx + dy) / r * 16) if lit else 0
                    self.put(i, j, shade(c, d))

    def ring(self, cx, cy, r1, r2, c, lit=True):
        c = self.col(c)
        for j in range(16):
            for i in range(16):
                dx, dy = i + .5 - cx, j + .5 - cy
                q = dx * dx + dy * dy
                if r1 * r1 <= q <= r2 * r2:
                    d = int(-(dx + dy) / r2 * 14) if lit else 0
                    self.put(i, j, shade(c, d))

    def line(self, x0, y0, x1, y1, c, th=1):
        n = max(abs(x1 - x0), abs(y1 - y0), 1)
        for s in range(n + 1):
            x = round(x0 + (x1 - x0) * s / n)
            y = round(y0 + (y1 - y0) * s / n)
            for a in range(th):
                for b in range(th):
                    self.put(x + a, y + b, c)

    def draw(self, rows, ox=0, oy=0):
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch not in ". ":
                    self.put(ox + i, oy + j, ch)

    def noise(self, x, y, w, h, amt=6):
        for j in range(y, y + h):
            for i in range(x, x + w):
                p = self.px[i, j]
                if p[3]:
                    d = self.rng.randint(-amt, amt)
                    self.px[i, j] = shade(p, d) + (255,)

    def outline(self, c=OUT):
        src = self.im.copy().load()
        for j in range(16):
            for i in range(16):
                if src[i, j][3] == 0:
                    for di, dj in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        a, b = i + di, j + dj
                        if 0 <= a < 16 and 0 <= b < 16 and src[a, b][3] and src[a, b][:3] != tuple(c):
                            self.px[i, j] = tuple(c) + (255,)
                            break


ITEMS = {}


def item(name):
    def deco(fn):
        ITEMS[name] = fn
        return fn
    return deco


# ============================================================ 1. camera accessories
@item("camera_flash")
def _(c):
    c.bevel(3, 2, 10, 7, "b")
    c.bevel(4, 3, 8, 3, "w", 14)
    c.rect(5, 4, 3, 1, "h")
    c.rect(4, 7, 5, 1, "B")
    c.put(11, 7, "r")
    c.rect(5, 9, 6, 2, "m")
    c.rect(5, 9, 6, 1, "M")
    c.rect(4, 11, 8, 1, "K")
    c.put(7, 12, "t"); c.put(9, 12, "t")
    c.rect(6, 13, 4, 1, "B")


@item("camera_strap")
def _(c):
    c.ring(8, 9, 3.2, 5.6, "n")
    for a in range(0, 360, 40):
        x, y = 8 + 4.4 * math.cos(math.radians(a)), 9 + 4.4 * math.sin(math.radians(a))
        c.put(int(x), int(y), "y")
    c.rect(6, 1, 4, 3, "m")
    c.rect(7, 2, 2, 1, "k")
    c.rect(6, 1, 4, 1, "l")
    c.rect(7, 4, 2, 1, "N")
    c.put(1, 9, "N"); c.put(14, 9, "N")


@item("camera_battery")
def _(c):
    c.bevel(4, 3, 8, 11, "u", 20)
    c.rect(5, 2, 6, 1, "m"); c.rect(6, 1, 4, 1, "l")
    c.rect(5, 5, 6, 5, "b")
    c.draw(["..y..", ".yy..", "yyyy.", "..yy.", "..y.."], 6, 5)
    c.rect(5, 11, 6, 1, "U")
    c.put(5, 12, "g"); c.put(6, 12, "g"); c.put(7, 12, "g"); c.put(8, 12, "k")


@item("battery_grip")
def _(c):
    c.bevel(3, 1, 10, 3, "B")
    c.rect(4, 2, 3, 1, "m")
    c.bevel(5, 4, 7, 10, "b")
    for j in range(6, 13, 2):
        c.rect(6, j, 5, 1, "B")
    c.put(10, 2, "r"); c.put(11, 2, "r")
    c.rect(3, 4, 2, 2, "K")
    c.rect(11, 5, 2, 3, "K")
    c.put(12, 6, "l")
    c.rect(6, 13, 5, 1, "k")


@item("remote_shutter")
def _(c):
    c.bevel(2, 2, 6, 7, "b")
    c.disc(5, 4.5, 1.8, "r")
    c.rect(3, 7, 4, 1, "B")
    c.line(5, 9, 5, 11, "k")
    c.line(5, 11, 9, 13, "k")
    c.line(9, 13, 12, 12, "k")
    c.rect(12, 10, 2, 4, "l")
    c.rect(13, 10, 1, 4, "m")
    c.put(14, 11, "t"); c.put(14, 12, "t")


@item("lens_cap")
def _(c):
    c.disc(8, 8, 6.2, "b")
    c.ring(8, 8, 4.6, 5.4, "B")
    c.disc(8, 8, 2.2, "K")
    c.disc(8, 8, 1.2, "w", False)
    c.rect(0, 7, 2, 3, "B"); c.rect(14, 7, 2, 3, "B")
    c.put(5, 5, "M"); c.put(6, 4, "M")


@item("lens_hood")
def _(c):
    rows = ["..bbbb..bbbb..", ".bBBBB.bBBBBb.", ".bBBb...bBBBb.", "..bBb...bBBb..",
            "..bBBbbbbBBb..", "..bBBBBBBBBb..", "..bBBBBBBBBb..", "...bBBBBBBb...",
            "...bBBBBBBb...", "...bkkkkkkb...", "....kmmmmk....", "....kMllMk...."]
    c.draw(rows, 1, 2)
    for j in range(5, 11):
        c.put(4, j, "K"); c.put(11, j, "K")
        c.put(7, j, "b"); c.put(9, j, "b")


@item("camera_cage")
def _(c):
    c.rect(2, 2, 12, 2, "m"); c.rect(2, 12, 12, 2, "m"); c.rect(2, 2, 2, 12, "m"); c.rect(12, 2, 2, 12, "m")
    c.rect(3, 3, 10, 1, "M"); c.rect(3, 12, 10, 1, "K")
    c.rect(2, 2, 12, 1, "l"); c.rect(2, 13, 12, 1, "K")
    c.rect(5, 6, 6, 4, "b")
    c.rect(6, 7, 4, 2, "B")
    c.rect(6, 6, 1, 1, "M")
    for i in (3, 12):
        c.put(i, 5, "k"); c.put(i, 8, "k"); c.put(i, 11, "k")
    c.rect(6, 0, 4, 1, "K")


@item("camera_backpack")
def _(c):
    c.bevel(4, 3, 8, 11, "G", 16)
    c.rect(4, 3, 8, 3, "g")
    c.rect(4, 5, 8, 1, "G")
    c.bevel(5, 8, 6, 4, "G", 22)
    c.rect(5, 8, 6, 1, "X")
    c.rect(7, 5, 2, 2, "y")
    c.rect(2, 5, 2, 8, "e"); c.rect(12, 5, 2, 8, "e")
    c.rect(2, 12, 2, 1, "m"); c.rect(12, 12, 2, 1, "m")
    c.rect(6, 2, 4, 1, "e")
    c.rect(7, 9, 2, 2, "k"); c.put(8, 9, "M")


@item("tripod_head")
def _(c):
    c.bevel(3, 1, 10, 3, "K")
    c.rect(6, 2, 4, 1, "m")
    c.rect(7, 4, 2, 2, "m")
    c.disc(8, 8.5, 3.6, "M")
    c.put(7, 7, "h"); c.put(6, 8, "h")
    c.line(11, 9, 14, 5, "K", 2)
    c.rect(13, 3, 2, 3, "b")
    c.rect(6, 12, 4, 2, "B")
    c.rect(7, 14, 2, 1, "m")


# ============================================================ 2. lens filters
def filter_base(c, glass, ringc="K"):
    c.disc(8, 8, 7, ringc)
    c.disc(8, 8, 5.2, glass, False)
    c.ring(8, 8, 5.2, 5.6, "k", False)


@item("uv_filter")
def _(c):
    filter_base(c, (190, 182, 232), "m")
    c.line(5, 7, 8, 4, "h"); c.line(5, 9, 10, 4, "w")
    c.put(10, 11, "p"); c.put(9, 12, "p")
    c.put(8, 1, "k")


@item("polarizing_filter")
def _(c):
    filter_base(c, (78, 92, 124), "B")
    for k in range(-6, 8, 4):
        c.line(3 + k, 12, 12 + k, 3, (112, 132, 176))
    c.line(5, 6, 7, 4, "h")
    for a in range(0, 360, 20):
        x, y = 8 + 6.4 * math.cos(math.radians(a)), 8 + 6.4 * math.sin(math.radians(a))
        c.put(int(x), int(y), "M" if (a // 20) % 2 else "K")
    c.put(8, 1, "r"); c.put(8, 14, "r")


@item("nd_filter")
def _(c):
    filter_base(c, (52, 54, 62), "m")
    c.line(4, 7, 7, 4, (120, 124, 136)); c.put(5, 8, (96, 100, 112))
    c.put(11, 11, (28, 28, 34)); c.put(10, 12, (28, 28, 34))
    c.put(12, 6, "l")


@item("variable_nd_filter")
def _(c):
    filter_base(c, (52, 54, 62), "M")
    for i in range(3, 13):
        t = (i - 3) / 9
        v = int(210 - 170 * t)
        for j in range(3, 13):
            dx, dy = i + .5 - 8, j + .5 - 8
            if dx * dx + dy * dy <= 5.2 ** 2:
                c.put(i, j, (v, v + 4, v + 14))
    for a in range(-60, 61, 20):
        x, y = 8 + 6.4 * math.cos(math.radians(a)), 8 + 6.4 * math.sin(math.radians(a))
        c.put(int(x), int(y), "k")
    c.put(14, 8, "r"); c.put(13, 8, "r")


@item("mist_filter")
def _(c):
    filter_base(c, (222, 206, 176), "b")
    r = random.Random(4)
    for _ in range(14):
        x, y = r.randint(3, 12), r.randint(3, 12)
        if (x - 7.5) ** 2 + (y - 7.5) ** 2 < 24:
            c.put(x, y, "w")
    c.put(5, 5, "h"); c.put(6, 4, "h"); c.put(10, 11, (190, 170, 140))
    c.put(11, 7, (190, 170, 140))


@item("star_filter")
def _(c):
    filter_base(c, (40, 46, 70), "K")
    c.line(8, 3, 8, 12, "z"); c.line(3, 8, 12, 8, "z")
    c.line(4, 4, 11, 11, (110, 130, 170)); c.line(4, 11, 11, 4, (110, 130, 170))
    c.rect(7, 7, 2, 2, "h")
    c.put(8, 2, "m"); c.put(8, 13, "m")


# ============================================================ 3. photo lab
@item("photo_paper")
def _(c):
    c.rect(4, 3, 10, 11, "W")
    c.rect(2, 2, 10, 11, "w")
    c.rect(2, 2, 10, 1, "h"); c.rect(2, 2, 1, 11, "h")
    c.rect(2, 12, 10, 1, "W")
    c.put(11, 2, None); c.put(11, 3, "W"); c.put(10, 3, "W"); c.put(10, 2, "W")
    c.rect(4, 5, 6, 1, (222, 218, 206)); c.rect(4, 7, 5, 1, (222, 218, 206))
    c.put(9, 9, "c")


def bottle(c, glass, cap, label, stripe):
    c.rect(6, 1, 4, 2, cap); c.rect(6, 1, 4, 1, shade(P[cap], 24) if cap in P else cap)
    c.rect(7, 3, 2, 2, glass)
    c.bevel(4, 5, 8, 9, glass, 22)
    c.rect(5, 7, 6, 5, label)
    c.rect(5, 8, 6, 1, stripe); c.rect(5, 10, 4, 1, shade(P[label], -30))
    c.put(5, 6, "h")
    c.rect(5, 13, 6, 1, shade(P[glass] if isinstance(glass, str) else glass, -40))


@item("developer_bottle")
def _(c):
    bottle(c, "A", "b", "w", "r")
    c.rect(5, 4, 1, 1, None)


@item("fixer_bottle")
def _(c):
    bottle(c, "C", "U", "w", "u")


@item("developing_tank")
def _(c):
    c.bevel(4, 5, 8, 9, "B", 18)
    for j in (7, 10, 12):
        c.rect(5, j, 6, 1, "b")
    c.bevel(5, 3, 6, 2, "K")
    c.bevel(6, 1, 4, 2, "r", 22)
    c.rect(5, 8, 2, 1, "h")
    c.rect(4, 13, 8, 1, "k")


@item("negative_strip")
def _(c):
    c.rect(4, 1, 8, 14, "O")
    for j in range(2, 14, 3):
        c.put(5, j, "k"); c.put(10, j, "k")
    for j, col in zip((2, 6, 10), ((90, 140, 160), (110, 150, 130), (70, 100, 150))):
        c.rect(6, j, 3, 3, col)
    c.put(6, 2, (200, 220, 230)); c.put(7, 7, (230, 240, 190)); c.put(8, 11, (30, 50, 90))
    c.rect(4, 1, 1, 14, "o"); c.rect(11, 1, 1, 14, "N")
    c.rect(6, 5, 3, 1, "N"); c.rect(6, 9, 3, 1, "N"); c.rect(6, 13, 3, 1, "N")


@item("photo_print")
def _(c):
    c.rect(2, 2, 12, 12, "w")
    c.rect(2, 12, 12, 2, "W")
    c.rect(4, 4, 8, 7, "c")
    c.rect(4, 4, 8, 3, (150, 200, 232))
    c.disc(10, 6, 1.2, "y", False)
    c.draw(["..G.....", ".GGG..G.", "GGGGGGGG"], 4, 8)
    c.rect(4, 10, 8, 1, "X")
    c.put(2, 2, "h")


@item("darkroom_tongs")
def _(c):
    c.line(3, 13, 9, 1, "M", 1)
    c.line(4, 14, 14, 7, "m", 1)
    c.put(2, 13, "K"); c.put(3, 14, "K"); c.put(4, 15, "K") if False else None
    c.line(4, 10, 6, 7, "o", 2)
    c.line(7, 12, 10, 10, "o", 2)
    c.line(5, 11, 6, 9, "O"); c.line(8, 13, 10, 11, "O")
    c.rect(8, 0, 2, 2, "l"); c.put(9, 0, "h")
    c.rect(13, 6, 2, 2, "l"); c.put(14, 6, "h")
    c.put(5, 9, "h")


# ============================================================ 4. film production
@item("shotgun_microphone")
def _(c):
    c.line(2, 13, 12, 3, "b", 3)
    c.line(3, 13, 12, 4, "B", 1)
    for k in range(5, 12, 2):
        c.put(k, 15 - k - 1, "K")
    c.disc(12.5, 2.5, 2.2, "m")
    c.rect(1, 12, 3, 3, "r")
    c.put(6, 7, "l"); c.put(8, 5, "l")
    c.line(10, 3, 13, 0, "N", 2)


@item("wireless_microphone")
def _(c):
    c.disc(8, 4, 3.4, "M")
    for j in range(2, 7, 2):
        c.rect(5, j, 6, 1, "K")
    c.line(8, 1, 8, 7, "K")
    c.bevel(6, 7, 4, 7, "b")
    c.rect(6, 8, 4, 1, "t")
    c.put(7, 11, "g"); c.put(9, 11, "r")
    c.rect(7, 13, 2, 1, "K")


@item("audio_recorder")
def _(c):
    c.disc(5.5, 2.5, 1.8, "m"); c.disc(10.5, 2.5, 1.8, "m")
    c.bevel(3, 3, 10, 11, "b", 14)
    c.rect(5, 5, 6, 3, "G")
    c.rect(5, 5, 6, 1, "g")
    c.put(6, 7, "h"); c.put(8, 6, "h")
    c.put(5, 10, "r"); c.put(7, 10, "m"); c.put(9, 10, "m")
    c.disc(10.5, 11.5, 1.4, "M")
    c.rect(5, 12, 4, 1, "B")


@item("directors_megaphone")
def _(c):
    c.draw([
        "..........rr",
        "......rrrrRR",
        "...rrwwrrRRk",
        "..rwwwwrrRk.",
        "..rwwwwrRk..",
        "..rrwwrrRk..",
        "...rrrrRk...",
    ], 2, 2)
    c.rect(2, 3, 2, 5, "R")
    c.rect(3, 4, 1, 3, "k")
    c.rect(8, 9, 3, 4, "b")
    c.rect(8, 9, 3, 1, "B")
    c.put(7, 8, "m")
    c.put(12, 3, "m")


@item("film_reel")
def _(c):
    c.disc(8, 8, 7, "M")
    c.disc(8, 8, 4.4, "b", False)
    c.ring(8, 8, 4.4, 5, "K", False)
    for a in (90, 210, 330):
        x, y = 8 + 5.6 * math.cos(math.radians(a)), 8 - 5.6 * math.sin(math.radians(a))
        c.disc(x, y, 1.1, None or (24, 24, 30), False)
    c.disc(8, 8, 1.6, "l")
    c.put(8, 8, "k")
    c.put(5, 5, "h")


@item("video_tape")
def _(c):
    c.bevel(1, 4, 14, 9, "b", 14)
    c.rect(4, 5, 8, 3, "w")
    c.rect(4, 5, 8, 1, "r")
    c.rect(5, 7, 4, 1, "W")
    c.disc(4.5, 10.5, 1.5, "K", False); c.disc(11.5, 10.5, 1.5, "K", False)
    c.put(4, 10, "l"); c.put(11, 10, "l")
    c.rect(6, 10, 4, 2, "B")
    c.rect(6, 12, 4, 1, "m")


@item("timecode_generator")
def _(c):
    c.bevel(1, 4, 14, 9, "b", 14)
    c.rect(2, 5, 12, 5, "G")
    zero = ["xxx", "x.x", "x.x", "x.x", "xxx"]
    one = [".x.", "xx.", ".x.", ".x.", "xxx"]
    two = ["xxx", "..x", "xxx", "x..", "xxx"]
    three = ["xxx", "..x", ".xx", "..x", "xxx"]
    def put_glyph(g, ox):
        for j, row in enumerate(g):
            for i, ch in enumerate(row):
                c.put(ox + i, 5 + j, "g" if ch == "x" else "G")
    put_glyph(one, 2); put_glyph(two, 6); put_glyph(three, 11)
    c.put(9, 6, "g"); c.put(9, 8, "g")
    c.put(3, 11, "r"); c.put(5, 11, "y"); c.rect(10, 11, 4, 1, "m")
    c.rect(13, 1, 1, 3, "K"); c.put(13, 0, "l")


@item("follow_focus")
def _(c):
    c.disc(8, 6, 5.4, "K")
    c.ring(8, 6, 3.6, 4.6, "l", False)
    for a in range(0, 360, 30):
        x, y = 8 + 5.0 * math.cos(math.radians(a)), 6 + 5.0 * math.sin(math.radians(a))
        c.put(int(x), int(y), "m" if a % 60 else "k")
    c.disc(8, 6, 2.6, "b")
    c.rect(7, 5, 2, 2, "M")
    for i in (4, 6, 8, 10):
        c.put(i, 5, "w") if False else None
    c.rect(1, 12, 14, 2, "m"); c.rect(1, 12, 14, 1, "l")
    c.rect(6, 10, 4, 2, "B")
    c.put(3, 13, "k"); c.put(12, 13, "k")


# ============================================================ 5. electronics & crafting
@item("image_sensor")
def _(c):
    c.bevel(2, 2, 12, 12, "X", 16)
    c.rect(4, 4, 8, 8, "k")
    c.rect(5, 5, 6, 6, "U")
    for j in range(5, 11):
        for i in range(5, 11):
            if (i + j) % 3 == 0:
                c.put(i, j, "u")
    c.put(5, 5, "c"); c.put(6, 5, "c"); c.put(5, 6, "c")
    c.put(10, 10, "p")
    for k in range(3, 13, 2):
        c.put(k, 2, "t"); c.put(k, 13, "t"); c.put(2, k, "t"); c.put(13, k, "t")


@item("image_processor")
def _(c):
    c.bevel(3, 3, 10, 10, "b", 14)
    c.rect(5, 5, 6, 6, "B")
    c.rect(6, 6, 4, 4, "K")
    c.put(7, 7, "m"); c.put(8, 8, "m")
    c.put(3, 3, "w")
    for k in range(4, 12, 2):
        c.put(k, 2, "t"); c.put(k, 13, "t"); c.put(2, k, "t"); c.put(13, k, "t")
    c.put(2, 2, None)


@item("autofocus_module")
def _(c):
    c.bevel(2, 4, 12, 8, "b", 14)
    c.rect(3, 5, 7, 6, "B")
    c.draw([".r.r.r.", "..rrr..", ".r.r.r."], 3, 6)
    c.disc(12, 8, 1.8, "C", False); c.put(11, 7, "h")
    c.rect(4, 12, 6, 2, "o")
    c.rect(4, 13, 6, 1, "O")
    c.put(2, 3, "t") if False else None
    c.rect(5, 2, 4, 2, "K")


@item("optical_glass")
def _(c):
    c.draw([
        "....hhhhhh....",
        "...hccccccz...",
        "..hcczzzzccz..",
        ".hccczzzzcccC.",
        ".hczzzccczzcC.",
        ".hcczzccczccC.",
        ".zccczzczcccC.",
        "..zcccccccCC..",
        "...zCCCCCCC...",
        "....CCCCCC....",
    ], 1, 3)
    c.put(4, 5, "h"); c.put(5, 4, "h")
    c.put(11, 11, "u")


@item("precision_lens")
def _(c):
    c.ring(8, 8, 5.6, 7, "T")
    c.ring(8, 8, 5.6, 6, "Y", False)
    c.disc(8, 8, 5.4, "C")
    c.disc(8, 8, 3.8, "c")
    c.ring(8, 8, 2.2, 3, "z", False)
    c.disc(8, 8, 1.4, "U", False)
    c.put(5, 5, "h"); c.put(6, 4, "h"); c.put(4, 6, "h")
    c.put(11, 10, "w")
    c.put(8, 1, "t"); c.put(8, 14, "T")


@item("shutter_mechanism")
def _(c):
    c.disc(8, 8, 7, "K")
    c.ring(8, 8, 6, 7, "t", False)
    c.disc(8, 8, 5.2, "M")
    for k in range(6):
        a = math.radians(60 * k + 20)
        c.line(8, 8, int(8 + 5.2 * math.cos(a)), int(8 + 5.2 * math.sin(a)), "m")
    c.disc(8, 8, 2.2, "k", False)
    c.put(8, 8, "c")
    c.put(8, 1, "k"); c.put(8, 14, "k"); c.put(1, 8, "k"); c.put(14, 8, "k")


@item("camera_circuit")
def _(c):
    c.bevel(1, 2, 14, 12, "x", 18)
    for line in ((3, 4, 7, 4), (7, 4, 7, 8), (7, 8, 12, 8), (3, 11, 9, 11), (9, 11, 9, 9), (4, 6, 4, 9)):
        c.line(*line, "g")
    c.rect(9, 4, 4, 3, "b"); c.rect(9, 4, 4, 1, "B")
    c.rect(3, 6, 2, 2, "b") if False else None
    c.rect(11, 10, 3, 3, "k"); c.put(12, 11, "m")
    c.put(3, 4, "t"); c.put(12, 8, "t"); c.put(3, 11, "t"); c.put(5, 13, "t"); c.put(7, 13, "t")
    c.put(5, 8, "r"); c.put(5, 7, "r")


@item("stabilization_module")
def _(c):
    c.ring(8, 8, 6.2, 7.2, "r", False)
    # vertical ring (ellipse)
    for j in range(16):
        for i in range(16):
            dx, dy = i + .5 - 8, j + .5 - 8
            q = (dx / 3.2) ** 2 + (dy / 5.2) ** 2
            if 0.78 <= q <= 1.0:
                c.put(i, j, "u")
            q2 = (dx / 5.2) ** 2 + (dy / 3.2) ** 2
            if 0.78 <= q2 <= 1.0:
                c.put(i, j, "g")
    c.rect(7, 7, 2, 2, "b")
    c.put(7, 7, "l")
    c.put(8, 1, "R") if False else None


def main():
    force = "--force" in sys.argv
    only = None
    if "--only" in sys.argv:
        only = set(sys.argv[sys.argv.index("--only") + 1].split(","))
    TEX.mkdir(parents=True, exist_ok=True)
    MOD.mkdir(parents=True, exist_ok=True)
    PREV.mkdir(parents=True, exist_ok=True)
    made = []
    for name, fn in ITEMS.items():
        if only and name not in only:
            continue
        cv = Cv(zlib_seed(name))
        fn(cv)
        if not any(cv.px[i, j][3] for i in range(16) for j in range(16)):
            continue
        cv.outline()
        t, m = TEX / f"{name}.png", MOD / f"{name}.json"
        if t.exists() and not force:
            print("skip", name)
            continue
        cv.im.save(t)
        if not m.exists() or force:
            m.write_text(json.dumps({"parent": "minecraft:item/generated",
                                     "textures": {"layer0": f"cinecraft:item/{name}"}}, indent=2) + "\n")
        made.append((name, cv.im))
    if made:
        sheet = Image.new("RGBA", (8 * 16 * 6, ((len(made) + 7) // 8) * 16 * 6), (74, 78, 88, 255))
        for k, (n, im) in enumerate(made):
            sheet.alpha_composite(im.resize((96, 96), Image.NEAREST), ((k % 8) * 96, (k // 8) * 96))
        sheet.save(PREV / "_gear_sheet.png")
    print("generated", len(made), [n for n, _ in made])


def zlib_seed(s):
    return sum(ord(ch) * (i + 1) for i, ch in enumerate(s))


if __name__ == "__main__":
    main()
