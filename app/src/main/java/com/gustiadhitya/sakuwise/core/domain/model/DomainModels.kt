package com.gustiadhitya.sakuwise.core.domain.model

import java.time.LocalDate

enum class AccountType { Cash, Bank, EWallet, Other;
    companion object {
        fun fromCode(code: String): AccountType = when (code.lowercase()) {
            "cash" -> Cash; "bank" -> Bank; "ewallet" -> EWallet; else -> Other
        }
    }
    fun code(): String = when (this) {
        Cash -> "cash"; Bank -> "bank"; EWallet -> "ewallet"; Other -> "other"
    }
}

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val initialBalance: Long,
    val iconName: String?,
    val archived: Boolean,
)

data class AccountSnapshot(
    val id: String,
    val accountId: String,
    val date: LocalDate,
    val observedBalance: Long,
    val computedBalance: Long,
    val diff: Long,
    val note: String?,
)

enum class AllocationId { Needs, Wants, Invest;
    companion object {
        fun fromName(name: String): AllocationId = when (name.lowercase()) {
            "needs", "kebutuhan" -> Needs
            "wants", "keinginan" -> Wants
            else -> Invest
        }
    }
}

data class Plan(
    val id: String,
    val start: LocalDate,
    val end: LocalDate,
    val label: String,
    val expectedIncome: Long,
    val note: String?,
)

data class Allocation(
    val id: String,
    val planId: String,
    val name: String,
    val targetPct: Int,
    val sortOrder: Int,
)

data class Category(
    val id: String,
    val allocationId: String,
    val name: String,
    val plannedAmount: Long?,
    val sortOrder: Int,
)

enum class Recurrence { OneOff, Monthly, Quarterly, Yearly;
    companion object {
        fun fromCode(code: String): Recurrence = when (code.lowercase()) {
            "monthly" -> Monthly
            "quarterly" -> Quarterly
            "yearly" -> Yearly
            else -> OneOff
        }
    }
    fun code(): String = name.lowercase().replace("oneoff", "oneoff")
}

data class PlanItem(
    val id: String,
    val categoryId: String,
    val name: String,
    val plannedAmount: Long,
    val recurrence: Recurrence,
    val note: String?,
    val sortOrder: Int,
)

enum class TxnType {
    Income, Expense, Transfer, DebtInflow, DebtOutflow, Reconciliation;
    fun code(): String = when (this) {
        Income -> "income"; Expense -> "expense"; Transfer -> "transfer"
        DebtInflow -> "debt_inflow"; DebtOutflow -> "debt_outflow"
        Reconciliation -> "reconciliation"
    }
    companion object {
        fun fromCode(code: String): TxnType = when (code) {
            "income" -> Income; "expense" -> Expense; "transfer" -> Transfer
            "debt_inflow" -> DebtInflow; "debt_outflow" -> DebtOutflow
            else -> Reconciliation
        }
    }
}

data class Transaction(
    val id: String,
    val date: LocalDate,
    val amount: Long,
    val type: TxnType,
    val planItemId: String?,
    val sourceAccountId: String,
    val destAccountId: String?,
    val transferFee: Long?,
    val debtId: String?,
    val photoBlob: ByteArray?,
    val incomeCategoryId: String?,
    val note: String?,
    val createdAt: Long,
) {
    override fun equals(other: Any?): Boolean = (other as? Transaction)?.id == id
    override fun hashCode(): Int = id.hashCode()
}

data class IncomeCategory(
    val id: String,
    val name: String,
    val iconName: String?,
    val sortOrder: Int,
    val isDefault: Boolean,
)

// ─── Assets ────────────────────────────────────────────────────
enum class AssetStatus { Held, Sold;
    fun code() = if (this == Held) "held" else "sold"
    companion object {
        fun fromCode(c: String) = if (c == "sold") Sold else Held
    }
}

data class GoldAsset(
    val id: String,
    val purchaseDate: LocalDate,
    val weightGram: Int,
    val serial: String?,
    val buyPrice: Long,
    val note: String?,
    val status: AssetStatus,
    val soldDate: LocalDate?,
    val soldPrice: Long?,
)

data class LandAsset(
    val id: String,
    val name: String,
    val location: String,
    val sertifikatId: String,
    val sizeM2: Int,
    val buyPrice: Long,
    val currentValue: Long?,
    val note: String?,
    val status: AssetStatus,
    val soldDate: LocalDate?,
    val soldPrice: Long?,
)

data class LandTaxPayment(
    val id: String,
    val assetLandId: String,
    val date: LocalDate,
    val amount: Long,
    val accountId: String?,
    val note: String?,
)

enum class DepositType { DPLK, BPJSTK, Deposito, Other;
    fun code(): String = when (this) {
        DPLK -> "DPLK"; BPJSTK -> "BPJSTK"; Deposito -> "Deposito"; Other -> "Other"
    }
    companion object {
        fun fromCode(c: String) = entries.firstOrNull { it.code() == c } ?: Other
    }
}

data class DepositAsset(
    val id: String,
    val name: String,
    val typeLabel: DepositType,
    val institutionInfo: String?,
    val note: String?,
    val active: Boolean,
)

data class DepositSnapshot(
    val id: String,
    val depositAssetId: String,
    val date: LocalDate,
    val balance: Long,
    val note: String?,
)

enum class DebtDirection { IOwe, OwedToMe;
    fun code() = if (this == IOwe) "i_owe" else "owed_to_me"
    companion object {
        fun fromCode(c: String) = if (c == "owed_to_me") OwedToMe else IOwe
    }
}

data class Debt(
    val id: String,
    val counterparty: String,
    val direction: DebtDirection,
    val principal: Long,
    val startDate: LocalDate,
    val dueDate: LocalDate?,
    val open: Boolean,
    val note: String?,
)

data class DebtPayment(
    val id: String,
    val debtId: String,
    val date: LocalDate,
    val amount: Long,
    val accountId: String?,
    val transactionId: String?,
    val note: String?,
)

// ─── Period ─────────────────────────────────────────────────────
data class PlanPeriod(val start: LocalDate, val end: LocalDate, val label: String) {
    val daysLeft: Int
        get() {
            val today = LocalDate.now()
            return if (today.isBefore(start)) {
                java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt() + 1
            } else if (today.isAfter(end)) {
                0
            } else {
                java.time.temporal.ChronoUnit.DAYS.between(today, end).toInt() + 1
            }
        }
}
