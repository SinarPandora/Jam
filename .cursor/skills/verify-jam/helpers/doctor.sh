#!/usr/bin/env bash
# Read-only health check for a Jam stub verification instance.
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "usage: doctor.sh /path/to/run/meta.env" >&2
  exit 2
fi

# shellcheck disable=SC1090
set -a
source "$1"
set +a

ok=1
say() { echo "$*"; }

if [[ ! -f "$JAM_VERIFY_PID_FILE" ]]; then
  say "FAIL: missing pid file $JAM_VERIFY_PID_FILE"
  exit 1
fi
PID="$(cat "$JAM_VERIFY_PID_FILE")"
if ! kill -0 "$PID" 2>/dev/null; then
  say "FAIL: process $PID not running"
  exit 1
fi
say "OK: process $PID alive"

if ! grep -q "开始接收消息" "$JAM_VERIFY_LOG" 2>/dev/null; then
  say "FAIL: ready marker not in $JAM_VERIFY_LOG"
  ok=0
else
  say "OK: ready marker present"
fi

if [[ ! -f "$JAM_VERIFY_STUB_LOG" ]]; then
  say "FAIL: stub log missing $JAM_VERIFY_STUB_LOG (get_login_info should have written it)"
  ok=0
else
  say "OK: stub log exists"
  if ! grep -q '"path":"get_login_info"' "$JAM_VERIFY_STUB_LOG" 2>/dev/null; then
    say "WARN: get_login_info not yet logged (unexpected if boot finished)"
  else
    say "OK: get_login_info observed"
  fi
fi

# Socket port listening
if command -v lsof >/dev/null; then
  if lsof -nP -iTCP:"$JAM_VERIFY_SOCKET_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
    say "OK: socket port $JAM_VERIFY_SOCKET_PORT listening"
  else
    say "FAIL: socket port $JAM_VERIFY_SOCKET_PORT not listening"
    ok=0
  fi
  if lsof -nP -iTCP:"$JAM_VERIFY_POST_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
    say "OK: stub post port $JAM_VERIFY_POST_PORT listening"
  else
    say "FAIL: stub post port $JAM_VERIFY_POST_PORT not listening"
    ok=0
  fi
fi

# Refuse shared production paths
case "$JAM_VERIFY_RUN_DIR" in
  *Production/ArfiesBot*|*ArfiesBot*)
    say "FAIL: run dir looks like production ArfiesBot — refuse to drive"
    exit 1
    ;;
esac

if [[ "$ok" -ne 1 ]]; then
  exit 1
fi
say "doctor: healthy run_id=$JAM_VERIFY_RUN_ID"
