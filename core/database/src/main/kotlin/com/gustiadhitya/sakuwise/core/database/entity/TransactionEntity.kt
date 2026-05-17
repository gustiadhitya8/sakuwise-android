package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gustiadhitya.sakuwise.core.model.TransactionType
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "amount") val amount: Long,
    @ColumnInfo(name = "type") val type: TransactionType,
    @ColumnInfo(name = "income_category_id") val incomeCategoryId: String?,
    @ColumnInfo(name = "plan_item_id") val planItemId: String?,
    @ColumnInfo(name = "source_account_id") val sourceAccountId: String?,
    @ColumnInfo(name = "destination_account_id") val destinationAccountId: String?,
    @ColumnInfo(name = "fee_amount") val feeAmount: Long?,
    @ColumnInfo(name = "fee_plan_item_id") val feePlanItemId: String?,
    @ColumnInfo(name = "debt_id") val debtId: String?,
    @ColumnInfo(name = "photo_blob") val photoBlob: ByteArray?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionEntity) return false
        return id == other.id && date == other.date && amount == other.amount &&
            type == other.type && incomeCategoryId == other.incomeCategoryId &&
            planItemId == other.planItemId && sourceAccountId == other.sourceAccountId &&
            destinationAccountId == other.destinationAccountId && feeAmount == other.feeAmount &&
            feePlanItemId == other.feePlanItemId && debtId == other.debtId &&
            photoBlob.contentEquals(other.photoBlob) &&
            note == other.note && createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (incomeCategoryId?.hashCode() ?: 0)
        result = 31 * result + (planItemId?.hashCode() ?: 0)
        result = 31 * result + (sourceAccountId?.hashCode() ?: 0)
        result = 31 * result + (destinationAccountId?.hashCode() ?: 0)
        result = 31 * result + (feeAmount?.hashCode() ?: 0)
        result = 31 * result + (feePlanItemId?.hashCode() ?: 0)
        result = 31 * result + (debtId?.hashCode() ?: 0)
        result = 31 * result + (photoBlob?.contentHashCode() ?: 0)
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
