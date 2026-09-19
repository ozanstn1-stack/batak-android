import sys

path = sys.argv[1] if len(sys.argv) > 1 else r"C:\Users\oustu\AppData\Local\Temp\opencode\prefs.bin"
data = open(path, "rb").read().decode("utf-8", "ignore")
for key in ["phase", "bids", "bidTurn", "highestBid", "highestBidder", "trump", "dealId", "redealCount", "currentPlayer", "tricksWon", "roundResult"]:
    marker = '"' + key + '"'
    i = data.find(marker)
    if i >= 0:
        print(data[i:i + 260].split('}')[0])
        print("---")
    else:
        print(key, ": bulunamadi")
        print("---")
