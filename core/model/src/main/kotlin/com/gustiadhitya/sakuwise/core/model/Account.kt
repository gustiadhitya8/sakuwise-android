package com.gustiadhitya.sakuwise.core.model

import java.time.Instant

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val initialBalance: Long,
    val color: String?,
    val icon: String?,
    val status: AccountStatus,
    val createdAt: Instant,
)
