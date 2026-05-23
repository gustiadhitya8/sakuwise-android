package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.AssetDepositEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetDepositSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetGoldEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetLandEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtPaymentEntity
import com.gustiadhitya.sakuwise.core.database.entity.LandTaxPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoldDao {
    @Query("SELECT * FROM asset_gold ORDER BY purchaseEpochDay DESC")
    fun observeAll(): Flow<List<AssetGoldEntity>>

    @Query("SELECT * FROM asset_gold WHERE id = :id")
    fun observeById(id: String): Flow<AssetGoldEntity?>

    @Upsert
    suspend fun upsert(gold: AssetGoldEntity)

    @Query("DELETE FROM asset_gold WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface LandDao {
    @Query("SELECT * FROM asset_land ORDER BY name")
    fun observeAll(): Flow<List<AssetLandEntity>>

    @Query("SELECT * FROM asset_land WHERE id = :id")
    fun observeById(id: String): Flow<AssetLandEntity?>

    @Upsert
    suspend fun upsert(land: AssetLandEntity)

    @Query("DELETE FROM asset_land WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM land_tax_payments WHERE assetLandId = :landId ORDER BY payEpochDay DESC")
    fun observeTaxPayments(landId: String): Flow<List<LandTaxPaymentEntity>>

    @Upsert
    suspend fun upsertTaxPayment(payment: LandTaxPaymentEntity)

    @Query("DELETE FROM land_tax_payments WHERE id = :id")
    suspend fun deleteTaxPayment(id: String)
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM asset_deposit ORDER BY name")
    fun observeAll(): Flow<List<AssetDepositEntity>>

    @Query("SELECT * FROM asset_deposit WHERE id = :id")
    fun observeById(id: String): Flow<AssetDepositEntity?>

    @Upsert
    suspend fun upsert(deposit: AssetDepositEntity)

    @Query("DELETE FROM asset_deposit WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM asset_deposit_snapshots WHERE assetDepositId = :depositId ORDER BY snapshotEpochDay")
    fun observeSnapshots(depositId: String): Flow<List<AssetDepositSnapshotEntity>>

    @Query("SELECT * FROM asset_deposit_snapshots WHERE assetDepositId = :depositId ORDER BY snapshotEpochDay DESC LIMIT 1")
    fun observeLatestSnapshot(depositId: String): Flow<AssetDepositSnapshotEntity?>

    @Upsert
    suspend fun upsertSnapshot(snapshot: AssetDepositSnapshotEntity)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY status, startEpochDay DESC")
    fun observeAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    fun observeById(id: String): Flow<DebtEntity?>

    @Upsert
    suspend fun upsert(debt: DebtEntity)

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY payEpochDay DESC")
    fun observePayments(debtId: String): Flow<List<DebtPaymentEntity>>

    @Query("SELECT IFNULL(SUM(amount), 0) FROM debt_payments WHERE debtId = :debtId")
    fun observePaidTotal(debtId: String): Flow<Long>

    @Upsert
    suspend fun upsertPayment(payment: DebtPaymentEntity)

    @Query("DELETE FROM debt_payments WHERE id = :id")
    suspend fun deletePayment(id: String)
}
