package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gustiadhitya.sakuwise.core.model.Account
import com.gustiadhitya.sakuwise.core.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String,
    val balance: Long,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    type = AccountType.valueOf(type),
    balance = balance,
    isArchived = isArchived,
    createdAt = createdAt,
)

fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    type = type.name,
    balance = balance,
    isArchived = isArchived,
    createdAt = createdAt,
)
