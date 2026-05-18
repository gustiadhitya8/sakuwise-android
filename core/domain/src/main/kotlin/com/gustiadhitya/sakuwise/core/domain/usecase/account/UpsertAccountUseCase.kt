package com.gustiadhitya.sakuwise.core.domain.usecase.account

import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.model.Account

class UpsertAccountUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(account: Account): Long = repository.upsert(account)
}
