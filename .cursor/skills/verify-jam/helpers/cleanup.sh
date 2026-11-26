#!/usr/bin/env bash
# Tear down the Jam process started by launch.sh. Keeps evidence outside the run dir if copied first.
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "usage: cleanup.sh /path/to/run/meta.env [--remove-run-dir]" >&2
  exit 2
fi

META="$1"
REMOVE_DIR=0
if [[ "${2:-}" == "--remove-run-dir" ]]; then
  REMOVE_DIR=1
fi

# shellcheck disable=SC1090
set -a
source "$META"
set +a

# Postgres.app on macOS often isn't on PATH (needed for dropdb)
if ! command -v dropdb >/dev/null; then
  for cand in \
    "/Applications/Postgres.app/Contents/Versions/latest/bin" \
    "/Applications/Postgres.app/Contents/Versions/17/bin" \
    "/Applications/Postgres.app/Contents/Versions/16/bin"
  do
    if [[ -x "$cand/dropdb" ]]; then
      PATH="$cand:$PATH"
      export PATH
      break
    fi
  done
fi

if [[ -f "$JAM_VERIFY_PID_FILE" ]]; then
  PID="$(cat "$JAM_VERIFY_PID_FILE")"
  if kill -0 "$PID" 2>/dev/null; then
    kill "$PID" 2>/dev/null || true
    for _ in $(seq 1 20); do
      if ! kill -0 "$PID" 2>/dev/null; then
        break
      fi
      sleep 0.5
    done
    if kill -0 "$PID" 2>/dev/null; then
      kill -9 "$PID" 2>/dev/null || true
    fi
    echo "stopped pid $PID"
  else
    echo "pid $PID already gone"
  fi
  rm -f "$JAM_VERIFY_PID_FILE"
else
  echo "no pid file; nothing to kill"
fi

if [[ -n "${JAM_VERIFY_PG_DB:-}" ]]; then
  case "$JAM_VERIFY_PG_DB" in
    jam_verify_*)
      dropdb --if-exists "$JAM_VERIFY_PG_DB" 2>/dev/null || true
      echo "dropped database $JAM_VERIFY_PG_DB"
      ;;
    *)
      echo "refusing to drop unexpected database name: $JAM_VERIFY_PG_DB" >&2
      ;;
  esac
fi

if [[ "$REMOVE_DIR" -eq 1 ]]; then
  # Never delete evidence roots under the skill artifacts/
  case "$JAM_VERIFY_RUN_DIR" in
    /tmp/jam-verify/*)
      rm -rf "$JAM_VERIFY_RUN_DIR"
      echo "removed $JAM_VERIFY_RUN_DIR"
      ;;
    *)
      echo "refusing to remove unexpected run dir: $JAM_VERIFY_RUN_DIR" >&2
      exit 1
      ;;
  esac
fi
