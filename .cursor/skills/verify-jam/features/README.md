# Jam verification map

Maintained recipes for proving user-facing Jam behavior. Read this index, then the feature file. Prefer `--stub_backend` isolation; never drive production ArfiesBot without consent.

## Baseline preconditions

- Prepared run dir via `helpers/prepare-run.sh` (disposable under `/tmp/jam-verify/`).
- Liberica JDK 17 on `JAVA_HOME` (until GraalVM 25 builds are green).
- Local Postgres accepting `createdb`/`dropdb` (each run gets `jam_verify_<id>`).
- `helpers/launch.sh` completed with `Jam stub ready`.
- `helpers/doctor.sh` exits 0.
- Fixture SXDL includes `验证券` → `验证通过：果酱假启动` (`fixtures/sxdl/global/verify.ssdl`).

## Driving conventions

- Start from a fresh run dir per proof unless the feature says otherwise.
- Drive chat by POSTing OneBot JSON with `drive-event.sh`, not by calling Scala APIs.
- Treat stub NDJSON `send_*` lines as the observable “bot spoke”.
- After mutations, copy evidence into `.cursor/skills/verify-jam/artifacts/` before cleaning the run dir.

## Proof and skip reporting

- Record feature id, run id, event file, and stub lines that prove the reply.
- If a path needs production NapCat, report blocked + reason; do not substitute a different path as success.

## Features

- [Stub boot and health](./stub-boot.md) — fake-start without OneBot, doctor ports/logs.
- [SSDL keyword reply](./ssdl-keyword-reply.md) — group message triggers SXDL reply via stub send.
- [SXDL compile export](./sxdl-compile.md) — `--compile` writes `ssdl.json` without a long-running bot.
- [CLI help](./cli-help.md) — `--help` prints launcher flags including `--stub_backend`.
