#!/usr/bin/env bash
# Checks the built APK: signature, zip alignment, and manifest summary.
set -euo pipefail
unset JAVA_TOOL_OPTIONS

HERE="$(cd "$(dirname "$0")" && pwd)"
TOOLS="${ANDROID_MINI_TOOLS:-$HOME/atools}"
APK="${1:-$HERE/out/Sober.apk}"

mkdir -p "$HERE/build/verify"
javac -nowarn -cp "$TOOLS/apksig.jar" -d "$HERE/build/verify" "$HERE/tools/Verify.java"
java --add-exports java.base/sun.security.x509=ALL-UNNAMED \
     -cp "$TOOLS/apksig.jar:$HERE/build/verify" Verify "$APK"

python3 - "$APK" <<'PY'
import struct, sys, zipfile
apk = sys.argv[1]
z = zipfile.ZipFile(apk)
bad = []
with open(apk, "rb") as f:
    for i in z.infolist():
        if i.compress_type != zipfile.ZIP_STORED:
            continue
        f.seek(i.header_offset + 26)
        n, e = struct.unpack("<HH", f.read(4))
        off = i.header_offset + 30 + n + e
        if off % (4096 if i.filename.endswith(".so") else 4):
            bad.append(i.filename)
print("alignment  : %s" % ("ok" if not bad else "MISALIGNED %s" % bad))
sys.exit(1 if bad else 0)
PY

"$TOOLS/aapt2" dump badging "$APK" | head -4
