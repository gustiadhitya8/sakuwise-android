package com.gustiadhitya.sakuwise.feature.settings.importexport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.ComputeCurrentPlanPeriodUseCase
import com.gustiadhitya.sakuwise.feature.settings.export.ExportPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface ExportTxnState {
    data object Idle : ExportTxnState
    data object Working : ExportTxnState
    data class Ready(val uri: Uri, val count: Int, val format: ExportFormat) : ExportTxnState
    data class Failure(val message: String) : ExportTxnState
}

@HiltViewModel
class ExportTransactionViewModel @Inject constructor(
    private val exportUseCase: ExportTransactionsUseCase,
    private val computePlanPeriod: ComputeCurrentPlanPeriodUseCase,
    private val prefsRepo: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ExportTxnState>(ExportTxnState.Idle)
    val state: StateFlow<ExportTxnState> = _state.asStateFlow()

    fun export(period: ExportPeriod, format: ExportFormat) {
        if (_state.value is ExportTxnState.Working) return
        _state.value = ExportTxnState.Working
        viewModelScope.launch {
            val (start, end) = resolveRange(period)
            exportUseCase(start, end, format).fold(
                onSuccess = { (uri, count) -> _state.value = ExportTxnState.Ready(uri, count, format) },
                onFailure = { t -> _state.value = ExportTxnState.Failure(t.message ?: "Tidak diketahui") },
            )
        }
    }

    fun clear() { _state.value = ExportTxnState.Idle }

    private suspend fun resolveRange(period: ExportPeriod): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (period) {
            ExportPeriod.CurrentMonth -> {
                val startDay = prefsRepo.prefs.first().planPeriodStartDay
                val pp = computePlanPeriod(today = today, planStartDay = startDay)
                pp.start to pp.end
            }
            ExportPeriod.Last30Days -> today.minusDays(29) to today
            ExportPeriod.ThisYear   -> today.withDayOfYear(1) to today
            ExportPeriod.AllTime    -> LocalDate.of(1970, 1, 1) to today
        }
    }
}
