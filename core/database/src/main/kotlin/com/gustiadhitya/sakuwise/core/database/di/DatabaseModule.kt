package com.gustiadhitya.sakuwise.core.database.di

import android.content.Context
import androidx.room.Room
import com.gustiadhitya.sakuwise.core.database.SakuwiseDatabase
import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import com.gustiadhitya.sakuwise.core.database.dao.AccountSnapshotDao
import com.gustiadhitya.sakuwise.core.database.dao.AllocationDao
import com.gustiadhitya.sakuwise.core.database.dao.CategoryDao
import com.gustiadhitya.sakuwise.core.database.dao.DebtDao
import com.gustiadhitya.sakuwise.core.database.dao.DebtPaymentDao
import com.gustiadhitya.sakuwise.core.database.dao.DepositAssetDao
import com.gustiadhitya.sakuwise.core.database.dao.DepositSnapshotDao
import com.gustiadhitya.sakuwise.core.database.dao.GoldAssetDao
import com.gustiadhitya.sakuwise.core.database.dao.IncomeCategoryDao
import com.gustiadhitya.sakuwise.core.database.dao.LandAssetDao
import com.gustiadhitya.sakuwise.core.database.dao.LandTaxPaymentDao
import com.gustiadhitya.sakuwise.core.database.dao.PlanDao
import com.gustiadhitya.sakuwise.core.database.dao.PlanItemDao
import com.gustiadhitya.sakuwise.core.database.dao.TransactionDao
import com.gustiadhitya.sakuwise.core.database.dao.UserProfileDao
import com.gustiadhitya.sakuwise.core.database.migration.MIGRATIONS
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
    fun provideSakuwiseDatabase(@ApplicationContext context: Context): SakuwiseDatabase {
        // TODO(M4c): replace stub passphrase with KeyManager.getDek()
        val passphrase = "sakuwise_dev_stub_replace_in_M4c".toByteArray(Charsets.UTF_8)
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(
            context,
            SakuwiseDatabase::class.java,
            "sakuwise.db",
        )
            .openHelperFactory(factory)
            .addMigrations(*MIGRATIONS)
            .build()
    }

    @Provides fun provideAccountDao(db: SakuwiseDatabase): AccountDao = db.accountDao()
    @Provides fun provideAccountSnapshotDao(db: SakuwiseDatabase): AccountSnapshotDao = db.accountSnapshotDao()
    @Provides fun providePlanDao(db: SakuwiseDatabase): PlanDao = db.planDao()
    @Provides fun provideAllocationDao(db: SakuwiseDatabase): AllocationDao = db.allocationDao()
    @Provides fun provideCategoryDao(db: SakuwiseDatabase): CategoryDao = db.categoryDao()
    @Provides fun providePlanItemDao(db: SakuwiseDatabase): PlanItemDao = db.planItemDao()
    @Provides fun provideIncomeCategoryDao(db: SakuwiseDatabase): IncomeCategoryDao = db.incomeCategoryDao()
    @Provides fun provideTransactionDao(db: SakuwiseDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideGoldAssetDao(db: SakuwiseDatabase): GoldAssetDao = db.goldAssetDao()
    @Provides fun provideLandAssetDao(db: SakuwiseDatabase): LandAssetDao = db.landAssetDao()
    @Provides fun provideLandTaxPaymentDao(db: SakuwiseDatabase): LandTaxPaymentDao = db.landTaxPaymentDao()
    @Provides fun provideDepositAssetDao(db: SakuwiseDatabase): DepositAssetDao = db.depositAssetDao()
    @Provides fun provideDepositSnapshotDao(db: SakuwiseDatabase): DepositSnapshotDao = db.depositSnapshotDao()
    @Provides fun provideDebtDao(db: SakuwiseDatabase): DebtDao = db.debtDao()
    @Provides fun provideDebtPaymentDao(db: SakuwiseDatabase): DebtPaymentDao = db.debtPaymentDao()
    @Provides fun provideUserProfileDao(db: SakuwiseDatabase): UserProfileDao = db.userProfileDao()
}
