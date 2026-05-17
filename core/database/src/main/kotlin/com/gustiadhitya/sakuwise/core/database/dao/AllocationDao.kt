package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.AllocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllocationDao {
    @Query("SELECT * FROM allocations WHERE plan_id = :planId")
    fun observeByPlanId(planId: String): Flow<List<AllocationEntity>>

    @Upsert
    suspend fun upsert(allocation: AllocationEntity)

    @Upsert
    suspend fun upsertAll(allocations: List<AllocationEntity>)
}
