package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gustiadhitya.sakuwise.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE date >= :startEpochDay AND date <= :endEpochDay ORDER BY date DESC, created_at DESC")
    fun observeByDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE source_account_id = :accountId OR destination_account_id = :accountId ORDER BY date DESC, created_at DESC")
    fun observeByAccountId(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE plan_item_id = :planItemId ORDER BY date DESC, created_at DESC")
    fun observeByPlanItemId(planItemId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE debt_id = :debtId ORDER BY date DESC, created_at DESC")
    fun observeByDebtId(debtId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, created_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)
}
