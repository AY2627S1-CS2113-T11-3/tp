#!/usr/bin/env sh
# Run from any directory after building with ./gradlew check shadowJar.
set -eu
#@@author DaynaG3
project_root=$(CDPATH= cd "$(dirname "$0")/.." && pwd)
mkdir -p "$project_root/build/text-ui-test"
test_workspace=$(mktemp -d "$project_root/build/text-ui-test/run-XXXXXX")
#@@author

#@@author DaynaG3
(
    cd "$test_workspace"
    java -jar "$project_root/build/libs/bob.jar" < "$project_root/text-ui-test/input.txt" \
        > "$project_root/build/text-ui-test/ACTUAL.txt"
)
#@@author

# Normalize Windows line endings without ignoring meaningful spaces or blank lines.
#@@author DaynaG3
tr -d '\r' < "$project_root/text-ui-test/EXPECTED.txt" > "$project_root/build/text-ui-test/EXPECTED-UNIX.txt"
tr -d '\r' < "$project_root/build/text-ui-test/ACTUAL.txt" > "$project_root/build/text-ui-test/ACTUAL-UNIX.txt"
diff -u "$project_root/build/text-ui-test/EXPECTED-UNIX.txt" "$project_root/build/text-ui-test/ACTUAL-UNIX.txt"
#@@author
echo "Console regression test passed."
