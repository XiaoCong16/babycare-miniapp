#!/usr/bin/env bash
set -euo pipefail
APP_HOME=$(cd "$(dirname "$0")/.." && pwd)
. "${APP_HOME}/bin/boot.conf"

if is_running; then
    echo "${APP_NAME} is running (PID $(cat "$PID_FILE"))"
else
    echo "${APP_NAME} is stopped"
    exit 1
fi
