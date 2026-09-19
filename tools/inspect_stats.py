import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
path = sys.argv[1] if len(sys.argv) > 1 else r"C:\Users\oustu\AppData\Local\Temp\opencode\prefs.bin"
data = open(path, "rb").read().decode("utf-8", "ignore")
for key in ["games_played", "games_won", "games_lost", "total_tricks", "highest_score",
            "longest_streak", "current_streak", "sound_enabled", "player_name", "rounds_per_game"]:
    i = data.find(key)
    if i < 0:
        print(key, "= (yok)")
        continue
    tail = data[i + len(key):i + len(key) + 24]
    m = re.match(r"[^A-Za-z0-9\"\-]*\"?([A-Za-z0-9_\- ]+)", tail)
    print(key, "=", m.group(1) if m else tail[:16])
