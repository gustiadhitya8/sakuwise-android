package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gustiadhitya.sakuwise.core.model.AccountStatus
import com.gustiadhitya.sakuwise.core.model.AccountType
import java.time.Instant

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: AccountType,
    @ColumnInfo(name = "initial_balance") val initialBalance: Long,
    @ColumnInfo(name = "color") val color: String?,
    @ColumnInfo(name = "icon") val icon: String?,
    @ColumnInfo(name = "status") val status: AccountStatus,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)
