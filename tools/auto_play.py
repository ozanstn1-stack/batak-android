import re
import subprocess
import sys
import time

ADB = r"C:\Users\oustu\AppData\Local\Android\Sdk\platform-tools\adb.exe"
SERIAL = "emulator-5554"
UI_LOCAL = r"C:\Users\oustu\AppData\Local\Temp\opencode\ui.xml"
SHOT_DIR = r"D:\AI\projects\batak-android\screenshots"

sys.stdout.reconfigure(encoding="utf-8", errors="replace")


def adb(*args, timeout=30):
    return subprocess.run([ADB, "-s", SERIAL, *args], capture_output=True, text=True, timeout=timeout).stdout


def tap(x, y):
    adb("shell", "input", "tap", str(int(x)), str(int(y)))


def dump_ui():
    adb("shell", "rm", "-f", "/sdcard/ui.xml")
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    adb("pull", "/sdcard/ui.xml", UI_LOCAL)
    return open(UI_LOCAL, encoding="utf-8", errors="ignore").read()


def screenshot(name):
    adb("shell", "screencap", "-p", "/sdcard/shot.png")
    adb("pull", "/sdcard/shot.png", SHOT_DIR + "\\" + name)
    print("[ss]", name, flush=True)


def nodes(ui):
    result = []
    for m in re.finditer(r'text="([^"]*)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', ui):
        text = m.group(1)
        x1, y1, x2, y2 = map(int, m.groups()[1:])
        result.append((text, x1, y1, x2, y2))
    return result


def find_center(ui, label):
    for text, x1, y1, x2, y2 in nodes(ui):
        if text == label:
            return ((x1 + x2) // 2, (y1 + y2) // 2)
    return None


def hand_cards(ui):
    rank = re.compile(r"^(2|3|4|5|6|7|8|9|10|J|Q|K|A)$")
    cards = []
    for text, x1, y1, x2, y2 in nodes(ui):
        if rank.match(text) and 2000 <= y1 <= 2115:
            cards.append(((x1 + x2) // 2, (y1 + y2) // 2))
    cards.sort()
    return cards


def main():
    start = time.time()
    limit = float(sys.argv[1]) if len(sys.argv) > 1 else 900
    plays = 0
    card_index = 0
    shot_round_end = False
    shot_game_over = False

    while time.time() - start < limit:
        ui = dump_ui()

        if "OYUN BİTTİ" in ui:
            if not shot_game_over:
                time.sleep(0.5)
                screenshot("10_game_over.png")
                shot_game_over = True
            print("MATCH FINISHED", flush=True)
            return

        if "EL BİTTİ" in ui:
            if not shot_round_end:
                time.sleep(0.5)
                screenshot("09_round_result.png")
                shot_round_end = True
            center = find_center(ui, "DEVAM") or find_center(ui, "SONUÇLAR")
            if center:
                print("[action] round result -> continue", flush=True)
                tap(*center)
                time.sleep(3)
            continue

        if "İHALEYİ ALDIN" in ui:
            center = find_center(ui, "Maça")
            print("[action] choose trump", flush=True)
            if center:
                tap(*center)
            time.sleep(2)
            continue

        if "İHALE SIRASI SENDE" in ui:
            center = find_center(ui, "PAS")
            print("[action] pass bid", flush=True)
            if center:
                tap(*center)
            time.sleep(2)
            continue

        if "KARTI OYNA" in ui:
            cards = hand_cards(ui)
            if cards:
                idx = card_index % len(cards)
                card_index += 1
                x, y = cards[idx]
                print("[action] play card", idx, "of", len(cards), flush=True)
                tap(x, 2140)
                time.sleep(0.45)
                tap(x, 2140)
                plays += 1
                if plays == 6 and not shot_round_end:
                    time.sleep(1.2)
                    screenshot("08_playing.png")
                time.sleep(1.0)
                continue

        time.sleep(0.8)

    print("TIME LIMIT, plays:", plays, flush=True)


if __name__ == "__main__":
    main()
