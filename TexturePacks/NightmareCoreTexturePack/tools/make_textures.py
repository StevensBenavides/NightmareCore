from PIL import Image, ImageDraw
import json
import math
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
REPO = Path(__file__).resolve().parents[3]
TEX = ROOT / "assets" / "nightmare" / "textures" / "item"
MODELS = ROOT / "assets" / "nightmare" / "models" / "item"
ITEMS = ROOT / "assets" / "nightmare" / "items"
SELECTORS = ROOT / "assets" / "minecraft" / "items"

SIZE = 32
CANVAS_CENTER = SIZE // 2

BLACK = (0, 0, 0, 255)
WHITE = (255, 255, 255, 255)
CREAM = (245, 245, 220, 255)
ABYSSAL_PURPLE = (138, 43, 226, 255)

def color(hexstr, alpha=255):
    return tuple(int(hexstr[i:i+2], 16) for i in (1, 3, 5)) + (alpha,)

def darken(c, amount=50):
    return tuple(max(0, v - amount) for v in c[:3]) + (c[3],)

def lighten(c, amount=60):
    return tuple(min(255, v + amount) for v in c[:3]) + (c[3],)

def shade(c, amount):
    if amount < 0:
        return darken(c, -amount)
    return lighten(c, amount)

def hexc(spec):
    if isinstance(spec, tuple):
        return spec
    if spec.startswith('#'):
        return color(spec)
    if spec in {'white', 'black', 'red', 'green', 'blue'}:
        return {'white': WHITE, 'black': BLACK, 'red': color('#CC2222'), 'green': color('#228822'), 'blue': color('#2255AA')}.get(spec, BLACK)
    raise ValueError(spec)

def rgb(c):
    return tuple(c[:3])

# ------------------------------------------------------------------
# Simple drawing helpers using a 32x32 canvas
# ------------------------------------------------------------------

def make_canvas():
    return Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

def draw_ellipse(d, x0, y0, x1, y1, fill, outline=None, width=1):
    d.ellipse((x0, y0, x1, y1), fill=fill, outline=outline, width=width)

def draw_polygon(d, points, fill, outline=None, width=1):
    d.polygon(points, fill=fill, outline=outline, width=width)

def draw_line(d, pts, fill, width=1):
    d.line(pts, fill=fill, width=width)

def draw_rect(d, x0, y0, x1, y1, fill, outline=None, width=1):
    d.rectangle((x0, y0, x1, y1), fill=fill, outline=outline, width=width)

def draw_rounded_rect(d, x0, y0, x1, y1, radius, fill, outline=None, width=1):
    d.rounded_rectangle((x0, y0, x1, y1), radius=radius, fill=fill, outline=outline, width=width)

