package com.gustiadhitya.sakuwise.core.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Item 2 (v1.0.4) — backup format versioning.
 *
 * Proves the inner payload codec is forward/backward compatible: the current
 * app reads BOTH the legacy v1 layout and the current v2 layout. This is the
 * guarantee that a backup made by an older app still restores after upgrade.
 *
 * Runs on the plain JVM (pure ByteBuffer logic, no Argon2/AES/Android).
 */
class BackupPayloadVersioningTest {

    private fun bytes(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }
    private val dek = ByteArray(32) { it.toByte() }            // 256-bit DEK
    private val db = bytes(0xDB, 0x00, 0x01, 0x02, 0x03, 0x04) // stand-in SQLCipher bytes

    @Test
    fun v2_roundTrips_withSettings() {
        val settings = "{\"goldPriceGlobal\":1000000}".toByteArray()
        val packed = BackupPayload.pack(dek, settings, db)

        val decoded = BackupPayload.unpack(packed)

        assertThat(decoded.dek).isEqualTo(dek)
        assertThat(decoded.settings).isEqualTo(settings)
        assertThat(decoded.db).isEqualTo(db)
    }

    @Test
    fun v2_roundTrips_withEmptySettings() {
        val packed = BackupPayload.pack(dek, ByteArray(0), db)

        val decoded = BackupPayload.unpack(packed)

        assertThat(decoded.dek).isEqualTo(dek)
        assertThat(decoded.settings).isEmpty()
        assertThat(decoded.db).isEqualTo(db)
    }

    /** The critical backward-compat case: an old (v1) backup must still read. */
    @Test
    fun v1_legacyPayload_stillReads() {
        val legacy = BackupPayload.packV1ForTest(dek, db)

        val decoded = BackupPayload.unpack(legacy)

        assertThat(decoded.dek).isEqualTo(dek)
        assertThat(decoded.db).isEqualTo(db)
        // v1 had no settings block → restored as defaults (empty).
        assertThat(decoded.settings).isEmpty()
    }

    @Test
    fun currentVersion_isV2() {
        assertThat(BackupPayload.CURRENT_VERSION).isEqualTo(BackupPayload.PAYLOAD_VERSION_V2)
    }

    @Test(expected = IllegalStateException::class)
    fun unknownVersion_throws() {
        // version=99, dekLen=32, then junk
        val buf = java.nio.ByteBuffer.allocate(64).order(java.nio.ByteOrder.BIG_ENDIAN)
        buf.putInt(99); buf.putInt(32); buf.put(dek)
        BackupPayload.unpack(buf.array())
    }

    @Test(expected = IllegalArgumentException::class)
    fun truncatedPayload_throws() {
        BackupPayload.unpack(bytes(0x00, 0x00))
    }
}
