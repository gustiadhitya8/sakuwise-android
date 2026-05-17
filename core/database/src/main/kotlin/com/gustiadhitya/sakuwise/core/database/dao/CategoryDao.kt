package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE allocation_id = :allocationId")
    fun observeByAllocationId(allocationId: String): Flow<List<CategoryEntity>>

    @Query("""
        SELECT c.* FROM categories c
        INNER JOIN allocations a ON c.allocation_id = a.id
        WHERE a.plan_id = :planId
    """)
    fun observeByPlanId(planId: String): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)
}