# ------------------------------------------------------------------
# Precise symbol shapes
# ------------------------------------------------------------------
def draw_pixel_heart(d, cx, cy, w, h, fill, edge, notch_depth=0, tip_sharpness=0, tip_length=1):
    xs = range(int(cx - w // 2), int(cx + w // 2) + 1)
    ys = range(int(cy - h // 2), int(cy + h // 2) + 1)
    inside = set()
    for y in ys:
        for x in xs:
            nx = (x - cx) / (w / 2)
            ny = (cy - y) / (h / 2)
            if (nx * nx + ny * ny - 1) ** 3 - nx * nx * ny ** 3 <= 0:
                inside.add((x, y))
                d.point((x, y), fill=fill)
    if notch_depth > 0:
        top = int(cy - h // 2)
        for depth in range(1, notch_depth + 1):
            half_width = max(0, notch_depth - depth)
            for dx in range(-half_width, half_width + 1):
                carved = (int(cx + dx), top + depth)
                inside.discard(carved)
                d.point(carved, fill=(0, 0, 0, 0))
    if tip_sharpness > 0:
        bottom = int(cy + h // 2)
        for y in range(int(cy + h * 0.15), bottom + 1):
            row = sorted(x for x in xs if (x, y) in inside)
            if len(row) > 2 * tip_sharpness + 1:
                for x in row[:tip_sharpness] + row[-tip_sharpness:]:
                    inside.discard((x, y))
                    d.point((x, y), fill=(0, 0, 0, 0))
        for extra in range(1, tip_length + 1):
            inside.add((int(cx), bottom + extra))
            d.point((int(cx), bottom + extra), fill=fill)
    for (x, y) in inside:
        if any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            d.point((x, y), fill=edge)

def draw_heart(d, x, y, size, fill, outline, width=1):
    draw_pixel_heart(d, x + size / 2, y + size * 0.52, size, size * 0.92, fill, outline)
    draw_line(d, [(x + size * 0.34, y + size * 0.30), (x + size * 0.42, y + size * 0.30)], lighten(fill, 70), 1)

def draw_bolt(d, x, y, w, h, fill, outline, width=2):
    pts = [
        (x + w * 0.58, y), (x + w * 0.18, y + h * 0.56),
        (x + w * 0.44, y + h * 0.56), (x + w * 0.34, y + h),
        (x + w * 0.78, y + h * 0.42), (x + w * 0.52, y + h * 0.42),
    ]
    draw_polygon(d, pts, fill, outline, width)
    draw_line(d, [(x + w * 0.50, y + h * 0.18), (x + w * 0.38, y + h * 0.48)], lighten(fill, 70), 1)

def draw_star(d, cx, cy, r, points, fill, outline, width=2):
    pts = []
    for i in range(points * 2):
        ang = math.pi * i / points - math.pi / 2
        radius = r if i % 2 == 0 else r * 0.42
        pts.append((cx + math.cos(ang) * radius, cy + math.sin(ang) * radius))
    draw_polygon(d, pts, fill, outline, width)
    draw_ellipse(d, cx - r * 0.20, cy - r * 0.20, cx + r * 0.20, cy + r * 0.20, lighten(fill, 60), outline, 1)

def draw_snowflake(d, cx, cy, r, fill, outline, width=2):
    for i in range(3):
        ang = math.pi * i / 3
        x1 = cx + math.cos(ang) * r
        y1 = cy + math.sin(ang) * r
        x2 = cx - math.cos(ang) * r
        y2 = cy - math.sin(ang) * r
        draw_line(d, [(x1, y1), (x2, y2)], outline, width + 1)
        draw_line(d, [(x1, y1), (x2, y2)], fill, width)
    draw_ellipse(d, cx - 2, cy - 2, cx + 2, cy + 2, fill, outline, 2)

def draw_drop(d, cx, cy, w, h, fill, outline, width=2):
    pts = [
        (cx, cy - h // 2),
        (cx + w // 2, cy + 1),
        (cx + w // 4, cy + h // 2),
        (cx - w // 4, cy + h // 2),
        (cx - w // 2, cy + 1),
    ]
    draw_polygon(d, pts, fill, outline, width)
    draw_line(d, [(cx - 2, cy), (cx - 1, cy + 3)], WHITE, 1)

def draw_shield(d, x0, y0, x1, y1, fill, outline, width=2):
    d.polygon([(x0, y0), (x1, y0), (x1, (y0 + y1) // 2), ((x0 + x1) // 2, y1), (x0, (y0 + y1) // 2)],
              fill=fill, outline=outline, width=width)

def draw_sword(d, x0, y0, x1, y1, fill, outline, width=2):
    cx = (x0 + x1) // 2
    draw_polygon(d, [(cx - 2, y0), (cx + 2, y0 + 2), (cx + 1, y1 - 3), (cx - 1, y1 - 3)], fill, outline, 1)
    draw_line(d, [(x0 + 1, y1 - 3), (x1 - 1, y1 - 3)], outline, 3)
    draw_line(d, [(cx, y1 - 3), (cx, y1)], outline, 2)

def draw_axe(d, x0, y0, x1, y1, haft, head, outline):
    draw_line(d, [(x0, y1), (x1 - 2, y0 + 3)], outline, 5)
    draw_line(d, [(x0, y1), (x1 - 2, y0 + 3)], haft, 3)
    draw_polygon(d, [(x1 - 7, y0 - 2), (x1 + 1, y0), (x1 - 1, y0 + 6), (x1 - 7, y0 + 4)], head, outline, 2)
    draw_line(d, [(x0 + 2, y1 - 5), (x0 + 5, y1 - 2)], outline, 1)

def draw_demon_face(d, cx, cy, w, fill, horn, outline):
    draw_polygon(d, [(cx - w - 1, cy + 1), (cx - w - 5, cy - 8), (cx - w + 1, cy - 3)], horn, outline, 1)
    draw_polygon(d, [(cx + w + 1, cy + 1), (cx + w + 5, cy - 8), (cx + w - 1, cy - 3)], horn, outline, 1)
    draw_polygon(d, [(cx - w, cy - 5), (cx + w, cy - 5), (cx + w - 1, cy + 1), (cx + 3, cy + 7), (cx - 3, cy + 7), (cx - w + 1, cy + 1)], fill, outline, 1)
    draw_line(d, [(cx - w + 2, cy - 3), (cx, cy - 1)], shade(fill, -70), 1)
    draw_line(d, [(cx, cy - 1), (cx + w - 2, cy - 3)], shade(fill, -70), 1)
    draw_line(d, [(cx - 4, cy), (cx - 2, cy + 1)], color('#FFE94A'), 1)
    draw_line(d, [(cx + 2, cy + 1), (cx + 4, cy)], color('#FFE94A'), 1)
    d.point((cx - 4, cy), fill=color('#FF2A00'))
    d.point((cx + 4, cy), fill=color('#FF2A00'))
    draw_ellipse(d, cx - 2, cy + 3, cx + 2, cy + 6, BLACK, outline, 1)
    draw_polygon(d, [(cx - 1, cy + 3), (cx, cy + 3), (cx - 1, cy + 5)], WHITE, outline, 1)
    draw_polygon(d, [(cx, cy + 3), (cx + 1, cy + 3), (cx + 1, cy + 5)], WHITE, outline, 1)

def draw_flame(d, cx, cy, w, h, outer, inner, outline):
    draw_polygon(d, [(cx, cy - h // 2), (cx + w // 2, cy), (cx + w // 4, cy + h // 2), (cx - w // 4, cy + h // 2), (cx - w // 2, cy)], outer, outline, 2)
    draw_polygon(d, [(cx, cy - h // 4), (cx + w // 4, cy + 1), (cx, cy + h // 3), (cx - w // 4, cy + 1)], inner, outline, 1)

def draw_rift(d, cx, cy, w, h, core, glow):
    draw_line(d, [(cx - 2, cy - h // 2), (cx + 2, cy - 2), (cx - 3, cy + 2), (cx + 3, cy + h // 2)], glow, 5)
    draw_line(d, [(cx - 2, cy - h // 2), (cx + 2, cy - 2), (cx - 3, cy + 2), (cx + 3, cy + h // 2)], core, 2)

def draw_wind(d, cx, cy, w, fill, outline):
    for i, dy in enumerate((-4, 0, 4)):
        draw_line(d, [(cx - w // 2, cy + dy), (cx + w // 2 - i, cy + dy)], outline, 3)
        draw_line(d, [(cx - w // 2, cy + dy), (cx + w // 2 - i, cy + dy)], fill, 1)
        draw_line(d, [(cx + w // 2 - i - 3, cy + dy - 2), (cx + w // 2 - i, cy + dy)], fill, 1)

def draw_portal(d, cx, cy, r, ring, hole, spark):
    draw_ellipse(d, cx - r, cy - r, cx + r, cy + r, hole, BLACK, 2)
    d.ellipse((cx - r, cy - r, cx + r, cy + r), outline=ring, width=2)
    d.ellipse((cx - r + 3, cy - r + 3, cx + r - 3, cy + r - 3), outline=spark, width=1)

def draw_claws(d, cx, cy, w, h, fill, glow):
    for dx in (-4, 0, 4):
        draw_line(d, [(cx + dx - 2, cy - h // 2), (cx + dx + 2, cy + h // 2)], glow, 4)
        draw_line(d, [(cx + dx - 2, cy - h // 2), (cx + dx + 2, cy + h // 2)], fill, 2)

def draw_maw(d, cx, cy, w, h, mouth, outline, fang):
    draw_ellipse(d, cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2, mouth, outline, 2)
    for fx in range(-5, 6, 2):
        draw_polygon(d, [(cx + fx - 1, cy - h // 2 + 1), (cx + fx + 1, cy - h // 2 + 1), (cx + fx, cy - h // 2 + 4)], fang, outline, 1)
        draw_polygon(d, [(cx + fx - 1, cy + h // 2 - 1), (cx + fx + 1, cy + h // 2 - 1), (cx + fx, cy + h // 2 - 4)], fang, outline, 1)

def draw_nucleus(d, cx, cy, r, body, core, outline):
    draw_ellipse(d, cx - r, cy - r, cx + r, cy + r, body, outline, 2)
    draw_ellipse(d, cx - r // 2, cy - r // 2, cx + r // 2, cy + r // 2, core, outline, 1)
    draw_ellipse(d, cx - 1, cy - 1, cx + 1, cy + 1, WHITE, outline, 1)

def draw_abyssal_tendrils(d, cx, cy, body_r, reach, fill, outline, width=2):
    for i in range(9):
        ang = -math.pi / 2 + i * (math.pi * 1.35 / 8) - math.pi * 0.17
        x = cx + math.cos(ang) * reach
        y = cy + math.sin(ang) * reach
        draw_line(d, [(cx, cy), (int(x), int(y))], outline, width + 1)
        draw_line(d, [(cx, cy), (int(x), int(y))], fill, width)
        hx = int(x + math.cos(ang + 1.2) * 3)
        hy = int(y + math.sin(ang + 1.2) * 3)
        draw_line(d, [(int(x), int(y)), (hx, hy)], outline, width)
        draw_line(d, [(int(x), int(y)), (hx, hy)], fill, max(1, width - 1))
    draw_ellipse(d, cx - body_r, cy - body_r, cx + body_r, cy + body_r, fill, outline, width)

def draw_eye(d, cx, cy, w, h, fill, outline, width=2):
    d.ellipse((cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2), fill=fill, outline=outline, width=width)
    draw_ellipse(d, cx - w // 8, cy - h // 8, cx + w // 8, cy + h // 8, WHITE, outline, 1)


def book_base(spec):
    base_color = color(spec["cover"])
    img = make_canvas()
    d = ImageDraw.Draw(img)
    dark = darken(base_color, 72)
    light = lighten(base_color, 38)
    panel = darken(base_color, 38)
    draw_rounded_rect(d, 3, 3, 28, 28, 4, base_color, BLACK, 2)
    draw_rect(d, 5, 5, 9, 27, dark, BLACK, 1)
    draw_rect(d, 23, 5, 27, 27, CREAM, BLACK, 1)
    draw_line(d, [(23, 7), (23, 25)], shade(CREAM, -48), 1)
    draw_line(d, [(11, 6), (11, 26)], light, 1)
    draw_rounded_rect(d, 10, 9, 21, 23, 3, panel, BLACK, 2)
    draw_rounded_rect(d, 11, 10, 20, 22, 2, None, lighten(panel, 28), 1)
    return img, d

def symbolize(d, id, spec):
    outline = BLACK
    cx = 15
    cy = 16
    if id == "lifesteal":
        draw_pixel_heart(d, cx, cy, 12, 11, color('#FF5B7A'), BLACK)
        draw_line(d, [(cx - 3, cy - 3), (cx - 2, cy - 3)], WHITE, 1)
    elif id == "stormcaller":
        draw_bolt(d, 10, 9, 11, 14, color('#FFD23F'), BLACK, 2)
        draw_line(d, [(14, 12), (13, 18)], color('#7A4A00'), 1)
    elif id == "blast_edge":
        draw_star(d, cx, cy, 7, 8, color('#FFB020'), BLACK, 2)
        draw_ellipse(d, cx - 2, cy - 2, cx + 2, cy + 2, color('#FF4D00'), BLACK, 1)
    elif id == "frostbite":
        draw_snowflake(d, cx, cy, 6, color('#BFF3FF'), BLACK, 2)
        draw_line(d, [(cx - 1, cy - 4), (cx + 1, cy - 2)], WHITE, 1)
    elif id == "venom":
        draw_drop(d, cx, cy, 9, 12, color('#7CFC6A'), BLACK, 2)
        draw_drop(d, cx + 4, cy + 4, 3, 4, color('#1E8A2E'), BLACK, 1)
    elif id == "executioner":
        draw_axe(d, 10, 11, 21, 22, color('#4A2C14'), color('#D7DEE8'), BLACK)
    elif id == "berserker":
        draw_demon_face(d, cx, cy, 6, color('#FF5A2A'), color('#FFE8B0'), BLACK)
    elif id == "soul_harvest":
        draw_flame(d, cx, cy, 10, 14, color('#8A2BE2'), color('#D6A8FF'), BLACK)
        d.point((cx, cy - 5), fill=WHITE)
    elif id == "void_rend":
        draw_rift(d, cx, cy, 8, 13, BLACK, color('#8A2BE2'))
    elif id == "guardian":
        draw_shield(d, 10, 9, 20, 23, color('#3E8BFF'), BLACK, 2)
        draw_line(d, [(cx, 12), (cx, 20)], BLACK, 3)
        draw_line(d, [(12, 15), (18, 15)], BLACK, 3)
        draw_line(d, [(cx, 12), (cx, 20)], WHITE, 1)
        draw_line(d, [(12, 15), (18, 15)], WHITE, 1)
    elif id == "retribution":
        draw_star(d, cx, cy, 6, 8, color('#FFD76A'), BLACK, 2)
        draw_polygon(d, [(cx - 1, cy - 7), (cx + 1, cy - 7), (cx, cy + 3)], color('#E8EEF6'), BLACK, 1)
        draw_line(d, [(cx - 4, cy + 3), (cx + 4, cy + 3)], BLACK, 3)
        draw_line(d, [(cx - 4, cy + 3), (cx + 4, cy + 3)], color('#C81E3A'), 1)
    elif id == "second_wind":
        draw_wind(d, cx, cy, 12, WHITE, BLACK)
    elif id == "blink":
        draw_portal(d, cx, cy, 6, color('#7CFFC4'), color('#063B26'), WHITE)
        draw_line(d, [(9, 10), (11, 8)], WHITE, 1)
    elif id == "nightmare_mark":
        draw_claws(d, cx, cy, 9, 13, BLACK, color('#FF2A2A'))
    elif id == "aegis":
        draw_shield(d, 10, 9, 20, 23, color('#E8A81C'), BLACK, 2)
        draw_shield(d, 12, 11, 18, 20, color('#7A2E00'), BLACK, 1)
        draw_ellipse(d, cx - 2, cy - 1, cx + 2, cy + 3, color('#FFF3B0'), BLACK, 1)
    elif id == "abyssal_howl":
        draw_maw(d, cx, cy, 13, 11, color('#12041F'), BLACK, color('#E8D8FF'))
    elif id == "abyssal_core":
        draw_nucleus(d, cx, cy, 6, color('#14041F'), ABYSSAL_PURPLE, BLACK)
        draw_line(d, [(cx - 7, cy), (cx - 5, cy)], ABYSSAL_PURPLE, 1)
        draw_line(d, [(cx + 5, cy), (cx + 7, cy)], ABYSSAL_PURPLE, 1)
    return d

BOOKS = {
    "lifesteal":     {"cover": "#DC143C", "accent": "#FFFFFF", "shadow": "#8B0000", "highlight": "#FFB6C1"},
    "stormcaller":   {"cover": "#FF8C00", "accent": "#FFFFFF", "shadow": "#CC5500", "highlight": "#FFE4B5"},
    "blast_edge":    {"cover": "#FF4500", "accent": "#FFFFFF", "shadow": "#A52A2A", "highlight": "#FFB347"},
    "frostbite":     {"cover": "#4169E1", "accent": "#FFFFFF", "shadow": "#003366", "highlight": "#AFEEEE"},
    "venom":         {"cover": "#32CD32", "accent": "#FFFFFF", "shadow": "#006400", "highlight": "#98FB98"},
    "executioner":   {"cover": "#708090", "accent": "#FFFFFF", "shadow": "#4A4A4A", "highlight": "#D3D3D3"},
    "berserker":     {"cover": "#DC143C", "accent": "#FFFFFF", "shadow": "#8B0000", "highlight": "#FF6347"},
    "soul_harvest":  {"cover": "#9932CC", "accent": "#FFFFFF", "shadow": "#4B0082", "highlight": "#BA55D3"},
    "void_rend":     {"cover": "#711C91", "accent": "#FFFFFF", "shadow": "#2B0038", "highlight": "#D4A5C1"},
    "guardian":      {"cover": "#0066CC", "accent": "#FFFFFF", "shadow": "#003366", "highlight": "#87CEEB"},
    "retribution":   {"cover": "#778899", "accent": "#FFFFFF", "shadow": "#36454F", "highlight": "#B0C4DE"},
    "second_wind":   {"cover": "#00CED1", "accent": "#FFFFFF", "shadow": "#008B8B", "highlight": "#AFEEEE"},
    "blink":         {"cover": "#228B22", "accent": "#FFFFFF", "shadow": "#006400", "highlight": "#90EE90"},
    "nightmare_mark":{"cover": "#8B0000", "accent": "#FFFFFF", "shadow": "#4A0000", "highlight": "#FF4500"},
    "aegis":         {"cover": "#DAA520", "accent": "#FFFFFF", "shadow": "#8B6914", "highlight": "#F4D03F"},
    "abyssal_howl":  {"cover": "#4B0082", "accent": "#FFFFFF", "shadow": "#2D0033", "highlight": "#BA55D3"},
    "abyssal_core":  {"cover": "#2B0038", "accent": "#FFFFFF", "shadow": "#15001A", "highlight": "#9B59B6"},
}

def draw_book(id, spec):
    img, d = book_base(spec)
    symbolize(d, id, spec)
    return img

def build_texture(id):
    if id == "soul_heart":
        img = make_canvas()
        d = ImageDraw.Draw(img)
        draw_pixel_heart(d, 16, 14, 20, 22, WHITE, BLACK, notch_depth=4, tip_sharpness=2,
                         tip_length=0)
        # Clean notch: remove every dark pixel inside the V so the dip reads
        # as transparency instead of an outline. White lobes are untouched.
        for y, x0, x1 in [(3, 13, 19), (4, 13, 19), (5, 14, 18),
                          (6, 14, 18), (7, 15, 17), (8, 16, 16)]:
            for x in range(x0, x1 + 1):
                if max(img.getpixel((x, y))[:3]) < 100:
                    d.point((x, y), fill=(0, 0, 0, 0))
        # Remove any dark stalk below the lowest light pixel of the point.
        column = range(13, 20)
        light_rows = [y for y in range(SIZE) for x in column
                      if img.getpixel((x, y))[3] > 0 and min(img.getpixel((x, y))[:3]) > 200]
        if light_rows:
            for y in range(max(light_rows) + 1, SIZE):
                for x in column:
                    pixel = img.getpixel((x, y))
                    if pixel[3] > 0 and max(pixel[:3]) < 100:
                        d.point((x, y), fill=(0, 0, 0, 0))
        # Taper the very bottom: on the lowest row that still holds light,
        # drop dark pixels sticking out sideways beyond the light pixels, so
        # the tip ends clean instead of sprouting a dark nub.
        if light_rows:
            bottom = max(light_rows)
            widest = max(abs(x - 16) for y in [bottom] for x in column
                         if img.getpixel((x, y))[3] > 0 and min(img.getpixel((x, y))[:3]) > 200)
            for x in column:
                pixel = img.getpixel((x, bottom))
                if pixel[3] > 0 and max(pixel[:3]) < 100 and abs(x - 16) > widest:
                    d.point((x, bottom), fill=(0, 0, 0, 0))
        draw_line(d, [(11, 10), (13, 10)], color('#D9D9D9'), 2)
        draw_line(d, [(19, 12), (19, 17)], color('#EDEDED'), 1)
        return img
    if id == "abyssal_tendrils":
        img = make_canvas()
        d = ImageDraw.Draw(img)
        draw_abyssal_tendrils(d, 16, 16, 6, 12, color('#08040D'), BLACK, 2)
        draw_ellipse(d, 13, 13, 19, 19, color('#08040D'), BLACK, 2)
        draw_ellipse(d, 14, 14, 18, 18, ABYSSAL_PURPLE, BLACK, 1)
        d.point((16, 16), fill=ABYSSAL_PURPLE)
        return img
    if id == "abyssal_ink":
        img = make_canvas()
        d = ImageDraw.Draw(img)
        draw_drop(d, 16, 15, 13, 18, color('#070309'), BLACK, 2)
        draw_ellipse(d, 13, 17, 19, 22, color('#1D0B2A'), BLACK, 1)
        draw_line(d, [(13, 12), (14, 15)], color('#8A5CFF'), 1)
        return img
    if id == "abyssal_nucleus":
        img = make_canvas()
        d = ImageDraw.Draw(img)
        draw_polygon(d, [(16, 5), (27, 16), (16, 27), (5, 16)], BLACK, BLACK, 2)
        draw_polygon(d, [(16, 8), (24, 16), (16, 24), (8, 16)], color('#12041F'), BLACK, 2)
        draw_polygon(d, [(16, 12), (20, 16), (16, 20), (12, 16)], ABYSSAL_PURPLE, BLACK, 1)
        d.point((16, 16), fill=ABYSSAL_PURPLE)
        return img
    return draw_book(id, BOOKS[id])

def save_texture(path, img):
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(str(path), "PNG")

def make_textures():
    ids = list(BOOKS.keys()) + ["soul_heart", "abyssal_tendrils", "abyssal_ink", "abyssal_nucleus"]
    for id in ids:
        img = build_texture(id)
        save_texture(TEX / f"{id}.png", img)
    return ids

def make_models(ids):
    for id in ids:
        path = MODELS / f"{id}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps({
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"nightmare:item/{id}"}
        }, indent=2))

def make_item_definitions(ids):
    for id in ids:
        path = ITEMS / f"{id}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps({
            "model": {"type": "minecraft:model", "model": f"nightmare:item/{id}"}
        }, indent=2))

def make_selector(case_ids, item_id, vanilla):
    cases = [{
        "when": f"nightmare:{id}",
        "model": {"type": "minecraft:model", "model": f"nightmare:item/{id}"}
    } for id in case_ids]
    data = {
        "model": {
            "type": "minecraft:select",
            "property": "minecraft:custom_model_data",
            "index": 0,
            "cases": cases,
            "fallback": {"type": "minecraft:model", "model": f"minecraft:item/{vanilla}"}
        }
    }
    path = SELECTORS / f"{item_id}.json"
    path.write_text(json.dumps(data, indent=2))

def make_pack(ids):
    zipPath = REPO / "src" / "main" / "resources" / "pack" / "NightmareCoreTexturePack.zip"
    zipPath.parent.mkdir(parents=True, exist_ok=True)
    zipPath.unlink(missing_ok=True)
    with zipfile.ZipFile(zipPath, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.write(ROOT / "pack.mcmeta", "pack.mcmeta")
        for path in sorted(ROOT.rglob("*")):
            if path.is_file() and path.name not in {".DS_Store"} and path.name != "pack.mcmeta":
                rel = path.relative_to(ROOT)
                if rel.parts[0] != "tools":
                    zf.write(path, rel.as_posix())

def make_contact_sheet(ids):
    scale = 4
    cols = 5
    rows = (len(ids) + cols - 1) // cols
    sheet = Image.new("RGBA", (SIZE * scale * cols, SIZE * scale * rows), (255, 255, 255, 255))
    for i, id in enumerate(ids):
        img = Image.open(TEX / f"{id}.png").resize((SIZE * scale, SIZE * scale), Image.NEAREST)
        x = (i % cols) * SIZE * scale
        y = (i // cols) * SIZE * scale
        sheet.paste(img, (x, y), img)
    path = Path('/tmp/NIGHTMARE_CONTACT.png')
    sheet.save(path, "PNG")
    return path

def main():
    ids = make_textures()
    make_models(ids)
    make_item_definitions(ids)
    make_selector(ids, "enchanted_book", "enchanted_book")
    make_selector(["soul_heart"], "heart_of_the_sea", "heart_of_the_sea")
    make_selector(["abyssal_tendrils"], "ender_eye", "ender_eye")
    make_selector(["abyssal_ink"], "ink_sac", "ink_sac")
    make_selector(["abyssal_nucleus"], "nether_star", "nether_star")
    make_pack(ids)
    make_contact_sheet(ids)
    print("✅ Generadas", len(ids), "texturas")

if __name__ == "__main__":
    main()
