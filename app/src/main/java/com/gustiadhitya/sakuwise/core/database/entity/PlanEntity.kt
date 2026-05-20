package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey val id: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val label: String,
    val expectedIncome: Long,
    val note: String?,
)

@Entity(tableName = "allocations")
data class AllocationEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val name: String,        // "Needs" / "Wants" / "Investment"
    val targetPct: Int,
    val sortOrder: Int,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val allocationId: String,
    val name: String,
    val plannedAmount: Long?,
    val sortOrder: Int,
)

@Entity(tableName = "plan_items")
data class PlanItemEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val plannedAmount: Long,
    val recurrence: String,  // "oneoff" / "monthly" / "quarterly" / "yearly"
    val note: String?,
    val sortOrder: Int,
)
