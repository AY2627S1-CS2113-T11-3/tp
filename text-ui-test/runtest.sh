#!/usr/bin/env sh
# Run from any directory after building with ./gradlew check shadowJar.
set -eu
cd "$(dirname "$0")"
mkdir -p ../build/text-ui-test

java -jar ../build/libs/bob.jar < input.txt > ../build/text-ui-test/ACTUAL.txt

# Normalize Windows line endings without ignoring meaningful spaces or blank lines.
tr -d '\r' < EXPECTED.txt > ../build/text-ui-test/EXPECTED-UNIX.txt
tr -d '\r' < ../build/text-ui-test/ACTUAL.txt > ../build/text-ui-test/ACTUAL-UNIX.txt
diff -u ../build/text-ui-test/EXPECTED-UNIX.txt ../build/text-ui-test/ACTUAL-UNIX.txt
echo "Console regression test passed."
