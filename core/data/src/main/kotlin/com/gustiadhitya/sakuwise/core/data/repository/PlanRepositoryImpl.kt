package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.AllocationDao
import com.gustiadhitya.sakuwise.core.database.dao.CategoryDao
import com.gustiadhitya.sakuwise.core.database.dao.PlanDao
import com.gustiadhitya.sakuwise.core.database.dao.PlanItemDao
import com.gustiadhitya.sakuwise.core.database.entity.AllocationEntity
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.model.Allocation
import com.gustiadhitya.sakuwise.core.model.AllocationName
import com.gustiadhitya.sakuwise.core.model.Category
import com.gustiadhitya.sakuwise.core.model.Plan
import com.gustiadhitya.sakuwise.core.model.PlanItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PlanRepositoryImpl @Inject constructor(
    private val planDao: PlanDao,
    private val allocationDao: AllocationDao,
    private val categoryDao: CategoryDao,
    private val planItemDao: PlanItemDao,
) : PlanRepository {

    override fun observeAll(): Flow<List<Plan>> =
        planDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Plan?> =
        planDao.observeById(id).map { it?.toDomain() }

    override fun observeCurrent(): Flow<Plan?> =
        planDao.observeCurrent(LocalDate.now().toEpochDay()).map { it?.toDomain() }

    override fun observeByPeriod(date: LocalDate): Flow<Plan?> =
        planDao.observeCurrent(date.toEpochDay()).map { it?.toDomain() }

    override fun observeAllocations(planId: String): Flow<List<Allocation>> =
        allocationDao.observeByPlanId(planId).map { list -> list.map { it.toDomain() } }

    override fun observeCategories(allocationId: String): Flow<List<Category>> =
        categoryDao.observeByAllocationId(allocationId).map { list -> list.map { it.toDomain() } }

    override fun observePlanItems(categoryId: String): Flow<List<PlanItem>> =
        planItemDao.observeByCategoryId(categoryId).map { list -> list.map { it.toDomain() } }

    override fun observeAllPlanItems(planId: String): Flow<List<PlanItem>> =
        planItemDao.observeByPlanId(planId).map { list -> list.map { it.toDomain() } }

    override suspend fun upsertPlan(plan: Plan) = planDao.upsert(plan.toEntity())

    override suspend fun upsertAllocation(allocation: Allocation) = allocationDao.upsert(allocation.toEntity())

    override suspend fun upsertCategory(category: Category) = categoryDao.upsert(category.toEntity())

    override suspend fun upsertPlanItem(planItem: PlanItem) = planItemDao.upsert(planItem.toEntity())

    override suspend fun deletePlanItem(id: String) = planItemDao.delete(id)

    override suspend fun deleteCategory(id: String) = categoryDao.delete(id)

    override suspend fun updateAllocationPercentages(planId: String, percentages: Map<AllocationName, Int>) {
        val entities = percentages.map { (name, pct) ->
            AllocationEntity(
                id = UUID.randomUUID().toString(),
                planId = planId,
                name = name,
                percentageTarget = pct,
            )
        }
        allocationDao.upsertAll(entities)
    }

    override suspend fun deletePlan(id: String) = planDao.delete(id)
}
