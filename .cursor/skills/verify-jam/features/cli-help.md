# CLI help

The launcher prints available flags, including fake-start `--stub_backend`.

## Sub-features

- `help-text` prints usage and exits.

## How to get to it (user POV)

- Run `./jam --help` (or Bootloader with `--help`).

## Driving it with verify-jam helpers

Preconditions:

- Classes installed (`installDist`) or runnable via the same java `-cp` as launch.

- **Help.** Run Bootloader with `--help` **before** needing a valid jam_config when possible; current Bootloader handles `--help` immediately after `--config=` property setup. Capture stdout.
- **Proof.** Stdout contains `--stub_backend` and `--compile`. Save under `artifacts/cli-help/<run-id>/help.txt`.

## Gotchas

- If `-Dconfig.file` points at a missing file, other modes fail; `--help` should still run after `setUpJVMParameters` as long as no config object was forced first.
