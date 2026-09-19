import sys


def read_varint(data, pos):
    result = 0
    shift = 0
    while True:
        b = data[pos]
        pos += 1
        result |= (b & 0x7F) << shift
        if not (b & 0x80):
            return result, pos
        shift += 7


def parse_fields(data, start=0, end=None):
    if end is None:
        end = len(data)
    pos = start
    fields = []
    while pos < end:
        key, pos = read_varint(data, pos)
        field_no = key >> 3
        wire = key & 0x7
        if wire == 0:
            value, pos = read_varint(data, pos)
            fields.append((field_no, wire, value))
        elif wire == 2:
            length, pos = read_varint(data, pos)
            value = data[pos:pos + length]
            pos += length
            fields.append((field_no, wire, value))
        elif wire == 5:
            value = data[pos:pos + 4]
            pos += 4
            fields.append((field_no, wire, value))
        elif wire == 1:
            value = data[pos:pos + 8]
            pos += 8
            fields.append((field_no, wire, value))
        else:
            raise ValueError(f"bilinmeyen wire tipi: {wire}")
    return fields


def main():
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    path = sys.argv[1]
    data = open(path, "rb").read()
    for field_no, wire, value in parse_fields(data):
        if wire != 2:
            continue
        entry = parse_fields(value)
        key = None
        val = None
        for fno, fwire, fval in entry:
            if fno == 1 and fwire == 2:
                key = fval.decode("utf-8", "replace")
            elif fno == 2 and fwire == 2:
                inner = parse_fields(fval)
                for ino, iwire, ival in inner:
                    if ino == 1:
                        val = bool(ival)
                    elif ino == 3 or ino == 4:
                        val = ival
                    elif ino == 5:
                        val = ival.decode("utf-8", "replace")
                    elif ino == 2:
                        val = "float"
        if key is not None:
            shown = val
            if isinstance(shown, str) and len(shown) > 60:
                shown = shown[:60] + "..."
            print(f"{key} = {shown}")


if __name__ == "__main__":
    main()
