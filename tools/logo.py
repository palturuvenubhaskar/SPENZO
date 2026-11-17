#!/usr/bin/env python3
"""Spenzo brand mark generator.

Draws the monoline S, previews it behind launcher masks, and writes the
Android vector drawables plus the brand SVGs / PNGs.

    python tools/logo.py            # previews + all assets
    python tools/logo.py --preview  # previews only
"""

import math
import os
import sys

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
BRAND = os.path.join(ROOT, "brand")
OUT = os.path.join(os.environ.get("TEMP", "."), "opencode")

# --- geometry: monoline S on a 108x108 grid (cubic beziers) ---
START = (67, 35)
SEGS = [
    ((62, 29), (56, 26), (49, 26)),
    ((40, 26), (33, 32), (33, 41)),
    ((33, 50), (39, 54), (52, 57)),
    ((65, 60), (71, 64), (71, 71)),
    ((71, 79), (64, 85), (54, 85)),
    ((47, 85), (42, 85), (37, 82)),
]
STROKE = 13.0          # stroke weight on the 108 grid
MARK_HEIGHT = 61.0     # final stroke bbox height, keeps it inside the 66dp safe circle

BG_INNER = "#1B3348"
BG_OUTER = "#060C13"
FG_TOP = "#4AE6B0"
FG_BOTTOM = "#1A9E74"


def _fit():
    """Scale so the painted bbox is MARK_HEIGHT tall and centred on (54, 54)."""
    raw = flatten()
    half = STROKE / 2
    top = min(p[1] for p in raw) - half
    bot = max(p[1] for p in raw) + half
    scale = MARK_HEIGHT / (bot - top)
    pts = [(54 + (x - 54) * scale, 54 + (y - 54) * scale) for x, y in raw]
    return pts, STROKE * scale


def cubic(p0, p1, p2, p3, n=120):
    pts = []
    for i in range(n + 1):
        t = i / n
        u = 1 - t
        pts.append((
            u**3 * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t**3 * p3[0],
            u**3 * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t**3 * p3[1],
        ))
    return pts


def flatten():
    pts, cur = [], START
    for c1, c2, end in SEGS:
        pts += cubic(cur, c1, c2, end)
        cur = end
    return pts


def _fmt(v):
    return f"{round(v, 2):g}"


def path_data(scale):
    """Path data for the S scaled about (54, 54)."""
    def tc(p):
        return (54 + (p[0] - 54) * scale, 54 + (p[1] - 54) * scale)

    out = ["M" + ",".join(_fmt(v) for v in tc(START))]
    for c1, c2, end in SEGS:
        out.append("C" + " ".join(",".join(_fmt(v) for v in tc(p)) for p in (c1, c2, end)))
    return " ".join(out)


def scale_factor():
    raw = flatten()
    half = STROKE / 2
    top = min(p[1] for p in raw) - half
    bot = max(p[1] for p in raw) + half
    return MARK_HEIGHT / (bot - top)


# --- previews -------------------------------------------------------------

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def _rgb(hexstr):
    h = hexstr.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def background(size):
    img = Image.new("RGB", (size, size))
    d = ImageDraw.Draw(img)
    inner, outer = _rgb(BG_INNER), _rgb(BG_OUTER)
    cx = cy = size / 2
    maxd = math.hypot(cx, cy)
    steps = 200
    for i in range(steps, -1, -1):
        t = i / steps
        r = maxd * t
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=lerp(inner, outer, t))
    return img


def mark(size, pts, sw):
    layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    f = size / 108.0
    r = sw * f / 2
    y0, y1 = min(p[1] for p in pts), max(p[1] for p in pts)
    top, bot = _rgb(FG_TOP), _rgb(FG_BOTTOM)
    for (x, y) in pts:
        t = (y - y0) / max(1e-6, y1 - y0)
        d.ellipse([x * f - r, y * f - r, x * f + r, y * f + r], fill=lerp(top, bot, t) + (255,))
    return layer


def mask(size, shape):
    m = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(m)
    if shape == "circle":
        inset = size * (108 - 66) / 2 / 108
        d.ellipse([inset, inset, size - inset, size - inset], fill=255)
    elif shape == "squircle":
        inset = size * (108 - 74) / 2 / 108
        d.rounded_rectangle([inset, inset, size - inset, size - inset], radius=size * 0.22, fill=255)
    else:
        d.rectangle([0, 0, size, size], fill=255)
    return m


def compose(size, shape, supersample=4):
    big = size * supersample
    img = background(big).convert("RGBA")
    pts, sw = _fit()
    img.alpha_composite(mark(big, pts, sw))
    img.putalpha(mask(big, shape))
    return img.resize((size, size), Image.LANCZOS)


