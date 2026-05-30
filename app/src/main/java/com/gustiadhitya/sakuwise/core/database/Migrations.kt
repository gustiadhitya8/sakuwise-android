package com.gustiadhitya.sakuwise.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migrations registry for [SakuwiseDatabase].
 *
 * ## Why this exists (v1.0.4 hardening)
 * Until v1.0.4 the database builder kept `fallbackToDestructiveMigration()` as
 * a "safety net" alongside the real migration chain. That fallback **wipes the
 * entire user database** on any schema version it can't migrate — unacceptable
 * once real users exist. v1.0.4 removes the fallback and relies solely on this
 * explicit, ordered, tested chain. An unmigrated version bump now fails LOUDLY
 * at open time instead of silently destroying financial history.
 *
 * ## Current chain
 * 1 → 2 → 3 → 4 → 5 (current shipping schema). New installs start at 5.
 * Schema JSONs are exported to `app/schemas/`; only 5.json exists because
 * schema export was enabled at v5, so historical 1–4 schemas were never
 * captured. That's fine: every existing device has already migrated to 5, and
 * [MigrationTest] validates the v5 baseline + scaffolds the next step.
 *
 * ## How to add the next migration (e.g. v1.1 account-detail columns)
 * 1. Bump `@Database(version = 6)` in [SakuwiseDatabase].
 * 2. Build once so Room exports `app/schemas/.../6.json`. Commit that JSON.
 * 3. Add a `MIGRATION_5_6` object below with the exact DDL (match 6.json).
 * 4. Append it to [ALL].
 * 5. In [MigrationTest], un-`@Ignore` the 5→6 case (seed v5 data, migrate,
 *    assert it survived). CI runs it on every PR.
 */
object SakuwiseMigrations {

    /**
     * v1 → v2 — adds the `net_worth_snapshots` table for the daily worker that
     * writes one row/day. Schema must match NetWorthSnapshotEntity exactly:
     * `epochDay` PK, six Long columns. Index implied by the PK; no FKs.
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS net_worth_snapshots (
                    epochDay INTEGER NOT NULL PRIMARY KEY,
                    accountsTotal INTEGER NOT NULL,
                    goldTotal INTEGER NOT NULL,
                    landTotal INTEGER NOT NULL,
                    depositTotal INTEGER NOT NULL,
                    debtsTotal INTEGER NOT NULL,
                    total INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    /**
     * v2 → v3 — adds `purchaseEpochDay` to `asset_land` so users can backdate
     * property purchases. NOT NULL with DEFAULT 0 so existing rows migrate
     * without manual fill; the mapper coerces epoch-day 0 to today on read.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE asset_land ADD COLUMN purchaseEpochDay INTEGER NOT NULL DEFAULT 0",
            )
        }
    }

    /**
     * v3 → v4 — `asset_gold.weightGram` (INTEGER, grams) rescaled to
     * `weightMilliGram` (INTEGER, milligrams) so fractional weights like 0.5g
     * (= 500) sum without float rounding. SQLite ≤ 3.34 (Android < 12) can't
     * DROP COLUMN, so the canonical recreate-and-rename pattern is used.
     */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE asset_gold_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    purchaseEpochDay INTEGER NOT NULL,
                    weightMilliGram INTEGER NOT NULL,
                    serial TEXT,
                    buyPrice INTEGER NOT NULL,
                    note TEXT,
                    photoBlob BLOB,
                    status TEXT NOT NULL,
                    soldEpochDay INTEGER,
                    soldPrice INTEGER
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO asset_gold_new (
                    id, purchaseEpochDay, weightMilliGram, serial, buyPrice,
                    note, photoBlob, status, soldEpochDay, soldPrice
                )
                SELECT
                    id, purchaseEpochDay, weightGram * 1000, serial, buyPrice,
                    note, photoBlob, status, soldEpochDay, soldPrice
                FROM asset_gold
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE asset_gold")
            db.execSQL("ALTER TABLE asset_gold_new RENAME TO asset_gold")
        }
    }

    /**
     * v4 → v5 — adds `kind` ("physical" | "digital") to asset_gold so users
     * can keep ANTAM bars and digital holdings in one list with separate
     * per-gram prices. Existing rows default to "physical".
     */
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE asset_gold ADD COLUMN kind TEXT NOT NULL DEFAULT 'physical'",
            )
        }
    }

    /**
     * All migrations in ascending order. Passed to
     * `Room.databaseBuilder(...).addMigrations(*SakuwiseMigrations.ALL)`.
     *
     * With destructive fallback removed, a version bump that lacks a matching
     * migration here makes Room throw `IllegalStateException` at open time — a
     * loud, debuggable failure in dev/CI instead of silent data loss.
     */
    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
    )
}
