package com.gustiadhitya.sakuwise.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented migration tests for [SakuwiseDatabase] (Item 1, v1.0.4).
 *
 * Runs against the exported schema JSON in `app/schemas/`, wired into
 * androidTest assets via `sourceSets.androidTest.assets` in build.gradle.kts.
 * Only 5.json exists (schema export was enabled at v5), so historical 1–4
 * cannot be re-validated here — but every device has already migrated to 5,
 * and this guards the v5 baseline plus the next migration.
 *
 * Purpose: prove the migration harness works and the v5 baseline schema is
 * valid, so that when v1.1 bumps the schema we have a tested path that does
 * NOT wipe user data (we removed `fallbackToDestructiveMigration`).
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SakuwiseDatabase::class.java,
    )

    /**
     * Baseline: create the DB at the current shipping version (5) from the
     * exported schema, confirm it has tables, then run the migration set and
     * validate the schema still matches 5. This is the regression guard that
     * 5.json is committed and consistent and that addMigrations(*ALL) opens.
     */
    @Test
    @Throws(IOException::class)
    fun baseline_v5_createsAndValidates() {
        helper.createDatabase(testDb, 5).use { db ->
            db.query(
                "SELECT count(*) FROM sqlite_master WHERE type='table' " +
                    "AND name NOT LIKE 'android_metadata' AND name NOT LIKE 'sqlite_%'",
            ).use { c ->
                assertTrue("schema 5 should have tables", c.moveToFirst() && c.getInt(0) > 0)
            }
        }

        // No migration runs (already at 5); this validates the exported 5.json
        // matches the runtime schema produced by the entities.
        helper.runMigrationsAndValidate(testDb, 5, true, *SakuwiseMigrations.ALL)
    }

    /**
     * A1 (v1.0.5) — upgrade-with-data test for the 4→5 migration.
     *
     * Seeds an account row and a gold row into a v4 schema DB, then runs the
     * full migration chain to v5, and asserts every seeded row survived with
     * correct values. This is the definitive proof that the migration chain
     * does NOT wipe user data.
     *
     * v4→v5 change: adds `kind TEXT NOT NULL DEFAULT 'physical'` to asset_gold.
     * All other seeded rows must be byte-for-byte identical after migration.
     *
     * Synthetic data only — no real user records.
     */
    @Test
    @Throws(IOException::class)
    fun migrate_v4_to_v5_preservesData() {
        helper.createDatabase(testDb, 4).use { db ->
            // Seed a synthetic account
            db.execSQL(
                """INSERT INTO accounts
                   (id, name, type, initialBalance, iconName, colorHex, archived, createdAt)
                   VALUES ('acc-001', 'Dompet Test', 'cash', 500000, 'cash', NULL, 0, 1700000000000)"""
            )
            // Seed a synthetic gold asset (v4 schema: no 'kind' column)
            db.execSQL(
                """INSERT INTO asset_gold
                   (id, purchaseEpochDay, weightMilliGram, serial, buyPrice,
                    note, photoBlob, status, soldEpochDay, soldPrice)
                   VALUES ('gold-001', 19000, 1000, 'SN-TEST', 950000,
                    'Synthetic', NULL, 'held', NULL, NULL)"""
            )
            // Seed a synthetic transaction
            db.execSQL(
                """INSERT INTO transactions
                   (id, dateEpochDay, amount, type, planItemId, sourceAccountId,
                    destAccountId, transferFee, debtId, photoBlob, incomeCategoryId, note, createdAt)
                   VALUES ('txn-001', 19000, 250000, 'expense', NULL, 'acc-001',
                    NULL, NULL, NULL, NULL, NULL, 'Test expense', 1700000000001)"""
            )
        }

        // Run the full migration chain and validate schema at v5
        val db = helper.runMigrationsAndValidate(testDb, 5, true, *SakuwiseMigrations.ALL)

        // Verify the account row survived unchanged
        db.query("SELECT name, initialBalance FROM accounts WHERE id = 'acc-001'").use { c ->
            assertTrue("account row must survive migration", c.moveToFirst())
            assertEquals("Dompet Test", c.getString(0))
            assertEquals(500000L, c.getLong(1))
        }

        // Verify the gold row survived AND gained the default 'kind' value
        db.query("SELECT weightMilliGram, kind FROM asset_gold WHERE id = 'gold-001'").use { c ->
            assertTrue("gold row must survive migration", c.moveToFirst())
            assertEquals(1000L, c.getLong(0))
            assertEquals("physical", c.getString(1)) // default added by MIGRATION_4_5
        }

        // Verify the transaction row survived unchanged
        db.query("SELECT amount, type FROM transactions WHERE id = 'txn-001'").use { c ->
            assertTrue("transaction row must survive migration", c.moveToFirst())
            assertEquals(250000L, c.getLong(0))
            assertEquals("expense", c.getString(1))
        }
    }

    /**
     * Scaffold for the first real schema change (e.g. v1.1 account-detail
     * columns). Enable by:
     *   1. Adding MIGRATION_5_6 to SakuwiseMigrations.ALL,
     *   2. Bumping @Database(version = 6) and committing 6.json,
     *   3. Removing @Ignore and filling in the seed + assertions below.
     */
    @Test
    @Ignore("Enable when the first real Migration(5,6) is added in v1.1")
    @Throws(IOException::class)
    fun migrate_5_to_6_preservesData() {
        helper.createDatabase(testDb, 5).use { db ->
            // db.execSQL("INSERT INTO accounts (...) VALUES (...)")
        }
        // helper.runMigrationsAndValidate(testDb, 6, true, *SakuwiseMigrations.ALL)
        // assert seeded rows survived + new columns defaulted
    }
}
