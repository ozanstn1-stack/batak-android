import sys
from PIL import Image

src, dst, width = sys.argv[1], sys.argv[2], int(sys.argv[3])
img = Image.open(src)
ratio = width / img.width
resized = img.resize((width, int(img.height * ratio)), Image.LANCZOS)
resized.save(dst, "PNG", optimize=True)
print("ok", dst, resized.size)
