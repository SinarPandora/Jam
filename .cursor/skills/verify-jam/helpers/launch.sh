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

JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

if [[ -f "$JAM_VERIFY_PID_FILE" ]] && kill -0 "$(cat "$JAM_VERIFY_PID_FILE")" 2>/dev/null; then
  echo "Jam already running pid=$(cat "$JAM_VERIFY_PID_FILE") for $JAM_VERIFY_RUN_DIR" >&2
  exit 1
fi

cd "$JAM_VERIFY_REPO_ROOT"

# Build distribution via sbt (project pins sbt 2.x). Prefer sbtn thin client when available.
if command -v sbtn >/dev/null 2>&1; then
  sbtn jam_self/installDist
else
  sbt --batch -Dsbt.supershell=false jam_self/installDist
fi
INSTALL_DIR="$JAM_VERIFY_REPO_ROOT/jam_self/target/install/jam_self"
if [[ ! -d "$INSTALL_DIR/lib" ]]; then
  echo "installDist missing lib at $INSTALL_DIR" >&2
  exit 1
fi

# Collect jars for classpath (native-packager installDist layout + plugins/)
CP="$(find "$INSTALL_DIR/lib" -name '*.jar' | tr '\n' ':')"
CP="${CP%:}"
if [[ -d "$INSTALL_DIR/plugins" ]]; then
  PLUGIN_CP="$(find "$INSTALL_DIR/plugins" -name '*.jar' | tr '\n' ':')"
  PLUGIN_CP="${PLUGIN_CP%:}"
  if [[ -n "$PLUGIN_CP" ]]; then
    CP="$CP:$PLUGIN_CP"
  fi
fi

# 脱离 agent/工具进程组：优先 setsid（Linux）；macOS 无 setsid 时退回 nohup
_java_cmd=(env -u ELECTRON_RUN_AS_NODE "$JAVA_HOME/bin/java"
  -Dconfig.file="$JAM_VERIFY_BOT_CONF"
  -Dfile.encoding=UTF-8
  -Xmx512m
  -cp "$CP"
  o.lartifa.jam.Bootloader
  --config="$JAM_VERIFY_BOT_CONF"
  --stub_backend)
if command -v setsid >/dev/null 2>&1; then
  setsid "${_java_cmd[@]}" >"$JAM_VERIFY_LOG" 2>&1 < /dev/null &
else
  nohup "${_java_cmd[@]}" >"$JAM_VERIFY_LOG" 2>&1 < /dev/null &
fi
echo $! >"$JAM_VERIFY_PID_FILE"

ready=0
for _ in $(seq 1 120); do
  if grep -q "开始接收消息" "$JAM_VERIFY_LOG" 2>/dev/null; then
    ready=1
    break
  fi
  if grep -qE "FlywaySqlScriptException|Migration .* failed|Exception in thread" "$JAM_VERIFY_LOG" 2>/dev/null \
    && ! grep -q "开始接收消息" "$JAM_VERIFY_LOG" 2>/dev/null; then
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
