package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Net-worth time series. One row per day (epoch-day primary key).
 * Populated by NetWorthSnapshotWorker — see core/work/.
 */
@Entity(tableName = "net_worth_snapshots")
data class NetWorthSnapshotEntity(
    @PrimaryKey val epochDay: Long,
    val accountsTotal: Long,
    val goldTotal: Long,
    val landTotal: Long,
    val depositTotal: Long,
    val debtsTotal: Long,
    val total: Long,
)
