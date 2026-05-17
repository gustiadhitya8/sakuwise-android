package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "account_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("account_id")],
)
data class AccountSnapshotEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "snapshot_date") val snapshotDate: LocalDate,
    @ColumnInfo(name = "observed_balance") val observedBalance: Long,
    @ColumnInfo(name = "computed_balance_at_snapshot") val computedBalanceAtSnapshot: Long,
    @ColumnInfo(name = "adjustment_amount") val adjustmentAmount: Long,
)
