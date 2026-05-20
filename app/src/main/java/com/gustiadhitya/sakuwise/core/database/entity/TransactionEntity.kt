package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val amount: Long,
    /** "income" | "expense" | "transfer" | "debt_inflow" | "debt_outflow" | "reconciliation" */
    val type: String,
    val planItemId: String?,       // required for type=expense
    val sourceAccountId: String,
    val destAccountId: String?,    // transfer only
    val transferFee: Long?,        // transfer only
    val debtId: String?,
    val photoBlob: ByteArray?,
    val incomeCategoryId: String?, // income only
    val note: String?,
    val createdAt: Long,
) {
    // override equals/hashCode because of the ByteArray field
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionEntity) return false
        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
}

@Entity(tableName = "income_categories")
data class IncomeCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String?,
    val sortOrder: Int,
    val isDefault: Boolean = false,
)
