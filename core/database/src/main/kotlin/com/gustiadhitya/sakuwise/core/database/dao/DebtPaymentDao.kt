package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gustiadhitya.sakuwise.core.database.entity.DebtPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtPaymentDao {
    @Query("SELECT * FROM debt_payments WHERE debt_id = :debtId ORDER BY payment_date DESC")
    fun observeByDebtId(debtId: String): Flow<List<DebtPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: DebtPaymentEntity)

    @Query("DELETE FROM debt_payments WHERE id = :id")
    suspend fun delete(id: String)
}
