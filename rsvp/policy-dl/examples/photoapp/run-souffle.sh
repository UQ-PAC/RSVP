#!/bin/bash

# Wrapper for running souffle

cd "$(dirname "$0")" || exit 1

INTERPRETER=souffle
which $INTERPRETER > /dev/null || { echo "Interpreter $INTERPRETER not found" && exit 1; }

(set -x; mkdir -p build)
(set -x; rm -f build/*)
(set -x; $INTERPRETER -D build auth.dl)

exit $?
