#!/usr/bin/env bash
set -euo pipefail
APP_HOME=$(cd "$(dirname "$0")/.." && pwd)
. "${APP_HOME}/bin/boot.conf"
ENV_FILE="${APP_HOME}/../babycare-server.env"
if [ -f "$ENV_FILE" ]; then
    set -a
    . "$ENV_FILE"
    set +a
fi

if is_running; then
    echo "${APP_NAME} is already running (PID $(cat "$PID_FILE"))"
    exit 0
fi

if [ ! -f "${APP_HOME}/boot/${APP_JAR}" ]; then
    echo "Missing ${APP_HOME}/boot/${APP_JAR}" >&2
    exit 1
fi

mkdir -p "$LOG_DIR"
rm -f "$PID_FILE"
cd "$APP_HOME"
nohup "$JAVA_CMD" -Xms"$JVM_XMS" -Xmx"$JVM_XMX" \
    -DJM.LOG.PATH="$LOG_DIR" \
    -cp "${APP_HOME}/config:${APP_HOME}/boot/${APP_JAR}:${APP_HOME}/lib/*" \
    "$MAIN_CLASS" "$@" > "${LOG_DIR}/startup.out" 2>&1 &
echo $! > "$PID_FILE"
for ((i = 0; i < START_TIMEOUT; i++)); do
    if ! is_running; then
        rm -f "$PID_FILE"
        echo "${APP_NAME} failed to start; see ${LOG_DIR}/startup.out" >&2
        exit 1
    fi
    if curl --silent --output /dev/null --max-time 1 "http://127.0.0.1:${APP_PORT}/"; then
        echo "${APP_NAME} started (PID $(cat "$PID_FILE"))"
        echo "Log: tail -f ${LOG_DIR}/info.log"
        exit 0
    fi
    sleep 1
done

"${APP_HOME}/bin/stop.sh" || true
echo "${APP_NAME} did not open port ${APP_PORT} within ${START_TIMEOUT}s; see ${LOG_DIR}/startup.out" >&2
exit 1
