package com.gustiadhitya.sakuwise.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gustiadhitya.sakuwise.core.database.converter.Converters
import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import com.gustiadhitya.sakuwise.core.database.dao.DebtDao
import com.gustiadhitya.sakuwise.core.database.dao.DepositDao
import com.gustiadhitya.sakuwise.core.database.dao.GoldDao
import com.gustiadhitya.sakuwise.core.database.dao.LandDao
import com.gustiadhitya.sakuwise.core.database.dao.PlanDao
import com.gustiadhitya.sakuwise.core.database.dao.NetWorthSnapshotDao
import com.gustiadhitya.sakuwise.core.database.dao.TransactionDao
import com.gustiadhitya.sakuwise.core.database.entity.AccountEntity
import com.gustiadhitya.sakuwise.core.database.entity.AccountSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.AllocationEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetDepositEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetDepositSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetGoldEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetLandEntity
import com.gustiadhitya.sakuwise.core.database.entity.CategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtPaymentEntity
import com.gustiadhitya.sakuwise.core.database.entity.IncomeCategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.LandTaxPaymentEntity
import com.gustiadhitya.sakuwise.core.database.entity.NetWorthSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanItemEntity
import com.gustiadhitya.sakuwise.core.database.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        AccountSnapshotEntity::class,
        PlanEntity::class,
        AllocationEntity::class,
        CategoryEntity::class,
        PlanItemEntity::class,
        TransactionEntity::class,
        IncomeCategoryEntity::class,
        AssetGoldEntity::class,
        AssetLandEntity::class,
        LandTaxPaymentEntity::class,
        AssetDepositEntity::class,
        AssetDepositSnapshotEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        NetWorthSnapshotEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SakuwiseDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun planDao(): PlanDao
    abstract fun transactionDao(): TransactionDao
    abstract fun goldDao(): GoldDao
    abstract fun landDao(): LandDao
    abstract fun depositDao(): DepositDao
    abstract fun debtDao(): DebtDao
    abstract fun netWorthSnapshotDao(): NetWorthSnapshotDao
}
