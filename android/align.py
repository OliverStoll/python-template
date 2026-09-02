#!/usr/bin/env python3
"""zipalign, in Python.

Rewrites an APK so every STORED entry begins on a 4-byte boundary (native
libraries on a 4096-byte one), by padding each local header's extra field.
Must run before signing: the signer inserts its block ahead of the central
directory and leaves entry offsets alone.
"""
import struct
import sys
import zipfile

ALIGN = 4
PAGE_ALIGN = 4096


def align_for(name):
    return PAGE_ALIGN if name.endswith(".so") else ALIGN


def main(src, dst):
    with zipfile.ZipFile(src, "r") as zin:
        infos = zin.infolist()
        data = {i.filename: zin.read(i.filename) for i in infos}

    out = open(dst, "wb")
    central = []

    for info in infos:
        raw = data[info.filename]
        name = info.filename.encode("utf-8")
        stored = info.compress_type == zipfile.ZIP_STORED

        if stored:
            payload = raw
            comp = zipfile.ZIP_STORED
        else:
            import zlib
            co = zlib.compressobj(9, zlib.DEFLATED, -15)
            payload = co.compress(raw) + co.flush()
            comp = zipfile.ZIP_DEFLATED

        extra = b""
        if stored:
            # Pad the extra field until the payload lands on its boundary.
            header_end = out.tell() + 30 + len(name)
            need = (-header_end) % align_for(info.filename)
            if need:
                # A single padding record; ids 0xD935/0xFFFF are ignored by Android.
                if need < 4:
                    extra = b"\0" * need
                else:
                    extra = struct.pack("<HH", 0xD935, need - 4) + b"\0" * (need - 4)

        offset = out.tell()
        dt = info.date_time
        dos_time = (dt[3] << 11) | (dt[4] << 5) | (dt[5] // 2)
        dos_date = ((dt[0] - 1980) << 9) | (dt[1] << 5) | dt[2]
        crc = info.CRC

        out.write(struct.pack("<IHHHHHIIIHH", 0x04034B50, 20, 0, comp,
                              dos_time, dos_date, crc, len(payload), len(raw),
                              len(name), len(extra)))
        out.write(name)
        out.write(extra)
        out.write(payload)
        central.append((info, offset, comp, len(payload), len(raw), crc,
                        dos_time, dos_date, name, len(extra)))

    cd_start = out.tell()
    for (info, offset, comp, csize, usize, crc, dos_time, dos_date, name,
         extra_len) in central:
        out.write(struct.pack("<IHHHHHHIIIHHHHHII", 0x02014B50, 20, 20, 0, comp,
                              dos_time, dos_date, crc, csize, usize,
                              len(name), 0, 0, 0, 0,
                              info.external_attr, offset))
        out.write(name)
    cd_size = out.tell() - cd_start

    out.write(struct.pack("<IHHHHIIH", 0x06054B50, 0, 0, len(central),
                          len(central), cd_size, cd_start, 0))
    out.close()

    # Verify what we just claimed to do.
    with open(dst, "rb") as f, zipfile.ZipFile(dst) as z:
        for i in z.infolist():
            if i.compress_type != zipfile.ZIP_STORED:
                continue
            f.seek(i.header_offset + 26)
            n, e = struct.unpack("<HH", f.read(4))
            start = i.header_offset + 30 + n + e
            if start % align_for(i.filename):
                raise SystemExit("misaligned: %s at %d" % (i.filename, start))
    print("aligned %d entries -> %s" % (len(central), dst))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
