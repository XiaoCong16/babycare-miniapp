#!/usr/bin/env bash
set -euo pipefail
APP_HOME=$(cd "$(dirname "$0")/.." && pwd)
. "${APP_HOME}/bin/boot.conf"

if ! is_running; then
    rm -f "$PID_FILE"
    echo "${APP_NAME} is stopped"
    exit 0
fi

pid=$(cat "$PID_FILE")
kill "$pid"
for ((i = 0; i < 30; i++)); do
    if ! is_running; then
        rm -f "$PID_FILE"
        echo "${APP_NAME} stopped"
        exit 0
    fi
    sleep 1
done

if is_running; then
    kill -9 "$pid"
fi
rm -f "$PID_FILE"
echo "${APP_NAME} stopped after timeout"
