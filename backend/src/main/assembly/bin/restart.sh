#!/usr/bin/env bash
set -euo pipefail
BIN_DIR=$(cd "$(dirname "$0")" && pwd)
"${BIN_DIR}/stop.sh"
"${BIN_DIR}/start.sh" "$@"
