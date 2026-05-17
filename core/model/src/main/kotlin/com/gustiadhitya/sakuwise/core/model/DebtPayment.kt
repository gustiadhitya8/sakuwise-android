package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

data class DebtPayment(
    val id: String,
    val debtId: String,
    val paymentDate: LocalDate,
    val amount: Long,
    val accountId: String,
    val transactionId: String?,
)
