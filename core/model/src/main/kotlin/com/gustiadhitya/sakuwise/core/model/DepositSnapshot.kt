package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

data class DepositSnapshot(
    val id: String,
    val assetDepositId: String,
    val snapshotDate: LocalDate,
    val balance: Long,
    val note: String?,
)
