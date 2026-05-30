package com.gustiadhitya.sakuwise.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migrations registry for [SakuwiseDatabase].
 *
 * ## Why this exists (v1.0.4 hardening)
 * Until v1.0.4 the database used `fallbackToDestructiveMigration()`, which
 * **wipes the entire user database** on any schema version bump. For an app
 * whose whole value is the user's financial history, that is unacceptable
 * once real users exist. This registry replaces destructive fallback with
 * explicit, tested migrations.
 *
 * ## Current state
 * The shipping schema is **version 5** (see `@Database(version = 5)`). No
 * migration objects exist yet because no schema change has happened since the
 * baseline was captured (`app/schemas/.../5.json`). [ALL] is therefore empty.
 * An upgrade from v1.0.3 → v1.0.4 is a no-op at the schema layer (both are
 * version 5), so no data is touched.
 *
 * ## How to add the next migration (e.g. when v1.1 adds account detail columns)
 * 1. Bump `@Database(version = 6)` in [SakuwiseDatabase].
 * 2. Build once so Room exports `app/schemas/.../6.json`. Commit that JSON.
 * 3. Add a `Migration(5, 6)` object below with the exact `ALTER TABLE` / DDL
 *    that transforms schema 5 → 6. Match the generated 6.json precisely.
 * 4. Append it to [ALL].
 * 5. Add a migration test in `MigrationTest` (un-`@Ignore` the 5→6 scaffold,
 *    or add a new case) that seeds v5 data, runs the migration, and asserts
 *    the data survived. CI runs this on every PR.
 *
 * Example template (DO NOT enable until schema actually changes):
 * ```
 * val MIGRATION_5_6 = object : Migration(5, 6) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE accounts ADD COLUMN bankName TEXT")
 *     }
 * }
 * ```
 */
object SakuwiseMigrations {

    /**
     * All migrations, in ascending order. Passed to
     * `Room.databaseBuilder(...).addMigrations(*SakuwiseMigrations.ALL)`.
     *
     * Empty for now (schema unchanged since baseline v5). When this is empty
     * and the DB version is bumped without a matching migration, Room will
     * throw `IllegalStateException` at open time — a LOUD, debuggable failure
     * in dev/CI — instead of silently destroying user data. That is the whole
     * point of removing `fallbackToDestructiveMigration()`.
     */
    val ALL: Array<Migration> = emptyArray()
}
