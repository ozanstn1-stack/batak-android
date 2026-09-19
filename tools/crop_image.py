import sys
from PIL import Image

src, dst, x1, y1, x2, y2, scale = sys.argv[1], sys.argv[2], int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5]), int(sys.argv[6]), int(sys.argv[7])
img = Image.open(src).convert("RGB")
crop = img.crop((x1, y1, x2, y2))
w, h = crop.size
crop = crop.resize((w * scale, h * scale), Image.LANCZOS)
crop.save(dst, "PNG")
print("ok", dst, crop.size)
