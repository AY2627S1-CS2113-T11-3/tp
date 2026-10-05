#!/usr/bin/env bash

set -e

# Run relative to this script, even when it is launched from another folder.
cd "$(dirname "$0")"

mkdir -p bin
javac -d bin $(find src/main/java -name "*.java")
java -cp bin seedu.duke.Duke
