package com.gustiadhitya.sakuwise.core.model

import java.time.Instant
import java.time.LocalDate

sealed class Transaction {

    data class Income(
        val id: String,
        val date: LocalDate,
        val amount: Long,
        val incomeCategoryId: String,
        val accountId: String,
        val note: String?,
        val createdAt: Instant,
    ) : Transaction()

    data class Expense(
        val id: String,
        val date: LocalDate,
        val amount: Long,
        val planItemId: String,
        val accountId: String,
        val debtId: String?,
        val photoBlob: ByteArray?,
        val note: String?,
        val createdAt: Instant,
    ) : Transaction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Expense) return false
            return id == other.id && date == other.date && amount == other.amount &&
                planItemId == other.planItemId && accountId == other.accountId &&
                debtId == other.debtId && photoBlob.contentEquals(other.photoBlob) &&
                note == other.note && createdAt == other.createdAt
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + date.hashCode()
            result = 31 * result + amount.hashCode()
            result = 31 * result + planItemId.hashCode()
            result = 31 * result + accountId.hashCode()
            result = 31 * result + (debtId?.hashCode() ?: 0)
            result = 31 * result + (photoBlob?.contentHashCode() ?: 0)
            result = 31 * result + (note?.hashCode() ?: 0)
            result = 31 * result + createdAt.hashCode()
            return result
        }
    }

    data class Transfer(
        val id: String,
        val date: LocalDate,
        val amount: Long,
        val sourceAccountId: String,
        val destinationAccountId: String,
        val feeAmount: Long?,
        val feePlanItemId: String?,
        val note: String?,
        val createdAt: Instant,
    ) : Transaction()

    data class DebtInflow(
        val id: String,
        val date: LocalDate,
        val amount: Long,
        val accountId: String,
        val debtId: String,
        val note: String?,
        val createdAt: Instant,
    ) : Transaction()

    data class DebtOutflow(
        val id: String,
        val date: LocalDate,
        val amount: Long,
        val accountId: String,
        val debtId: String,
        val note: String?,
        val createdAt: Instant,
    ) : Transaction()

    data class Reconciliation(
        val id: String,
        val date: LocalDate,
        val amount: Long,
        val accountId: String,
        val note: String?,
        val createdAt: Instant,
    ) : Transaction() {
        companion object {
            fun create(accountId: String, adjustmentAmount: Long, note: String?): Reconciliation =
                Reconciliation(
                    id = java.util.UUID.randomUUID().toString(),
                    date = LocalDate.now(),
                    amount = adjustmentAmount,
                    accountId = accountId,
                    note = note,
                    createdAt = Instant.now(),
                )
        }
    }
}
