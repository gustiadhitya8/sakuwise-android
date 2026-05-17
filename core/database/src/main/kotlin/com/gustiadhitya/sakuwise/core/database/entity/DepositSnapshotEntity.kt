package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "deposit_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = DepositAssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["asset_deposit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("asset_deposit_id")],
)
data class DepositSnapshotEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "asset_deposit_id") val assetDepositId: String,
    @ColumnInfo(name = "snapshot_date") val snapshotDate: LocalDate,
    @ColumnInfo(name = "balance") val balance: Long,
    @ColumnInfo(name = "note") val note: String?,
)
