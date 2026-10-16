"""Build lightweight local UI assets. Only needed when regenerating the theme.

Usage: python scripts/build-ui-assets.py --landscape PATH_TO_GENERATED_PNG
Requires Pillow; it is not a mini-program runtime dependency.
"""
import argparse
from pathlib import Path

from PIL import Image, ImageDraw


def draw_icon(name, color, target):
    # Draw original line icons at 3x resolution, then antialias to native tab size.
    scale = 3
    canvas = Image.new('RGBA', (64 * scale, 64 * scale), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)
    stroke = 3 * scale

    def line(points):
        draw.line([(x * scale, y * scale) for x, y in points], fill=color,
                  width=stroke, joint='curve')

    def circle(bounds):
        draw.ellipse(tuple(v * scale for v in bounds), outline=color, width=stroke)

    if name == 'map':
        line([(9, 25), (32, 12), (55, 25), (9, 25)])
        line([(13, 51), (51, 51)])
        line([(9, 55), (55, 55)])
        for x in (19, 32, 45):
            line([(x, 29), (x, 48)])
        line([(32, 7), (32, 12)])
    elif name == 'plan':
        circle((8, 8, 24, 24))
        circle((40, 40, 56, 56))
        line([(16, 28), (16, 39), (25, 47), (35, 47)])
        line([(40, 17), (49, 17), (49, 28)])
        line([(34, 18), (37, 18)])
    elif name == 'check':
        draw.rounded_rectangle((12 * scale, 15 * scale, 52 * scale, 54 * scale),
                               radius=5 * scale, outline=color, width=stroke)
        line([(12, 27), (52, 27)])
        line([(22, 8), (22, 20)])
        line([(42, 8), (42, 20)])
        line([(23, 40), (29, 46), (41, 34)])
    elif name == 'me':
        circle((22, 7, 42, 27))
        draw.arc((12 * scale, 33 * scale, 52 * scale, 68 * scale), 180, 360,
                 fill=color, width=stroke)
        line([(12, 50), (12, 54), (52, 54), (52, 50)])
    canvas.resize((64, 64), Image.Resampling.LANCZOS).save(target, optimize=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--landscape', type=Path)
    args = parser.parse_args()
    target = Path(__file__).resolve().parents[1] / 'images' / 'ui'
    target.mkdir(parents=True, exist_ok=True)
    if args.landscape:
        with Image.open(args.landscape) as source:
            image = source.convert('RGB')
            image.thumbnail((1200, 800), Image.Resampling.LANCZOS)
            image.save(target / 'fujian-landscape.jpg', quality=85, optimize=True,
                       progressive=True)
    for name in ('map', 'plan', 'check', 'me'):
        draw_icon(name, '#857d70', target / f'{name}.png')
        draw_icon(name, '#596638', target / f'{name}-on.png')
    for path in sorted(target.iterdir()):
        print(f'{path.name}: {path.stat().st_size:,} bytes')


if __name__ == '__main__':
    main()
