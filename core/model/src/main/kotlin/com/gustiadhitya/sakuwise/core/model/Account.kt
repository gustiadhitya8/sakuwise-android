package com.gustiadhitya.sakuwise.core.model

data class Account(
    val id: Long = 0L,
    val name: String,
    val type: AccountType,
    val balance: Long,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
