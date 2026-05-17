package com.gustiadhitya.sakuwise.core.domain.usecase.account

import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.model.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAccountsUseCase @Inject constructor(
    private val repository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = repository.observeActive()
}
