#!/usr/bin/env bash
set -euo pipefail
umask 077

DEPLOY_DIR="${DEPLOY_DIR:-/data/babycare-server}"
case "$DEPLOY_DIR" in
    /*) ;;
    *) echo "DEPLOY_DIR must be an absolute path" >&2; exit 1 ;;
esac
APP_BASENAME=$(basename "$DEPLOY_DIR")
if [ "$APP_BASENAME" = "." ] || [ "$APP_BASENAME" = ".." ]; then
    echo "DEPLOY_DIR must name an application directory" >&2
    exit 1
fi
DEPLOY_ROOT=$(cd "$(dirname "$DEPLOY_DIR")" && pwd -P)
if [ "$DEPLOY_ROOT" = "/" ]; then
    echo "DEPLOY_DIR must be inside a writable parent directory" >&2
    exit 1
fi
APP_DIR="$DEPLOY_ROOT/$APP_BASENAME"
ARCHIVE="${ARCHIVE:-$HOME/babycare-server.tar.gz}"
CONFIG_OVERLAY="$DEPLOY_ROOT/babycare-config"
BACKUP_DIR="$DEPLOY_ROOT/babycare-backups"

test -f "$ARCHIVE"
if [ ! -w "$DEPLOY_ROOT" ]; then
    echo "Deployment user needs write permission on $DEPLOY_ROOT" >&2
    exit 1
fi
mkdir -p "$BACKUP_DIR"
STAGE=$(mktemp -d "$DEPLOY_ROOT/.babycare-stage.XXXXXX")
trap 'rm -rf "$STAGE"' EXIT
tar -xzf "$ARCHIVE" -C "$STAGE"
NEXT="$STAGE/babycare-server"
test -x "$NEXT/bin/start.sh"

if [ -d "$CONFIG_OVERLAY" ]; then
    cp -a "$CONFIG_OVERLAY/." "$NEXT/config/"
fi

BACKUP=""
if [ -d "$APP_DIR" ]; then
    "$APP_DIR/bin/stop.sh"
    BACKUP="$BACKUP_DIR/babycare-server-$(date +%Y%m%d%H%M%S)-$$"
    mv "$APP_DIR" "$BACKUP"
fi

if ! mv "$NEXT" "$APP_DIR"; then
    if [ -n "$BACKUP" ]; then
        mv "$BACKUP" "$APP_DIR"
        "$APP_DIR/bin/start.sh" || true
    fi
    exit 1
fi

report_deployment_failure() {
    echo "Latest build remains at $APP_DIR for inspection and restart." >&2
    if [ -n "$BACKUP" ]; then
        echo "Previous build was backed up to $BACKUP." >&2
    fi
}

if ! "$APP_DIR/bin/start.sh"; then
    report_deployment_failure
    exit 1
fi

# Any HTTP response confirms that this Java process has opened its local port.
for ((i = 0; i < 30; i++)); do
    if ! "$APP_DIR/bin/status.sh" >/dev/null; then
        report_deployment_failure
        exit 1
    fi
    if curl --silent --show-error --output /dev/null --max-time 2 \
        http://127.0.0.1:8080/; then
        rm -f "$ARCHIVE"
        echo "babycare-server deployed successfully"
        exit 0
    fi
    sleep 2
done

echo "babycare-server did not open port 8080" >&2
"$APP_DIR/bin/stop.sh" || true
report_deployment_failure
exit 1
