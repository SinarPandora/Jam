# SSDL keyword reply

A group member sends a fixed passphrase; Jam’s SXDL rule replies with a fixed confirmation string. This is the core “CQ/text in → reply out” user path.

## Sub-features

- `ssdl-match-exact` matches full-content keyword `验证券`.
- `ssdl-reply-group` sends the reply to the same group via OneBot `send_group_msg` (stubbed).

## How to get to it (user POV)

- Ensure global SXDL contains the verify rule (fixture ships it).
- In a group chat, send exactly `验证券`.
- Observe the bot reply `验证通过：果酱假启动`.

## Driving it with verify-jam helpers

Preconditions:

- Stub instance healthy (`doctor.sh` ok).
- Fixture `verify.ssdl` loaded (default prepare-run copies it).

- **Post event.** Run `helpers/drive-event.sh "$RUN_DIR/meta.env" fixtures/events/verify-token-group.json` (path relative to skill dir as documented in SKILL.md).
- **Observe stub.** Read `$JAM_VERIFY_STUB_LOG` for a `send_group_msg` whose body message contains `验证通过：果酱假启动`.
- **Proof.** Save the event JSON and stub NDJSON slice under `artifacts/ssdl-keyword-reply/<run-id>/`.

## Gotchas

- `self_id` in the event must equal `bot.jam_qq` (fixture uses `90001`) or Picq cannot resolve `BotAccount`.
- `group_id` must appear in stub `get_group_list` (fixture uses `713201079`); otherwise Picq NPEs and curl sees an empty reply.
- Verify DB is disposable Postgres (`jam_verify_*`); if migrate fails, check `jam.stdout.log`.
- Async listeners may lag a second; poll the stub log briefly before failing.
- Success receive status is HTTP **204**; do not require a response body.
- Picq Gson must use `@Expose`-only deserialization on JDK 17+ (already fixed in `PicqConstants`); otherwise event parse crashes with `InaccessibleObjectException`.
- Do not claim success from stdout alone without a stub `send_group_msg` line.
