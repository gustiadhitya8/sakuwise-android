package com.gustiadhitya.sakuwise.feature.asset.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.DepositAsset
import com.gustiadhitya.sakuwise.core.domain.model.GoldAsset
import com.gustiadhitya.sakuwise.core.domain.model.LandAsset
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DebtRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DepositRepository
import com.gustiadhitya.sakuwise.core.domain.repository.GoldRepository
import com.gustiadhitya.sakuwise.core.domain.repository.LandRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.ComputeNetWorthTrendUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ComputeNetWorthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AssetsHubState(
    val netWorth: ComputeNetWorthUseCase.NetWorth = ComputeNetWorthUseCase.NetWorth(0, 0, 0, 0, 0, 0),
    val accounts: List<Account> = emptyList(),
    val accountsTotal: Long = 0,
    val gold: List<GoldAsset> = emptyList(),
    val land: List<LandAsset> = emptyList(),
    val deposits: List<DepositAsset> = emptyList(),
    val netWorthTrend: List<Pair<LocalDate, Long>> = emptyList(),
    val balancesHidden: Boolean = false,
)

@HiltViewModel
class AssetsHubViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val goldRepo: GoldRepository,
    private val landRepo: LandRepository,
    private val depositRepo: DepositRepository,
    private val debtRepo: DebtRepository,
    private val computeNetWorth: ComputeNetWorthUseCase,
    private val computeNetWorthTrend: ComputeNetWorthTrendUseCase,
    private val prefsRepo: UserPreferencesRepository,
) : ViewModel() {

    private val _trend = MutableStateFlow<List<Pair<LocalDate, Long>>>(emptyList())

    init {
        // Recompute the trend any time the net-worth value changes (cheap O(N*M)
        // over ≤ 10k txns × 12 months → microseconds in practice).
        viewModelScope.launch {
            computeNetWorth().collect { nw ->
                _trend.value = computeNetWorthTrend(currentNw = nw)
            }
        }
    }

    val state: StateFlow<AssetsHubState> = combine(
        computeNetWorth(),
        accountRepo.observeActive(),
        accountRepo.observeTotalBalance(),
        goldRepo.observeAll(),
        landRepo.observeAll(),
        depositRepo.observeAll(),
        _trend,
        prefsRepo.prefs,
    ) { args ->
        val nw = args[0] as ComputeNetWorthUseCase.NetWorth
        @Suppress("UNCHECKED_CAST") val accs = args[1] as List<Account>
        val total = args[2] as Long
        @Suppress("UNCHECKED_CAST") val gold = args[3] as List<GoldAsset>
        @Suppress("UNCHECKED_CAST") val land = args[4] as List<LandAsset>
        @Suppress("UNCHECKED_CAST") val deposits = args[5] as List<DepositAsset>
        @Suppress("UNCHECKED_CAST") val trend = args[6] as List<Pair<LocalDate, Long>>
        val prefs = args[7] as com.gustiadhitya.sakuwise.core.datastore.UserPreferences
        AssetsHubState(
            netWorth = nw,
            accounts = accs,
            accountsTotal = total,
            gold = gold,
            land = land,
            deposits = deposits,
            netWorthTrend = trend,
            balancesHidden = prefs.balancesHidden,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AssetsHubState())

    fun toggleBalancesHidden() {
        viewModelScope.launch {
            val current = state.value.balancesHidden
            prefsRepo.setBalancesHidden(!current)
        }
    }
}
