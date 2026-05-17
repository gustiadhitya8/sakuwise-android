package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

data class Plan(
    val id: String,
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate,
    val label: String,
    val expectedIncome: Long,
    val notes: String?,
)
