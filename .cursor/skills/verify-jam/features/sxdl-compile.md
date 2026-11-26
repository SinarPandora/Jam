# SXDL compile export

Operators can compile SXDL without connecting a protocol client; Jam writes `ssdl.json` under the SXDL root and exits.

## Sub-features

- `compile-run` executes `--compile` with an isolated config.
- `compile-artifact` produces `ssdl.json` describing loaded steps/matchers.

## How to get to it (user POV)

- Run the Jam launcher with `--compile` (and a config pointing at the SXDL tree).
- Inspect the generated `ssdl.json` next to the scripts.

## Driving it with verify-jam helpers

Preconditions:

- `prepare-run.sh` completed (provides `bot.conf` + fixture SXDL).
- No need to launch the long-running stub for this feature.

- **Compile.** From repo root, after `installDist`, run Bootloader with `--config="$RUN_DIR/bot.conf" --compile` and `-Dconfig.file` set the same (see SKILL.md).
- **Proof.** Copy `$RUN_DIR/sxdl/ssdl.json` to `artifacts/sxdl-compile/<run-id>/`. File is non-empty and references the verify step reply text or step id `1`.

## Gotchas

- `--compile` calls `sys.exit(0)` on success; treat exit `0` plus `ssdl.json` as success.
- Still requires valid `jam_config_file` and SXDL paths in `bot.conf`.
