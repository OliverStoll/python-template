#!/usr/bin/env bash
# Runs the plain-JVM tests over the logic that does not need a device.
set -euo pipefail
unset JAVA_TOOL_OPTIONS

HERE="$(cd "$(dirname "$0")" && pwd)"
TOOLS="${ANDROID_MINI_TOOLS:-$HOME/atools}"
OUT="$HERE/build/test"

# The tests reference R, which only exists after a link pass.
[ -n "$(find "$HERE/build/gen" -name R.java 2>/dev/null)" ] || "$HERE/build.sh" >/dev/null

mkdir -p "$OUT"
javac -nowarn -encoding UTF-8 -cp "$TOOLS/android-34.jar" -d "$OUT" \
    "$HERE"/src/com/oliverstoll/sobriety/*.java \
    $(find "$HERE/build/gen" -name R.java) \
    "$HERE"/test/com/oliverstoll/sobriety/*.java

java -cp "$OUT:$TOOLS/android-34.jar" com.oliverstoll.sobriety.Tests
