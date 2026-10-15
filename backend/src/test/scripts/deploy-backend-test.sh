#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT=$(cd "$(dirname "$0")/../../../.." && pwd)
TEST_ROOT=$(mktemp -d)
DEPLOY_DIR="${TEST_ROOT}/babycare-server"
PACKAGE_ROOT="${TEST_ROOT}/package"
ARCHIVE="${TEST_ROOT}/babycare-server.tar.gz"
OUTPUT_FILE="${TEST_ROOT}/deploy-output.log"

cleanup() {
    rm -rf "$TEST_ROOT"
}
trap cleanup EXIT

mkdir -p "${DEPLOY_DIR}/bin" "${PACKAGE_ROOT}/babycare-server/bin"
printf 'old-build\n' > "${DEPLOY_DIR}/build-id"
cat > "${DEPLOY_DIR}/bin/stop.sh" <<'EOF'
#!/usr/bin/env bash
exit 0
EOF
chmod +x "${DEPLOY_DIR}/bin/stop.sh"

printf 'latest-build\n' > "${PACKAGE_ROOT}/babycare-server/build-id"
cat > "${PACKAGE_ROOT}/babycare-server/bin/start.sh" <<'EOF'
#!/usr/bin/env bash
echo "simulated startup failure" >&2
exit 1
EOF
cat > "${PACKAGE_ROOT}/babycare-server/bin/stop.sh" <<'EOF'
#!/usr/bin/env bash
exit 0
EOF
cat > "${PACKAGE_ROOT}/babycare-server/bin/status.sh" <<'EOF'
#!/usr/bin/env bash
exit 1
EOF
chmod +x "${PACKAGE_ROOT}/babycare-server/bin/"*.sh
tar -czf "$ARCHIVE" -C "$PACKAGE_ROOT" babycare-server

set +e
ARCHIVE="$ARCHIVE" DEPLOY_DIR="$DEPLOY_DIR" \
    bash "${REPO_ROOT}/.github/scripts/deploy-backend.sh" > "$OUTPUT_FILE" 2>&1
deploy_status=$?
set -e

if [ "$deploy_status" -eq 0 ]; then
    echo "Expected simulated startup failure to fail deployment" >&2
    exit 1
fi
grep -Fx "latest-build" "${DEPLOY_DIR}/build-id"
grep -R -F -x "old-build" "${TEST_ROOT}/babycare-backups"
if compgen -G "${TEST_ROOT}/babycare-backups/failed-*" > /dev/null; then
    echo "Latest build must not be moved to a failed-* backup" >&2
    exit 1
fi
grep -F "Latest build remains at" "$OUTPUT_FILE"

echo "deploy-backend-test passed"
