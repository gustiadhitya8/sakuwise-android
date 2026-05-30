package com.gustiadhitya.sakuwise.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
