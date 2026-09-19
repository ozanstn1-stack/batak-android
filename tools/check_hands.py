import json
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
data = open(r"C:\Users\oustu\AppData\Local\Temp\opencode\prefs.bin", "rb").read().decode("utf-8", "ignore")


def extract_json(text, start_index):
    depth = 0
    in_string = False
    escaped = False
    for i in range(start_index, len(text)):
        ch = text[i]
        if in_string:
            if escaped:
                escaped = False
            elif ch == "\\":
                escaped = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
        elif ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return text[start_index:i + 1]
    return None


i = data.find('saved_game')
j = data.find('{', i)
raw = extract_json(data, j)
game = json.loads(raw)
trump = game.get("trump")
print("trump:", trump)
for p in range(4):
    hand = game["hands"][p]
    counts = {}
    for card in hand:
        counts[card["suit"]] = counts.get(card["suit"], 0) + 1
    spades = [c["rank"] for c in hand if c["suit"] == trump]
    print(p, "kart:", len(hand), counts, "| kozlar:", spades)
print("trick:", [(t["player"], t["card"]["suit"], t["card"]["rank"]) for t in game.get("trick", [])])
