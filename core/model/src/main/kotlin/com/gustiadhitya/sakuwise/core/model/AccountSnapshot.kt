package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

data class AccountSnapshot(
    val id: String,
    val accountId: String,
    val snapshotDate: LocalDate,
    val observedBalance: Long,
    val computedBalanceAtSnapshot: Long,
    val adjustmentAmount: Long,
)
