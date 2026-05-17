package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

data class Debt(
    val id: String,
    val counterparty: String,
    val direction: DebtDirection,
    val principal: Long,
    val dateOpened: LocalDate,
    val expectedCloseDate: LocalDate?,
    val status: DebtStatus,
    val note: String?,
)
