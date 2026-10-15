#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT=$(cd "$(dirname "$0")/../../../.." && pwd)
TEST_ROOT=$(mktemp -d)
APP_HOME="${TEST_ROOT}/babycare-server"
OUTPUT_FILE="${TEST_ROOT}/start-output.log"
FAILURE_OUTPUT_FILE="${TEST_ROOT}/start-failure-output.log"
LOG_DIR="${TEST_ROOT}/xcAppLogs/babycare-server-logs"
LOG_FILE="${LOG_DIR}/info.log"

cleanup() {
    if [ -f "${APP_HOME}/bin/babycare-server.pid" ]; then
        pid=$(cat "${APP_HOME}/bin/babycare-server.pid")
        kill "$pid" 2>/dev/null || true
    fi
    rm -rf "$TEST_ROOT"
}
trap cleanup EXIT

mkdir -p "${APP_HOME}/bin" "${APP_HOME}/boot" "${APP_HOME}/config" \
    "${APP_HOME}/lib" "${TEST_ROOT}/fake-bin"
cp "${REPO_ROOT}/backend/src/main/assembly/bin/start.sh" "${APP_HOME}/bin/start.sh"
cp "${REPO_ROOT}/backend/src/main/assembly/bin/boot.conf" "${APP_HOME}/bin/boot.conf"
cp "${REPO_ROOT}/backend/src/main/assembly/bin/boot.config" "${APP_HOME}/bin/boot.config"
touch "${APP_HOME}/boot/babycare-server.jar"

cat > "${TEST_ROOT}/fake-java.sh" <<'EOF'
#!/usr/bin/env bash
printf 'fake java args:'
printf ' <%s>' "$@"
printf '\nmock startup line\n'
if [ "${FAKE_JAVA_MODE:-success}" = failure ]; then
    printf 'mock startup failure\n'
    exit 3
fi
sleep 1
EOF
chmod +x "${TEST_ROOT}/fake-java.sh"

cat > "${TEST_ROOT}/fake-bin/curl" <<'EOF'
#!/usr/bin/env bash
if [ "${FAKE_JAVA_MODE:-success}" = failure ]; then
    exit 1
fi
exit 0
EOF
chmod +x "${TEST_ROOT}/fake-bin/curl"

JAVA_CMD="${TEST_ROOT}/fake-java.sh" START_TIMEOUT=2 \
    PATH="${TEST_ROOT}/fake-bin:${PATH}" \
    "${APP_HOME}/bin/start.sh" -l y > "$OUTPUT_FILE" 2>&1

grep -F "Starting babycare-server with command:" "$OUTPUT_FILE"
grep -F "mock startup line" "$OUTPUT_FILE"
grep -F -- "-cp" "$OUTPUT_FILE"
test -f "$LOG_FILE"
grep -F "mock startup line" "$LOG_FILE"
grep -F -- "-Dnacos.logging.default.config.enabled=false" "$OUTPUT_FILE"
if grep -F -- "-DJM.LOG.PATH" "$OUTPUT_FILE"; then
    echo "Nacos must use the application Log4j2 configuration" >&2
    exit 1
fi
if [ -e "${LOG_DIR}/startup.out" ] || [ -d "${LOG_DIR}/nacos" ]; then
    echo "Only info.log may be used as the active application log" >&2
    exit 1
fi
if grep -F "<-l>" "$OUTPUT_FILE"; then
    echo "-l y must be consumed by start.sh instead of passed to Java" >&2
    exit 1
fi

set +e
FAKE_JAVA_MODE=failure JAVA_CMD="${TEST_ROOT}/fake-java.sh" START_TIMEOUT=2 \
    PATH="${TEST_ROOT}/fake-bin:${PATH}" \
    "${APP_HOME}/bin/start.sh" -l y > "$FAILURE_OUTPUT_FILE" 2>&1
failure_status=$?
set -e

if [ "$failure_status" -eq 0 ]; then
    echo "Expected failed Java startup to return a non-zero status" >&2
    exit 1
fi
grep -F "mock startup failure" "$FAILURE_OUTPUT_FILE"
grep -F "babycare-server failed to start" "$FAILURE_OUTPUT_FILE"

echo "start-script-test passed"
