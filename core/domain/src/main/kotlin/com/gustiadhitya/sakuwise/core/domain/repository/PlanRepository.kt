package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.Allocation
import com.gustiadhitya.sakuwise.core.model.AllocationName
import com.gustiadhitya.sakuwise.core.model.Category
import com.gustiadhitya.sakuwise.core.model.Plan
import com.gustiadhitya.sakuwise.core.model.PlanItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PlanRepository {
    fun observeAll(): Flow<List<Plan>>
    fun observeById(id: String): Flow<Plan?>
    fun observeCurrent(): Flow<Plan?>
    fun observeByPeriod(date: LocalDate): Flow<Plan?>
    fun observeAllocations(planId: String): Flow<List<Allocation>>
    fun observeCategories(allocationId: String): Flow<List<Category>>
    fun observePlanItems(categoryId: String): Flow<List<PlanItem>>
    fun observeAllPlanItems(planId: String): Flow<List<PlanItem>>
    suspend fun upsertPlan(plan: Plan)
    suspend fun upsertAllocation(allocation: Allocation)
    suspend fun upsertCategory(category: Category)
    suspend fun upsertPlanItem(planItem: PlanItem)
    suspend fun deletePlanItem(id: String)
    suspend fun deleteCategory(id: String)
    suspend fun updateAllocationPercentages(planId: String, percentages: Map<AllocationName, Int>)
    suspend fun deletePlan(id: String)
}
