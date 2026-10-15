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

SHOW_LOGS=n
APP_ARGS=()
while [ "$#" -gt 0 ]; do
    case "$1" in
        -l)
            if [ "$#" -lt 2 ]; then
                echo "Usage: $0 [-l y|n] [-- application-arguments]" >&2
                exit 2
            fi
            case "$2" in
                y|Y) SHOW_LOGS=y ;;
                n|N) SHOW_LOGS=n ;;
                *)
                    echo "-l only accepts y or n" >&2
                    exit 2
                    ;;
            esac
            shift 2
            ;;
        --)
            shift
            APP_ARGS+=("$@")
            break
            ;;
        *)
            APP_ARGS+=("$1")
            shift
            ;;
    esac
done

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
LOG_FILE="${LOG_DIR}/info.log"
touch "$LOG_FILE"
LOG_START_LINE=$(( $(wc -l < "$LOG_FILE") + 1 ))
JAVA_COMMAND=(
    "$JAVA_CMD"
    "-Xms${JVM_XMS}"
    "-Xmx${JVM_XMX}"
    "-Dnacos.logging.default.config.enabled=false"
    -cp "${APP_HOME}/config:${APP_HOME}/boot/${APP_JAR}:${APP_HOME}/lib/*"
    "$MAIN_CLASS"
    "${APP_ARGS[@]}"
)

printf 'Starting %s with command:\n  nohup' "$APP_NAME"
printf ' %q' "${JAVA_COMMAND[@]}"
printf ' >> %q 2>&1 &\n' "$LOG_FILE"

nohup "${JAVA_COMMAND[@]}" >> "$LOG_FILE" 2>&1 &
APP_PID=$!
echo "$APP_PID" > "$PID_FILE"

LOG_TAIL_PID=""
if [ "$SHOW_LOGS" = y ]; then
    echo "Following application log: ${LOG_FILE} (Ctrl+C to stop following)"
    tail --pid="$APP_PID" -n +"$LOG_START_LINE" -F "$LOG_FILE" &
    LOG_TAIL_PID=$!
    trap 'kill "$LOG_TAIL_PID" 2>/dev/null || true; exit 130' INT TERM
fi

for ((i = 0; i < START_TIMEOUT; i++)); do
    if ! is_running; then
        rm -f "$PID_FILE"
        if [ -n "$LOG_TAIL_PID" ]; then
            wait "$LOG_TAIL_PID" || true
        fi
        echo "${APP_NAME} failed to start; see ${LOG_FILE}" >&2
        exit 1
    fi
    if curl --silent --output /dev/null --max-time 1 "http://127.0.0.1:${APP_PORT}/"; then
        echo "${APP_NAME} started (PID $(cat "$PID_FILE"))"
        echo "Log: tail -f ${LOG_DIR}/info.log"
        if [ -n "$LOG_TAIL_PID" ]; then
            set +e
            wait "$APP_PID"
            app_status=$?
            wait "$LOG_TAIL_PID"
            set -e
            rm -f "$PID_FILE"
            exit "$app_status"
        fi
        exit 0
    fi
    sleep 1
done

"${APP_HOME}/bin/stop.sh" || true
if [ -n "$LOG_TAIL_PID" ]; then
    wait "$LOG_TAIL_PID" || true
fi
echo "${APP_NAME} did not open port ${APP_PORT} within ${START_TIMEOUT}s; see ${LOG_FILE}" >&2
exit 1
