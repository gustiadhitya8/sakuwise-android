package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.PlanDao
import com.gustiadhitya.sakuwise.core.domain.model.Allocation
import com.gustiadhitya.sakuwise.core.domain.model.Category
import com.gustiadhitya.sakuwise.core.domain.model.Plan
import com.gustiadhitya.sakuwise.core.domain.model.PlanItem
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class PlanRepositoryImpl @Inject constructor(
    private val dao: PlanDao,
) : PlanRepository {
    override fun observeAll(): Flow<List<Plan>> = dao.observeAll().map { list -> list.map { it.toDomain() } }
    override fun observeForDate(date: LocalDate): Flow<Plan?> =
        dao.observeForDate(date.toEpochDay()).map { it?.toDomain() }
    override fun observeById(id: String): Flow<Plan?> = dao.observeById(id).map { it?.toDomain() }
    override suspend fun upsert(plan: Plan) = dao.upsert(plan.toEntity())

    override fun observeAllocations(planId: String): Flow<List<Allocation>> =
        dao.observeAllocations(planId).map { list -> list.map { it.toDomain() } }
    override suspend fun upsertAllocation(allocation: Allocation) =
        dao.upsertAllocation(allocation.toEntity())

    override fun observeCategories(allocationId: String): Flow<List<Category>> =
        dao.observeCategories(allocationId).map { list -> list.map { it.toDomain() } }
    override suspend fun upsertCategory(category: Category) = dao.upsertCategory(category.toEntity())
    override suspend fun deleteCategory(id: String) = dao.deleteCategory(id)

    override fun observePlanItems(categoryId: String): Flow<List<PlanItem>> =
        dao.observePlanItems(categoryId).map { list -> list.map { it.toDomain() } }
    override suspend fun getPlanItem(id: String): PlanItem? = dao.getPlanItem(id)?.toDomain()
    override fun observePlanItem(id: String): Flow<PlanItem?> = dao.observePlanItem(id).map { it?.toDomain() }
    override suspend fun upsertPlanItem(item: PlanItem) = dao.upsertPlanItem(item.toEntity())
    override suspend fun deletePlanItem(id: String) = dao.deletePlanItem(id)
    override fun observePlanItemUsed(planItemId: String): Flow<Long> = dao.observePlanItemUsed(planItemId)
}
