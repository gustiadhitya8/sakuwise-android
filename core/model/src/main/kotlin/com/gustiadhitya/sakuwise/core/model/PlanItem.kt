package com.gustiadhitya.sakuwise.core.model

data class PlanItem(
    val id: String,
    val categoryId: String,
    val name: String,
    val plannedAmount: Long,
    val recurrence: RecurrenceType,
    val notes: String?,
)
