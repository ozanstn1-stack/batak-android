import math
import os
import random
import struct
import wave

SR = 44100
OUT_DIR = r"D:\AI\projects\batak-android\app\src\main\res\raw"


def envelope(i, n, attack=0.004, decay=1.0):
    t = i / max(1, n)
    a = min(1.0, t / attack) if attack > 0 else 1.0
    d = math.exp(-decay * t * 4.0)
    return a * d


def tone(freq, dur, vol=0.5, decay=1.0, harmonics=(1.0, 0.28, 0.12, 0.05)):
    n = int(SR * dur)
    out = []
    for i in range(n):
        t = i / SR
        s = 0.0
        for k, amp in enumerate(harmonics, start=1):
            s += amp * math.sin(2.0 * math.pi * freq * k * t)
        out.append(s * vol * envelope(i, n, 0.003, decay))
    return out


def noise(dur, vol=0.4, decay=2.5, seed=7, lowpass=0.5):
    rnd = random.Random(seed)
    n = int(SR * dur)
    out = []
    prev = 0.0
    for i in range(n):
        raw = rnd.uniform(-1.0, 1.0)
        prev = prev * (1.0 - lowpass) + raw * lowpass
        out.append(prev * vol * envelope(i, n, 0.0015, decay))
    return out


def mix(*tracks):
    length = max(len(t) for t in tracks)
    out = [0.0] * length
    for track in tracks:
        for i, v in enumerate(track):
            out[i] += v
    return out


def concat(*tracks):
    out = []
    for track in tracks:
        out.extend(track)
    return out


def silence(dur):
    return [0.0] * int(SR * dur)


def normalize(samples, peak=0.82):
    m = max(1e-6, max(abs(s) for s in samples))
    k = peak / m
    return [s * k for s in samples]


def write_wav(name, samples):
    path = os.path.join(OUT_DIR, name)
    frames = bytearray()
    for s in samples:
        v = int(max(-1.0, min(1.0, s)) * 32767)
        frames += struct.pack('<h', v)
    with wave.open(path, 'wb') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(bytes(frames))
    print("yazildi:", path, len(samples) / SR, "sn")


def main():
    os.makedirs(OUT_DIR, exist_ok=True)

    # Kart dagitma: kisa hisirti + yumusak tokus
    deal = mix(
        noise(0.10, vol=0.55, decay=3.2, seed=11, lowpass=0.65),
        tone(180, 0.07, vol=0.18, decay=3.5, harmonics=(1.0, 0.15))
    )
    write_wav("card_deal.wav", normalize(deal, 0.72))

    # Kart oynama: net kisa tik
    play = mix(
        noise(0.014, vol=0.6, decay=1.6, seed=23, lowpass=0.85),
        tone(880, 0.055, vol=0.30, decay=2.6)
    )
    write_wav("card_play.wav", normalize(play, 0.75))

    # Buton: yumusak tik
    button = tone(1320, 0.035, vol=0.5, decay=3.0, harmonics=(1.0, 0.2))
    write_wav("button_click.wav", normalize(button, 0.62))

    # Ihale: iki tonlu kisa cingi
    bid = concat(tone(660, 0.085, vol=0.42, decay=1.6), tone(990, 0.13, vol=0.42, decay=1.8))
    write_wav("bid.wav", normalize(bid, 0.7))

    # El kazanma: kisa yukselen vurgu
    trick = concat(
        tone(523.25, 0.07, vol=0.4, decay=1.4),
        tone(783.99, 0.13, vol=0.42, decay=1.7)
    )
    write_wav("trick_win.wav", normalize(trick, 0.72))

    # Tur kazanma: arpej
    win = concat(
        tone(523.25, 0.085, vol=0.4, decay=1.2),
        tone(659.25, 0.085, vol=0.4, decay=1.2),
        tone(783.99, 0.085, vol=0.42, decay=1.2),
        tone(1046.50, 0.30, vol=0.46, decay=1.4)
    )
    write_wav("round_win.wav", normalize(win, 0.78))

    # Tur kaybetme: inen uzgun tonlar
    lose = concat(
        tone(392.00, 0.16, vol=0.40, decay=1.6),
        tone(311.13, 0.16, vol=0.40, decay=1.6),
        tone(233.08, 0.34, vol=0.44, decay=1.5)
    )
    write_wav("round_lose.wav", normalize(lose, 0.72))


if __name__ == "__main__":
    main()
