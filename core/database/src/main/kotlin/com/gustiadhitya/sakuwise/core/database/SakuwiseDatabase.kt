package com.gustiadhitya.sakuwise.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gustiadhitya.sakuwise.core.database.converter.DateConverters
import com.gustiadhitya.sakuwise.core.database.converter.EnumConverters
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
import com.gustiadhitya.sakuwise.core.database.entity.AccountEntity
import com.gustiadhitya.sakuwise.core.database.entity.AccountSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.AllocationEntity
import com.gustiadhitya.sakuwise.core.database.entity.CategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtPaymentEntity
import com.gustiadhitya.sakuwise.core.database.entity.DepositAssetEntity
import com.gustiadhitya.sakuwise.core.database.entity.DepositSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.GoldAssetEntity
import com.gustiadhitya.sakuwise.core.database.entity.IncomeCategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.LandAssetEntity
import com.gustiadhitya.sakuwise.core.database.entity.LandTaxPaymentEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanItemEntity
import com.gustiadhitya.sakuwise.core.database.entity.TransactionEntity
import com.gustiadhitya.sakuwise.core.database.entity.UserProfileEntity

@Database(
    entities = [
        AccountEntity::class,
        AccountSnapshotEntity::class,
        PlanEntity::class,
        AllocationEntity::class,
        CategoryEntity::class,
        PlanItemEntity::class,
        IncomeCategoryEntity::class,
        TransactionEntity::class,
        GoldAssetEntity::class,
        LandAssetEntity::class,
        LandTaxPaymentEntity::class,
        DepositAssetEntity::class,
        DepositSnapshotEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        UserProfileEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DateConverters::class, EnumConverters::class)
abstract class SakuwiseDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun accountSnapshotDao(): AccountSnapshotDao
    abstract fun planDao(): PlanDao
    abstract fun allocationDao(): AllocationDao
    abstract fun categoryDao(): CategoryDao
    abstract fun planItemDao(): PlanItemDao
    abstract fun incomeCategoryDao(): IncomeCategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun goldAssetDao(): GoldAssetDao
    abstract fun landAssetDao(): LandAssetDao
    abstract fun landTaxPaymentDao(): LandTaxPaymentDao
    abstract fun depositAssetDao(): DepositAssetDao
    abstract fun depositSnapshotDao(): DepositSnapshotDao
    abstract fun debtDao(): DebtDao
    abstract fun debtPaymentDao(): DebtPaymentDao
    abstract fun userProfileDao(): UserProfileDao
}
