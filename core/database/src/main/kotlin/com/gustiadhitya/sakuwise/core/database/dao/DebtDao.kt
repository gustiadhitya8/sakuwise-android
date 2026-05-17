package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY date_opened DESC")
    fun observeAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    fun observeById(id: String): Flow<DebtEntity?>

    @Query("SELECT * FROM debts WHERE status = 'OPEN' ORDER BY date_opened DESC")
    fun observeOpen(): Flow<List<DebtEntity>>

    @Upsert
    suspend fun upsert(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun delete(id: String)
}
