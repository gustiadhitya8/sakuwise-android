package com.gustiadhitya.sakuwise.core.model

data class Category(
    val id: String,
    val allocationId: String,
    val name: String,
    val plannedAmount: Long?,
)