def write_previews():
    os.makedirs(OUT, exist_ok=True)
    sheet = Image.new("RGBA", (192 * 3 + 24, 192), "#000000")
    for i, shape in enumerate(("square", "squircle", "circle")):
        tile = compose(192, shape)
        sheet.paste(tile, (i * 204, 0), tile)
    sheet.save(os.path.join(OUT, "_logo_192.png"))

    small = Image.new("RGBA", (60 * 3 + 96, 96), "#000000")
    x = 0
    for shape in ("square", "squircle", "circle"):
        tile = compose(48, shape)
        small.paste(tile, (x, 0), tile)
        x += 60
    tile = compose(96, "squircle")
    small.paste(tile, (x, 0), tile)
    small.save(os.path.join(OUT, "_logo_small.png"))
    print("previews ->", OUT)


# --- assets ---------------------------------------------------------------

BACKGROUND_XML = """<?xml version="1.0" encoding="utf-8"?>
<!-- Adaptive icon background: deep navy with a soft radial lift behind the mark. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="radial"
                android:centerX="54"
                android:centerY="52"
                android:gradientRadius="74"
                android:startColor="#FF{inner}"
                android:endColor="#FF{outer}" />
        </aapt:attr>
    </path>
</vector>
"""

FOREGROUND_XML = """<?xml version="1.0" encoding="utf-8"?>
<!-- Spenzo mark: one continuous monoline S, mint gradient, inside the adaptive safe zone. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#00000000"
        android:pathData="{path}"
        android:strokeWidth="{stroke}"
        android:strokeLineCap="round"
        android:strokeLineJoin="round">
        <aapt:attr name="android:strokeColor">
            <gradient
                android:type="linear"
                android:startX="54"
                android:startY="24"
                android:endX="54"
                android:endY="84"
                android:startColor="#FF{top}"
                android:endColor="#FF{bottom}" />
        </aapt:attr>
    </path>
</vector>
"""

MONOCHROME_XML = """<?xml version="1.0" encoding="utf-8"?>
<!-- Themed-icon layer: the system tints this from its alpha channel. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#00000000"
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="{stroke}"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="{path}" />
</vector>
"""

SVG_TEMPLATE = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" role="img" aria-labelledby="title desc">
  <title id="title">Spenzo mark</title>
  <desc id="desc">A single monoline S for everyday money flow.</desc>
  <path d="{path}"
        fill="none" stroke="{stroke_color}" stroke-width="{stroke}" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
"""

SVG_ICON_TEMPLATE = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" role="img" aria-labelledby="title desc">
  <title id="title">Spenzo app icon</title>
  <desc id="desc">The Spenzo S in mint on deep navy.</desc>
  <defs>
    <radialGradient id="bg" cx="50%" cy="48%" r="69%">
      <stop offset="0" stop-color="{bg_inner}"/>
      <stop offset="1" stop-color="{bg_outer}"/>
    </radialGradient>
    <linearGradient id="fg" x1="0" y1="22%" x2="0" y2="78%">
      <stop offset="0" stop-color="{fg_top}"/>
      <stop offset="1" stop-color="{fg_bottom}"/>
    </linearGradient>
  </defs>
  <rect width="108" height="108" fill="url(#bg)"/>
  <path d="{path}"
        fill="none" stroke="url(#fg)" stroke-width="{stroke}" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
"""


def write_assets():
    s = scale_factor()
    path = path_data(s)
    stroke = round(STROKE * s, 2)

    files = {
        os.path.join(RES, "drawable", "ic_launcher_background.xml"): BACKGROUND_XML.format(
            inner=BG_INNER.lstrip("#"), outer=BG_OUTER.lstrip("#")),
        os.path.join(RES, "drawable", "ic_launcher_foreground.xml"): FOREGROUND_XML.format(
            path=path, stroke=stroke, top=FG_TOP.lstrip("#"), bottom=FG_BOTTOM.lstrip("#")),
        os.path.join(RES, "drawable", "ic_launcher_monochrome.xml"): MONOCHROME_XML.format(
            path=path, stroke=stroke),
        os.path.join(BRAND, "spenzo-mark.svg"): SVG_TEMPLATE.format(
            path=path, stroke=stroke, stroke_color="#3DDC97"),
        os.path.join(BRAND, "spenzo-mark-white.svg"): SVG_TEMPLATE.format(
            path=path, stroke=stroke, stroke_color="#FFFFFF"),
        os.path.join(BRAND, "spenzo-mark-black.svg"): SVG_TEMPLATE.format(
            path=path, stroke=stroke, stroke_color="#0B1014"),
        os.path.join(BRAND, "spenzo-icon.svg"): SVG_ICON_TEMPLATE.format(
            path=path, stroke=stroke, bg_inner=BG_INNER, bg_outer=BG_OUTER,
            fg_top=FG_TOP, fg_bottom=FG_BOTTOM),
    }
    for target, content in files.items():
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with open(target, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(content)
        print("wrote", os.path.relpath(target, ROOT))

    os.makedirs(BRAND, exist_ok=True)
    compose(512, "square").save(os.path.join(BRAND, "spenzo-icon-512.png"))
    print("wrote brand/spenzo-icon-512.png")


if __name__ == "__main__":
    write_previews()
    if "--preview" not in sys.argv:
        write_assets()
