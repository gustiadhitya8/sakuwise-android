package com.gustiadhitya.sakuwise.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.RoomDatabase
import com.gustiadhitya.sakuwise.core.crypto.KeyManager
import com.gustiadhitya.sakuwise.core.database.SakuwiseDatabase
import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import com.gustiadhitya.sakuwise.core.database.dao.DebtDao
import com.gustiadhitya.sakuwise.core.database.dao.DepositDao
import com.gustiadhitya.sakuwise.core.database.dao.GoldDao
import com.gustiadhitya.sakuwise.core.database.dao.LandDao
import com.gustiadhitya.sakuwise.core.database.dao.PlanDao
import com.gustiadhitya.sakuwise.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKeyManager(@ApplicationContext ctx: Context): KeyManager = KeyManager(ctx)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext ctx: Context,
        keyManager: KeyManager,
    ): SakuwiseDatabase {
        // SQLCipher must be loaded once per process before opening any DB.
        System.loadLibrary("sqlcipher")
        val dek = keyManager.getOrCreateDek()
        try {
            val factory = SupportOpenHelperFactory(dek.copyOf())
            return Room.databaseBuilder(ctx, SakuwiseDatabase::class.java, "sakuwise.db")
                .openHelperFactory(factory)
                .addCallback(seedCallback())
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                // Keep destructive fallback as a safety net for any other
                // schema drift not yet covered by a Migration. Real launches
                // should remove this once we trust the migration chain.
                .fallbackToDestructiveMigration()
                .build()
        } finally {
            keyManager.zeroize(dek)
        }
    }

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
     * property purchases (e.g. "field bought 6 months ago"). NOT NULL with
     * DEFAULT 0 so existing rows migrate without manual fill; the mapper
     * coerces epoch-day 0 to today on read.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE asset_land ADD COLUMN purchaseEpochDay INTEGER NOT NULL DEFAULT 0",
            )
        }
    }

    /**
     * v3 → v4 — `asset_gold.weightGram` (INTEGER, grams) renamed and rescaled
     * to `weightMilliGram` (INTEGER, milligrams). Old rows like "10 gram"
     * become 10_000; storing in milligrams lets users save fractional weights
     * like 0.5g (= 500) without floating-point rounding errors when summing
     * holdings. SQLite ≤ 3.34 (Android < 12) can't DROP COLUMN, so the
     * canonical recreate-table-and-rename pattern is used.
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
     * Seed default income categories on first DB creation. Matches the
     * prototype's "Kategori Sumber" picker contents (PRD §7.2.1).
     * Uses raw INSERT so we don't need DAOs at callback time.
     */
    private fun seedCallback(): RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            val rows = listOf(
                "gaji-pokok" to "Gaji Pokok",
                "bonus" to "Bonus",
                "thr" to "THR",
                "penghasilan-sampingan" to "Penghasilan Sampingan",
                "lainnya" to "Lainnya",
            )
            rows.forEachIndexed { i, (id, name) ->
                db.execSQL(
                    "INSERT OR IGNORE INTO income_categories (id, name, iconName, sortOrder, isDefault) " +
                        "VALUES (?, ?, ?, ?, ?)",
                    arrayOf<Any>("${id}-$now", name, "income", i, 1),
                )
            }
        }
    }

    @Provides fun provideAccountDao(db: SakuwiseDatabase): AccountDao = db.accountDao()
    @Provides fun providePlanDao(db: SakuwiseDatabase): PlanDao = db.planDao()
    @Provides fun provideTransactionDao(db: SakuwiseDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideGoldDao(db: SakuwiseDatabase): GoldDao = db.goldDao()
    @Provides fun provideLandDao(db: SakuwiseDatabase): LandDao = db.landDao()
    @Provides fun provideDepositDao(db: SakuwiseDatabase): DepositDao = db.depositDao()
    @Provides fun provideDebtDao(db: SakuwiseDatabase): DebtDao = db.debtDao()
    @Provides fun provideNetWorthSnapshotDao(db: SakuwiseDatabase): com.gustiadhitya.sakuwise.core.database.dao.NetWorthSnapshotDao = db.netWorthSnapshotDao()
}
