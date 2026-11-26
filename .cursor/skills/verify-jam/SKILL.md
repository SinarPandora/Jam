---
name: verify-jam
description: "Verify Jam (果酱) QQ bot behavior via stub OneBot fake-start and HTTP event injection — use when proving SXDL replies, CLI --compile/--help, or message handling without a live NapCat/production instance."
---

# Verify Jam

Jam is a Scala QQ bot. Users experience it as chat replies driven by SXDL/CQ-code messages. Production talks to NapCat over OneBot; **verification must not touch** Mac Mini `Development/Production/ArfiesBot` (or any live protocol client) unless the human maintainer explicitly consents in the current chat.

Primary verification surface: **HTTP OneBot event ingest + stub outbound API** (`--stub_backend`). Secondary: CLI `--compile` / `--help`.

## Hard rules

- Never start, stop, or POST into production ArfiesBot / shared NapCat without explicit user consent this turn.
- Never drive an instance you did not launch via these helpers.
- Prefer Liberica JDK 17 for runs until M1 makes GraalVM 25 builds green (`JAVA_HOME` override allowed).
- Local **Postgres** must accept `createdb`/`dropdb` (migrations are PostgreSQL SQL; H2 cannot apply them).

## Launch

From repo root `Jam/`:

```bash
RUN_DIR=$(.cursor/skills/verify-jam/helpers/prepare-run.sh)
# prints absolute run dir; also writes $RUN_DIR/meta.env
.cursor/skills/verify-jam/helpers/launch.sh "$RUN_DIR/meta.env"
```

Ready when stdout says `Jam stub ready` and log contains `开始接收消息`. Stub outbound calls append NDJSON to `$JAM_VERIFY_STUB_LOG` (`tmp/stub-onebot.ndjson` under the run dir).

Teardown:

```bash
.cursor/skills/verify-jam/helpers/cleanup.sh "$RUN_DIR/meta.env"
# optional: also delete disposable run dir
.cursor/skills/verify-jam/helpers/cleanup.sh "$RUN_DIR/meta.env" --remove-run-dir
```

`--stub_backend` starts an embedded fake OneBot HTTP API on `coolQQ.port.post`, then boots Picq receive server on `coolQQ.port.socket` and loads SXDL. No Mirai/NapCat process is required.

## Doctor

```bash
.cursor/skills/verify-jam/helpers/doctor.sh "$RUN_DIR/meta.env"
```

Requires: pid alive, ready marker, stub log present, both ports listening, run dir not under production ArfiesBot.

## Drive

Inject OneBot **v11-style** event JSON (real user path = “someone sent a message”):

```bash
.cursor/skills/verify-jam/helpers/drive-event.sh "$RUN_DIR/meta.env" \
  .cursor/skills/verify-jam/fixtures/events/verify-token-group.json
```

Outbound side effects appear as NDJSON lines in the stub log (`path` = `send_group_msg` / `send_private_msg`, `body.message` = CQ/text).

CLI compile (no long-running server):

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home
./gradlew :jam_self:installDist
java -Dconfig.file="$RUN_DIR/bot.conf" -cp "$(find jam_self/build/install/jam_self/lib -name '*.jar' | tr '\n' ':')" \
  o.lartifa.jam.Bootloader --config="$RUN_DIR/bot.conf" --compile
# proof: $RUN_DIR/sxdl/ssdl.json exists and mentions step content
```

## Evidence

Store proofs under `.cursor/skills/verify-jam/artifacts/<feature>/<run-id>/` (survives cleanup of `/tmp/jam-verify/...`):

- `jam.stdout.log` copy
- `stub-onebot.ndjson` copy (must show the send that proves the reply)
- optional: the event JSON posted
- note `run_id`, socket/post ports, feature id

Proof standards:

- Exercise the user path (SSDL match → reply), not internal setters.
- Capture action (POST event) and resulting stub `send_*` line.
- Do not treat “process up” alone as feature proof.

## Cleanup

Always `cleanup.sh` with the **same** `meta.env` from launch. Kill only that pid. Copy evidence out of the run dir **before** `--remove-run-dir`.

## Helpers

| Script | Role |
|--------|------|
| `helpers/prepare-run.sh` | Isolated run dir + `bot.conf` + fixture SXDL |
| `helpers/launch.sh` | `installDist` + java `--stub_backend` |
| `helpers/doctor.sh` | Read-only health |
| `helpers/drive-event.sh` | POST event JSON to socket port |
| `helpers/cleanup.sh` | Stop pid; optional remove `/tmp/jam-verify/*` run dir |

## Feature map

See [features/README.md](features/README.md).

## Maintenance

When boot flags, ports, SXDL fixture paths, or stub response shapes change, update this skill and the feature map. Use `/maintain-verification-skill` to keep recipes honest.
