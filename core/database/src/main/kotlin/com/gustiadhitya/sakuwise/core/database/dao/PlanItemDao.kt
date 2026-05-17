package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.PlanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanItemDao {
    @Query("SELECT * FROM plan_items WHERE category_id = :categoryId")
    fun observeByCategoryId(categoryId: String): Flow<List<PlanItemEntity>>

    @Query("""
        SELECT pi.* FROM plan_items pi
        INNER JOIN categories c ON pi.category_id = c.id
        INNER JOIN allocations a ON c.allocation_id = a.id
        WHERE a.plan_id = :planId
    """)
    fun observeByPlanId(planId: String): Flow<List<PlanItemEntity>>

    @Upsert
    suspend fun upsert(planItem: PlanItemEntity)

    @Query("DELETE FROM plan_items WHERE id = :id")
    suspend fun delete(id: String)
}
