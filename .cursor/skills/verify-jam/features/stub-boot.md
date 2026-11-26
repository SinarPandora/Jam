# Stub boot and health

Jam can fake-start with an embedded Stub OneBot API so agents load SXDL and accept events without NapCat or Mirai.

## Sub-features

- `stub-launch` starts receive + stub post ports from an isolated run dir.
- `stub-doctor` confirms process, ready marker, and ports.
- `stub-login-info` records `get_login_info` during `addAccount`.

## How to get to it (user POV)

- Operator starts Jam with `--stub_backend` and a disposable `--config`.
- Operator checks that the bot reports it is receiving messages.

## Driving it with verify-jam helpers

Preconditions:

- Repo builds with Liberica 17 (`./gradlew :jam_self:installDist`).
- Postgres is up (`pg_isready -h localhost`).
- No other process holds the chosen post/socket ports.

- **Prepare.** Run `helpers/prepare-run.sh`. A run dir and `meta.env` appear under `/tmp/jam-verify/`.
- **Launch.** Run `helpers/launch.sh "$RUN_DIR/meta.env"`. Stdout contains `Jam stub ready`.
- **Doctor.** Run `helpers/doctor.sh "$RUN_DIR/meta.env"`. Exit code `0`; output includes socket/post listening and `get_login_info`.
- **Proof.** Copy `jam.stdout.log` and `tmp/stub-onebot.ndjson` to `artifacts/stub-boot/<run-id>/`. Stub log contains `"path":"get_login_info"`.

## Gotchas

- Default `../conf/bot.conf` only works from distribution `bin/`; always pass absolute `--config=` / `-Dconfig.file=` for verify.
- Gradle `run` wraps Java awkwardly; helpers use `installDist` + direct `java -cp`.
- Production ArfiesBot paths are rejected by doctor.
