#!/usr/bin/env bash
# Prepare an isolated Jam verification run directory + disposable Postgres DB.
set -euo pipefail

SKILL_DIR="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$SKILL_DIR/../../.." && pwd)"
RUN_ID="${JAM_VERIFY_RUN_ID:-$(date +%Y%m%d-%H%M%S)-$$}"
RUN_ROOT="${JAM_VERIFY_RUN_ROOT:-/tmp/jam-verify}"
RUN_DIR="$RUN_ROOT/$RUN_ID"
POST_PORT="${JAM_VERIFY_POST_PORT:-18700}"
SOCKET_PORT="${JAM_VERIFY_SOCKET_PORT:-18790}"
PG_DB="jam_verify_${RUN_ID//-/_}"
PG_USER="${JAM_VERIFY_PG_USER:-$(whoami)}"
PG_PASSWORD="${JAM_VERIFY_PG_PASSWORD:-}"

# Postgres.app on macOS often isn't on PATH
if ! command -v createdb >/dev/null; then
  for cand in \
    "/Applications/Postgres.app/Contents/Versions/latest/bin" \
    "/Applications/Postgres.app/Contents/Versions/17/bin" \
    "/Applications/Postgres.app/Contents/Versions/16/bin"
  do
    if [[ -x "$cand/createdb" ]]; then
      PATH="$cand:$PATH"
      export PATH
      break
    fi
  done
fi

if ! command -v createdb >/dev/null; then
  echo "createdb not found; Postgres client tools required for verify runs" >&2
  exit 1
fi
if ! pg_isready -h localhost >/dev/null 2>&1; then
  echo "Postgres on localhost is not ready (pg_isready failed)" >&2
  exit 1
fi

mkdir -p "$RUN_DIR"/{tmp,db,plugins,backend-unused,sxdl/template}
cp -R "$SKILL_DIR/fixtures/sxdl/." "$RUN_DIR/sxdl/"
ABS_RUN="$(cd "$RUN_DIR" && pwd)"

createdb "$PG_DB"

sed -e "s|__RUN_DIR__|$ABS_RUN|g" \
    -e "s|__POST_PORT__|$POST_PORT|g" \
    -e "s|__SOCKET_PORT__|$SOCKET_PORT|g" \
    -e "s|__PG_DB__|$PG_DB|g" \
    -e "s|__PG_USER__|$PG_USER|g" \
    -e "s|__PG_PASSWORD__|$PG_PASSWORD|g" \
    "$SKILL_DIR/fixtures/bot.conf.template" > "$RUN_DIR/bot.conf"

python3 - "$ABS_RUN" <<'PY'
import sys
from pathlib import Path
run = Path(sys.argv[1])
cfg = run / "sxdl/config/jam_config.conf"
text = cfg.read_text()
text = text.replace('template_dir = "../conf/sxdl/template"', f'template_dir = "{run}/sxdl/template"')
cfg.write_text(text)
PY

cat > "$RUN_DIR/meta.env" <<EOF
JAM_VERIFY_RUN_ID=$RUN_ID
JAM_VERIFY_RUN_DIR=$ABS_RUN
JAM_VERIFY_POST_PORT=$POST_PORT
JAM_VERIFY_SOCKET_PORT=$SOCKET_PORT
JAM_VERIFY_REPO_ROOT=$REPO_ROOT
JAM_VERIFY_BOT_CONF=$ABS_RUN/bot.conf
JAM_VERIFY_STUB_LOG=$ABS_RUN/tmp/stub-onebot.ndjson
JAM_VERIFY_PID_FILE=$ABS_RUN/jam.pid
JAM_VERIFY_LOG=$ABS_RUN/jam.stdout.log
JAM_VERIFY_PG_DB=$PG_DB
EOF

echo "$ABS_RUN"
echo "Wrote $ABS_RUN/meta.env (pg=$PG_DB)" >&2
