package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY period_start_date DESC")
    fun observeAll(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE id = :id")
    fun observeById(id: String): Flow<PlanEntity?>

    @Query("SELECT * FROM plans WHERE period_start_date <= :todayEpochDay AND period_end_date >= :todayEpochDay LIMIT 1")
    fun observeCurrent(todayEpochDay: Long): Flow<PlanEntity?>

    @Query("SELECT * FROM plans WHERE period_start_date >= :startEpochDay AND period_end_date <= :endEpochDay ORDER BY period_start_date DESC")
    fun observeByPeriod(startEpochDay: Long, endEpochDay: Long): Flow<List<PlanEntity>>

    @Upsert
    suspend fun upsert(plan: PlanEntity)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun delete(id: String)
}
