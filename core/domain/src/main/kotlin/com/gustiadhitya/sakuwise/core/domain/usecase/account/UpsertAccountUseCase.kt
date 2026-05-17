package com.gustiadhitya.sakuwise.core.domain.usecase.account

import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.model.Account
import javax.inject.Inject

class UpsertAccountUseCase @Inject constructor(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(account: Account) = repository.upsert(account)
}
