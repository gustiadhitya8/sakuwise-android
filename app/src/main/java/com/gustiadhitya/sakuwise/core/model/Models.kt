package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

enum class AccountType { Cash, Bank, EWallet }

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val balance: Long,
)

enum class AllocationId { Needs, Wants, Invest }

data class Allocation(
    val id: AllocationId,
    val name: String,
    val targetPct: Int,
    val plan: Long,
    val used: Long,
)

enum class TxnType { Expense, Income, Transfer }

data class Transaction(
    val id: String,
    val type: TxnType,
    val amount: Long,
    val date: LocalDate,
    val category: String,
    val merchant: String?,
    val accountId: String,
)

data class TopCategory(val name: String, val amount: Long, val color: ColorTone)

enum class ColorTone { Primary, Accent, Info, Warning, Success, Danger }

data class PeriodInfo(val label: String, val daysLeft: Int)
