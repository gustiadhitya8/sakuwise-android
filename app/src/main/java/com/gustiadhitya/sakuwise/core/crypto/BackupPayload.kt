package com.gustiadhitya.sakuwise.core.crypto

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Pure (Android-free) codec for the **inner backup payload** — the bytes that
 * sit inside the AES-GCM-encrypted body of a `.sakuwise` file.
 *
 * Extracted from BackupService in v1.0.4 (Item 2 — backup format versioning)
 * so the version-compatibility logic can be unit-tested on the plain JVM
 * without Argon2/AES native libs or an Android Context. BackupService now
 * delegates pack/unpack here.
 *
 * ## Versioned layout (big-endian)
 * v1: `[4B ver=1][4B dekLen][DEK][DB]`
 * v2: `[4B ver=2][4B dekLen][DEK][4B settingsLen][settings][DB]`
 *
 * v2 adds a settings blob (gold prices, plan config, …) so a restore does not
 * silently revert those to defaults. v1 payloads remain restorable — their
 * settings block is treated as absent.
 *
 * ## Forward compatibility with DB migrations (why this matters)
 * The `DB` segment is the raw SQLCipher database at whatever schema version
 * the backup was taken at (e.g. schema 5 from v1.0.3). On restore, those bytes
 * are written to disk as-is; the next time Room opens the DB it runs the
 * normal migration path (see SakuwiseMigrations). So an old backup restored
 * into a future app version is brought forward by the same tested migrations
 * that protect a live upgrade — NOT by anything in this codec. Restoring an
 * old backup never needs a payload-format change just because the DB schema
 * changed.
 *
 * ## How to add payload version 3
 * 1. Add `PAYLOAD_VERSION_V3` and its header length constant.
 * 2. Add a `v3` branch in [pack] (write the new layout) and [unpack] (read it).
 * 3. KEEP the v1 + v2 read branches so older backups still restore.
 * 4. Add round-trip + backward-read cases to BackupPayloadVersioningTest.
 */
object BackupPayload {

    const val PAYLOAD_VERSION_V1 = 1
    const val PAYLOAD_VERSION_V2 = 2

    /** v1 header = 4B version + 4B dekLen. */
    const val V1_HEADER_LEN = 8

    /** v2 header = 4B version + 4B dekLen + 4B settingsLen. */
    const val V2_HEADER_LEN = 12

    /** The version this app writes today. */
    const val CURRENT_VERSION = PAYLOAD_VERSION_V2

    /** Decoded payload. [settings] is empty for v1 backups. */
    data class Decoded(val dek: ByteArray, val settings: ByteArray, val db: ByteArray)

    /** Pack the current (v2) payload layout. */
    fun pack(dek: ByteArray, settings: ByteArray, db: ByteArray): ByteArray {
        require(dek.size in 16..64) { "DEK length out of range: ${dek.size}" }
        val totalLen = V2_HEADER_LEN + dek.size + settings.size + db.size
        val out = ByteArray(totalLen)
        val buf = ByteBuffer.wrap(out).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(PAYLOAD_VERSION_V2)
        buf.putInt(dek.size)
        buf.put(dek)
        buf.putInt(settings.size)
        if (settings.isNotEmpty()) buf.put(settings)
        buf.put(db)
        return out
    }

    /** Read any supported payload version. Throws on unknown/truncated input. */
    fun unpack(payload: ByteArray): Decoded {
        require(payload.size > V1_HEADER_LEN) { "Payload too small" }
        val buf = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
        return when (val version = buf.int) {
            PAYLOAD_VERSION_V1 -> {
                val dekLen = buf.int
                require(dekLen in 16..64) { "Invalid DEK length: $dekLen" }
                require(payload.size > V1_HEADER_LEN + dekLen) { "v1 payload truncated" }
                val dek = ByteArray(dekLen).also { buf.get(it) }
                val dbLen = payload.size - V1_HEADER_LEN - dekLen
                val db = ByteArray(dbLen).also { buf.get(it) }
                Decoded(dek, ByteArray(0), db)
            }
            PAYLOAD_VERSION_V2 -> {
                val dekLen = buf.int
                require(dekLen in 16..64) { "Invalid DEK length: $dekLen" }
                require(payload.size >= V2_HEADER_LEN + dekLen) { "v2 payload truncated" }
                val dek = ByteArray(dekLen).also { buf.get(it) }
                val settingsLen = buf.int
                // Bytes left for settings + DB after the header and DEK. Compared
                // by SUBTRACTION (never addition) so a maliciously huge settingsLen
                // can't integer-overflow the bound. settingsLen must leave at least
                // one DB byte, so the comparison is strict (<). This yields a clean
                // "truncated" error instead of a raw BufferUnderflowException.
                val remainingForSettingsAndDb = payload.size - V2_HEADER_LEN - dekLen
                require(settingsLen in 0 until remainingForSettingsAndDb) { "v2 payload truncated" }
                val settings = if (settingsLen > 0) ByteArray(settingsLen).also { buf.get(it) } else ByteArray(0)
                val dbLen = remainingForSettingsAndDb - settingsLen
                val db = ByteArray(dbLen).also { buf.get(it) }
                Decoded(dek, settings, db)
            }
            else -> error("Backup payload version $version not supported")
        }
    }

    /**
     * Build a v1-layout payload. Production never writes v1 (we're on v2), but
     * tests use this to prove old backups still restore. Kept here so the v1
     * byte layout lives next to its reader.
     */
    fun packV1ForTest(dek: ByteArray, db: ByteArray): ByteArray {
        require(dek.size in 16..64) { "DEK length out of range: ${dek.size}" }
        val out = ByteArray(V1_HEADER_LEN + dek.size + db.size)
        val buf = ByteBuffer.wrap(out).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(PAYLOAD_VERSION_V1)
        buf.putInt(dek.size)
        buf.put(dek)
        buf.put(db)
        return out
    }
}
