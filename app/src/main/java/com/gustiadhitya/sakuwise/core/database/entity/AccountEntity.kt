package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,         // "cash" / "bank" / "ewallet" / "other"
    val initialBalance: Long,
    val iconName: String?,
    val colorHex: String?,
    val archived: Boolean = false,
    val createdAt: Long,
)

@Entity(tableName = "account_snapshots")
data class AccountSnapshotEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val snapshotEpochDay: Long,
    val observedBalance: Long,
    val computedBalance: Long,
    val diff: Long,
    val note: String?,
)
