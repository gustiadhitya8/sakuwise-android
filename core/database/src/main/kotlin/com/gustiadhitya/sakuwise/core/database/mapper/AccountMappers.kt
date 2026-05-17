package com.gustiadhitya.sakuwise.core.database.mapper

import com.gustiadhitya.sakuwise.core.database.entity.AccountEntity
import com.gustiadhitya.sakuwise.core.database.entity.AccountSnapshotEntity
import com.gustiadhitya.sakuwise.core.model.Account
import com.gustiadhitya.sakuwise.core.model.AccountSnapshot

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    type = type,
    initialBalance = initialBalance,
    color = color,
    icon = icon,
    status = status,
    createdAt = createdAt,
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    type = type,
    initialBalance = initialBalance,
    color = color,
    icon = icon,
    status = status,
    createdAt = createdAt,
)

fun AccountSnapshotEntity.toDomain(): AccountSnapshot = AccountSnapshot(
    id = id,
    accountId = accountId,
    snapshotDate = snapshotDate,
    observedBalance = observedBalance,
    computedBalanceAtSnapshot = computedBalanceAtSnapshot,
    adjustmentAmount = adjustmentAmount,
)

fun AccountSnapshot.toEntity(): AccountSnapshotEntity = AccountSnapshotEntity(
    id = id,
    accountId = accountId,
    snapshotDate = snapshotDate,
    observedBalance = observedBalance,
    computedBalanceAtSnapshot = computedBalanceAtSnapshot,
    adjustmentAmount = adjustmentAmount,
)
