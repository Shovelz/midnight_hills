from PIL import Image
import os

# Configuration
input_dir = "/home/crow/code/midnight_hills/assets/map/tileset_padding/input"
output_dir = "/home/crow/code/midnight_hills/assets/map/room_tilesets"

tile_size = 16
padding = 2
valid_exts = (".png", ".jpg", ".jpeg")

os.makedirs(output_dir, exist_ok=True)

for filename in os.listdir(input_dir):
    if not filename.lower().endswith(valid_exts):
        continue

    input_file = os.path.join(input_dir, filename)
    output_file = os.path.join(output_dir, filename)

    img = Image.open(input_file).convert("RGBA")
    img_width, img_height = img.size

    tiles_x = img_width // tile_size
    tiles_y = img_height // tile_size

    new_tile_size = tile_size + 2 * padding
    new_img = Image.new(
        "RGBA",
        (tiles_x * new_tile_size, tiles_y * new_tile_size)
    )

    for ty in range(tiles_y):
        for tx in range(tiles_x):
            tile = img.crop((
                tx * tile_size,
                ty * tile_size,
                (tx + 1) * tile_size,
                (ty + 1) * tile_size
            ))

            padded_tile = Image.new("RGBA", (new_tile_size, new_tile_size))

            # Center
            padded_tile.paste(tile, (padding, padding))

            # Top / bottom
            top_strip = tile.crop((0, 0, tile_size, 1)).resize((tile_size, padding))
            bottom_strip = tile.crop((0, tile_size - 1, tile_size, tile_size)).resize((tile_size, padding))
            padded_tile.paste(top_strip, (padding, 0))
            padded_tile.paste(bottom_strip, (padding, padding + tile_size))

            # Left / right
            left_strip = tile.crop((0, 0, 1, tile_size)).resize((padding, tile_size))
            right_strip = tile.crop((tile_size - 1, 0, tile_size, tile_size)).resize((padding, tile_size))
            padded_tile.paste(left_strip, (0, padding))
            padded_tile.paste(right_strip, (padding + tile_size, padding))

            # Corners
            tl = Image.new("RGBA", (padding, padding), tile.getpixel((0, 0)))
            tr = Image.new("RGBA", (padding, padding), tile.getpixel((tile_size - 1, 0)))
            bl = Image.new("RGBA", (padding, padding), tile.getpixel((0, tile_size - 1)))
            br = Image.new("RGBA", (padding, padding), tile.getpixel((tile_size - 1, tile_size - 1)))

            padded_tile.paste(tl, (0, 0))
            padded_tile.paste(tr, (padding + tile_size, 0))
            padded_tile.paste(bl, (0, padding + tile_size))
            padded_tile.paste(br, (padding + tile_size, padding + tile_size))

            new_img.paste(
                padded_tile,
                (tx * new_tile_size, ty * new_tile_size)
            )

    new_img.save(output_file)
    print(f"Padded tileset saved: {output_file}")
