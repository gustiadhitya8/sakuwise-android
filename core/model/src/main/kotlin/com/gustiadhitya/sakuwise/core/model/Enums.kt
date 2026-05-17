package com.gustiadhitya.sakuwise.core.model

enum class AccountType { CASH, BANK, E_WALLET, OTHER }

enum class AccountStatus { ACTIVE, ARCHIVED }

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER, DEBT_INFLOW, DEBT_OUTFLOW, RECONCILIATION
}

enum class RecurrenceType { ONE_OFF, MONTHLY, QUARTERLY, YEARLY }

enum class AllocationName { NEEDS, WANTS, INVESTMENT }

enum class GoldAssetStatus { HELD, SOLD }

enum class LandAssetStatus { HELD, SOLD }

enum class DepositAssetStatus { ACTIVE, CLOSED }

enum class DepositAssetType { DPLK, BPJSTK, DEPOSITO, OTHER }

enum class DebtDirection { I_OWE, OWED_TO_ME }

enum class DebtStatus { OPEN, SETTLED }
