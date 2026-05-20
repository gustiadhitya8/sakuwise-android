package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.AllocationEntity
import com.gustiadhitya.sakuwise.core.database.entity.CategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY endEpochDay DESC")
    fun observeAll(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE :epochDay BETWEEN startEpochDay AND endEpochDay LIMIT 1")
    fun observeForDate(epochDay: Long): Flow<PlanEntity?>

    @Query("SELECT * FROM plans WHERE id = :id")
    fun observeById(id: String): Flow<PlanEntity?>

    @Upsert
    suspend fun upsert(plan: PlanEntity)

    @Query("SELECT * FROM allocations WHERE planId = :planId ORDER BY sortOrder")
    fun observeAllocations(planId: String): Flow<List<AllocationEntity>>

    @Upsert
    suspend fun upsertAllocation(allocation: AllocationEntity)

    @Query("SELECT * FROM categories WHERE allocationId = :allocationId ORDER BY sortOrder")
    fun observeCategories(allocationId: String): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)

    @Query("SELECT * FROM plan_items WHERE categoryId = :categoryId ORDER BY sortOrder")
    fun observePlanItems(categoryId: String): Flow<List<PlanItemEntity>>

    @Query("SELECT * FROM plan_items WHERE id = :id")
    suspend fun getPlanItem(id: String): PlanItemEntity?

    @Query("SELECT * FROM plan_items WHERE id = :id")
    fun observePlanItem(id: String): Flow<PlanItemEntity?>

    @Upsert
    suspend fun upsertPlanItem(item: PlanItemEntity)

    @Query("DELETE FROM plan_items WHERE id = :id")
    suspend fun deletePlanItem(id: String)

    @Query("SELECT IFNULL(SUM(t.amount), 0) FROM transactions t WHERE t.planItemId = :planItemId AND t.type = 'expense'")
    fun observePlanItemUsed(planItemId: String): Flow<Long>
}
