package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "debt_payments",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debt_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("debt_id")],
)
data class DebtPaymentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "debt_id") val debtId: String,
    @ColumnInfo(name = "payment_date") val paymentDate: LocalDate,
    @ColumnInfo(name = "amount") val amount: Long,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "transaction_id") val transactionId: String?,
)
