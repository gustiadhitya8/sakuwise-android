package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "period_start_date") val periodStartDate: LocalDate,
    @ColumnInfo(name = "period_end_date") val periodEndDate: LocalDate,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "expected_income") val expectedIncome: Long,
    @ColumnInfo(name = "notes") val notes: String?,
)
