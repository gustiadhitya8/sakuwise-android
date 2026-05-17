package com.gustiadhitya.sakuwise.core.database.mapper

import com.gustiadhitya.sakuwise.core.database.entity.AllocationEntity
import com.gustiadhitya.sakuwise.core.database.entity.CategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanItemEntity
import com.gustiadhitya.sakuwise.core.model.Allocation
import com.gustiadhitya.sakuwise.core.model.Category
import com.gustiadhitya.sakuwise.core.model.Plan
import com.gustiadhitya.sakuwise.core.model.PlanItem

fun PlanEntity.toDomain(): Plan = Plan(
    id = id,
    periodStartDate = periodStartDate,
    periodEndDate = periodEndDate,
    label = label,
    expectedIncome = expectedIncome,
    notes = notes,
)

fun Plan.toEntity(): PlanEntity = PlanEntity(
    id = id,
    periodStartDate = periodStartDate,
    periodEndDate = periodEndDate,
    label = label,
    expectedIncome = expectedIncome,
    notes = notes,
)

fun AllocationEntity.toDomain(): Allocation = Allocation(
    id = id,
    planId = planId,
    name = name,
    percentageTarget = percentageTarget,
)

fun Allocation.toEntity(): AllocationEntity = AllocationEntity(
    id = id,
    planId = planId,
    name = name,
    percentageTarget = percentageTarget,
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    allocationId = allocationId,
    name = name,
    plannedAmount = plannedAmount,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    allocationId = allocationId,
    name = name,
    plannedAmount = plannedAmount,
)

fun PlanItemEntity.toDomain(): PlanItem = PlanItem(
    id = id,
    categoryId = categoryId,
    name = name,
    plannedAmount = plannedAmount,
    recurrence = recurrence,
    notes = notes,
)

fun PlanItem.toEntity(): PlanItemEntity = PlanItemEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    plannedAmount = plannedAmount,
    recurrence = recurrence,
    notes = notes,
)
