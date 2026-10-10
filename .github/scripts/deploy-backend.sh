#!/usr/bin/env bash
set -euo pipefail
umask 077

APP_DIR="$HOME/babycare-server"
ARCHIVE="$HOME/babycare-server.tar.gz"
CONFIG_OVERLAY="$HOME/babycare-config"
BACKUP_DIR="$HOME/babycare-backups"

test -f "$ARCHIVE"
mkdir -p "$BACKUP_DIR"
STAGE=$(mktemp -d "$HOME/.babycare-stage.XXXXXX")
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

rollback() {
    "$APP_DIR/bin/stop.sh" || true
    mv "$APP_DIR" "$BACKUP_DIR/failed-$(date +%Y%m%d%H%M%S)-$$"
    if [ -n "$BACKUP" ]; then
        mv "$BACKUP" "$APP_DIR"
        "$APP_DIR/bin/start.sh" || true
    fi
}

if ! "$APP_DIR/bin/start.sh"; then
    rollback
    exit 1
fi

# Any HTTP response confirms that this Java process has opened its local port.
for ((i = 0; i < 30; i++)); do
    if ! "$APP_DIR/bin/status.sh" >/dev/null; then
        rollback
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

echo "babycare-server did not open port 8080; rolling back" >&2
rollback
exit 1
