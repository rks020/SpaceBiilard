from PIL import Image
import os
import sys

def remove_black_background(image_path):
    try:
        img = Image.open(image_path).convert("RGBA")
        datas = img.getdata()

        new_data = []
        for item in datas:
            # Check if pixel is dark (black)
            # Threshold: R+G+B < 30 (Very dark)
            if item[0] < 30 and item[1] < 30 and item[2] < 30:
                # Make it transparent
                new_data.append((0, 0, 0, 0))
            else:
                new_data.append(item)

        img.putdata(new_data)
        img.save(image_path, "PNG")
        print(f"Processed: {image_path}")
    except Exception as e:
        print(f"Error processing {image_path}: {e}")

files = [
    "ic_upgrade_aim.png",
    "ic_upgrade_energy.png",
    "ic_upgrade_luck.png",
    "ic_upgrade_shield.png"
]

base_path = r"c:\Users\raufk\OneDrive\Desktop\SpaceBillardOnline\app\src\main\res\drawable"

for f in files:
    full_path = os.path.join(base_path, f)
    if os.path.exists(full_path):
        remove_black_background(full_path)
    else:
        print(f"File not found: {full_path}")
