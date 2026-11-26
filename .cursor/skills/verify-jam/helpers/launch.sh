#!/usr/bin/env bash
# Launch Jam in --stub_backend mode for a prepared run dir (meta.env).
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "usage: launch.sh /path/to/run/meta.env" >&2
  exit 2
fi

META="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
# shellcheck disable=SC1090
set -a
source "$META"
set +a

JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

if [[ -f "$JAM_VERIFY_PID_FILE" ]] && kill -0 "$(cat "$JAM_VERIFY_PID_FILE")" 2>/dev/null; then
  echo "Jam already running pid=$(cat "$JAM_VERIFY_PID_FILE") for $JAM_VERIFY_RUN_DIR" >&2
  exit 1
fi

cd "$JAM_VERIFY_REPO_ROOT"
./gradlew :jam_self:installDist --console=plain

INSTALL_DIR="$JAM_VERIFY_REPO_ROOT/jam_self/build/install/jam_self"
# Collect jars for classpath (application plugin layout)
CP="$(find "$INSTALL_DIR/lib" -name '*.jar' | tr '\n' ':')"
CP="${CP%:}"

nohup "$JAVA_HOME/bin/java" \
  -Dconfig.file="$JAM_VERIFY_BOT_CONF" \
  -Dfile.encoding=UTF-8 \
  -Xmx512m \
  -cp "$CP" \
  o.lartifa.jam.Bootloader \
  --config="$JAM_VERIFY_BOT_CONF" \
  --stub_backend \
  >"$JAM_VERIFY_LOG" 2>&1 &
echo $! >"$JAM_VERIFY_PID_FILE"

ready=0
for _ in $(seq 1 120); do
  if grep -q "开始接收消息" "$JAM_VERIFY_LOG" 2>/dev/null; then
    ready=1
    break
  fi
  if grep -qE "FlywaySqlScriptException|Migration .* failed|Exception in thread" "$JAM_VERIFY_LOG" 2>/dev/null \
    && ! grep -q "开始接收消息" "$JAM_VERIFY_LOG" 2>/dev/null; then
    # Give a moment for concurrent log flush, then fail if still not ready
    sleep 1
    if ! grep -q "开始接收消息" "$JAM_VERIFY_LOG" 2>/dev/null; then
      echo "Jam init failed; see $JAM_VERIFY_LOG" >&2
      tail -100 "$JAM_VERIFY_LOG" >&2 || true
      if [[ -f "$JAM_VERIFY_PID_FILE" ]]; then
        kill "$(cat "$JAM_VERIFY_PID_FILE")" 2>/dev/null || true
      fi
      exit 1
    fi
  fi
  if ! kill -0 "$(cat "$JAM_VERIFY_PID_FILE")" 2>/dev/null; then
    echo "Jam process exited early; see $JAM_VERIFY_LOG" >&2
    tail -100 "$JAM_VERIFY_LOG" >&2 || true
    exit 1
  fi
  sleep 1
done

if [[ "$ready" -ne 1 ]]; then
  echo "Timed out waiting for Jam ready; see $JAM_VERIFY_LOG" >&2
  tail -100 "$JAM_VERIFY_LOG" >&2 || true
  exit 1
fi

echo "Jam stub ready pid=$(cat "$JAM_VERIFY_PID_FILE") socket=$JAM_VERIFY_SOCKET_PORT stub_log=$JAM_VERIFY_STUB_LOG"
