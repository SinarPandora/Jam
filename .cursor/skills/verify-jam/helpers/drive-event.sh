#!/usr/bin/env bash
# POST a OneBot event JSON file to Jam's receive (socket) port.
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "usage: drive-event.sh /path/to/run/meta.env /path/to/event.json" >&2
  exit 2
fi

# shellcheck disable=SC1090
set -a
source "$1"
set +a
EVENT_JSON="$2"

# OneBot inbound responds 204 No Content on success; curl -f would treat empty body oddly — check status explicitly.
code="$(curl -sS -o /dev/null -w '%{http_code}' -X POST \
  -H 'Content-Type: application/json; charset=utf-8' \
  --data-binary @"$EVENT_JSON" \
  "http://127.0.0.1:${JAM_VERIFY_SOCKET_PORT}/")"
echo "posted $(basename "$EVENT_JSON") -> socket $JAM_VERIFY_SOCKET_PORT http=$code"
if [[ "$code" != "204" && "$code" != "200" ]]; then
  echo "unexpected HTTP status from OneBot receive port: $code" >&2
  exit 1
fi
