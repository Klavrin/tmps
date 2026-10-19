#!/usr/bin/env bash
# Compiles every .java file in src/ into out/ and starts the app.
# Pass --demo to print the scripted SOLID walkthrough instead.
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out
javac -d out $(find src -name "*.java")
exec java -cp out com.bank.Main "$@"
