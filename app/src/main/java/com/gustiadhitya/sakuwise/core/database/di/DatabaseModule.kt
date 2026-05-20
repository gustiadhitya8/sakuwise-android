package com.gustiadhitya.sakuwise.core.database.di

import android.content.Context
import androidx.room.Room
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
                // v1 → v2: adds net_worth_snapshots table. Pre-prod schema bump;
                // existing dev installs will have their DB wiped. Safe because
                // there are no production users yet.
                .fallbackToDestructiveMigration()
                .build()
        } finally {
            keyManager.zeroize(dek)
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
