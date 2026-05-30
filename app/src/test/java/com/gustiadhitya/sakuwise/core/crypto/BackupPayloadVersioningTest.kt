package com.gustiadhitya.sakuwise.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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

        assertArrayEquals(dek, decoded.dek)
        assertArrayEquals(settings, decoded.settings)
        assertArrayEquals(db, decoded.db)
    }

    @Test
    fun v2_roundTrips_withEmptySettings() {
        val packed = BackupPayload.pack(dek, ByteArray(0), db)

        val decoded = BackupPayload.unpack(packed)

        assertArrayEquals(dek, decoded.dek)
        assertEquals(0, decoded.settings.size)
        assertArrayEquals(db, decoded.db)
    }

    /** The critical backward-compat case: an old (v1) backup must still read. */
    @Test
    fun v1_legacyPayload_stillReads() {
        val legacy = BackupPayload.packV1ForTest(dek, db)

        val decoded = BackupPayload.unpack(legacy)

        assertArrayEquals(dek, decoded.dek)
        assertArrayEquals(db, decoded.db)
        // v1 had no settings block → restored as defaults (empty).
        assertEquals(0, decoded.settings.size)
    }

    @Test
    fun currentVersion_isV2() {
        assertEquals(BackupPayload.PAYLOAD_VERSION_V2, BackupPayload.CURRENT_VERSION)
    }

    @Test
    fun unknownVersion_throws() {
        // version=99, dekLen=32, then the DEK bytes
        val buf = java.nio.ByteBuffer.allocate(40).order(java.nio.ByteOrder.BIG_ENDIAN)
        buf.putInt(99); buf.putInt(32); buf.put(dek)
        assertThrows(IllegalStateException::class.java) {
            BackupPayload.unpack(buf.array())
        }
    }

    @Test
    fun truncatedPayload_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupPayload.unpack(bytes(0x00, 0x00))
        }
    }
}
