# Sakuwise `.sakuwise` Backup Format

> Last updated: v1.0.4. Source of truth: `core/crypto/BackupCrypto.kt`,
> `core/crypto/BackupPayload.kt`, `core/crypto/BackupService.kt`.

A `.sakuwise` file is a self-contained, zero-knowledge encrypted backup. Everything
needed to read the data lives behind the user's backup PIN/passphrase — no server,
no key escrow.

## Two layers of versioning

### 1. File envelope (BackupCrypto)
The on-disk file is AES-256-GCM ciphertext with a small clear header:

```
offset  size  field
0       4     Magic "SKWS"
4       1     Format version      (FORMAT_VERSION, currently 0x01)
5       1     App schema version  (APP_SCHEMA_VERSION, currently 0x01)
6       2     Reserved 0x00 0x00
8       16    Argon2id salt
24      12    AES-GCM nonce
36      4     Ciphertext length (uint32, big-endian)
40      N     AES-GCM ciphertext (+ 16B GCM tag appended by JCE)
```

The KEK is derived from the PIN via Argon2id (t=3, m=64 MB, p=1, 32-byte output).
On restore, a newer-than-supported `Format version` or `App schema version` is
rejected with a clear message ("update aplikasi dulu").

### 2. Inner payload (BackupPayload) — the part that is unit-tested
Inside the encrypted body sits a versioned payload:

```
v1: [4B ver=1][4B dekLen][DEK][DB]
v2: [4B ver=2][4B dekLen][DEK][4B settingsLen][settings][DB]   <- current
```

- **DEK** — the 32-byte SQLCipher key that encrypted the DB. Bundled so the
  backup is restorable after uninstall/reinstall (where the Keystore-bound DEK
  would otherwise be lost).
- **settings** (v2+) — JSON of finance-affecting prefs (gold prices, plan
  config, allocations, theme, language, …) so a restore doesn't revert them.
- **DB** — the raw SQLCipher database bytes, at whatever **Room schema version**
  the backup was taken at.

`BackupPayload.unpack()` reads **all** historical versions; `pack()` always
writes the current version (v2).

## Why old backups keep working after a schema migration

This is the important interaction with Room migrations (see
`core/database/Migrations.kt`).

The `DB` segment is opaque, schema-versioned bytes. On restore, `BackupService`
writes those bytes straight to `sakuwise.db` and installs the bundled DEK. The
**next time Room opens the database it runs the normal migration path**. So a
backup taken at schema 5 (v1.0.3) and restored into a future app at schema 6+
is brought forward by the exact same tested migrations that protect a live
in-place upgrade.

Consequence: **a DB schema change does NOT require a backup payload-format
change.** You only bump the payload version when the *payload wrapper itself*
changes (e.g. adding a new sibling blob next to settings).

## How to add payload version 3

1. In `BackupPayload`: add `PAYLOAD_VERSION_V3` + its header-length constant.
2. Add a `v3` branch to `pack()` (write new layout) and `unpack()` (read it).
3. **Keep the v1 and v2 read branches** — older backups must still restore.
4. Set `CURRENT_VERSION = PAYLOAD_VERSION_V3`.
5. Add round-trip + backward-read cases in `BackupPayloadVersioningTest`.

## How to add a file-envelope version

Bump `FORMAT_VERSION` (or `APP_SCHEMA_VERSION`) in `BackupCrypto`, and relax the
`require(fileBytes[4] == FORMAT_VERSION)` check into a `when` that dispatches by
version while still accepting older files.

## Test coverage

- `BackupPayloadVersioningTest` (JVM): v2 round-trip (with/without settings),
  v1 legacy read, unknown-version + truncated rejection. This is the
  forward/backward-compat guarantee.
- The full encrypt→decrypt round-trip (Argon2 + AES-GCM) runs on-device only,
  since Argon2Kt needs native libs (see `BackupCryptoTest`).
