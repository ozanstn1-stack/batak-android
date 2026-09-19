import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

path = sys.argv[1] if len(sys.argv) > 1 else r"C:\Users\oustu\AppData\Local\Temp\opencode\ui.xml"
xml = open(path, encoding="utf-8").read()
for m in re.finditer(r'text="([^"]+)"[^>]*bounds="(\[\d+,\d+\]\[\d+,\d+\])"', xml):
    text, bounds = m.group(1), m.group(2)
    if text.strip():
        print(repr(text), bounds)
