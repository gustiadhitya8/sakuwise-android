package com.gustiadhitya.sakuwise.feature.settings.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.ComputeCurrentPlanPeriodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Period preset the user picks in the export screen. */
enum class ExportPeriod { CurrentMonth, Last30Days, ThisYear, AllTime }

sealed interface ExportState {
    data object Idle : ExportState
    data object Working : ExportState
    data class Ready(val uri: Uri) : ExportState
    data class Failure(val message: String) : ExportState
}

@HiltViewModel
class ExportPdfViewModel @Inject constructor(
    private val exportPdf: ExportPdfUseCase,
    private val computePlanPeriod: ComputeCurrentPlanPeriodUseCase,
    private val prefsRepo: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ExportState>(ExportState.Idle)
    val state: StateFlow<ExportState> = _state.asStateFlow()

    fun export(period: ExportPeriod) {
        if (_state.value is ExportState.Working) return
        _state.value = ExportState.Working
        viewModelScope.launch {
            val (start, end) = resolveRange(period)
            val result = exportPdf(start, end)
            result.fold(
                onSuccess = { uri -> _state.value = ExportState.Ready(uri) },
                onFailure = { t ->
                    _state.value = ExportState.Failure(t.message ?: "Tidak diketahui")
                },
            )
        }
    }

    fun clear() {
        _state.value = ExportState.Idle
    }

    private suspend fun resolveRange(period: ExportPeriod): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (period) {
            ExportPeriod.CurrentMonth -> {
                val startDay = prefsRepo.prefs.first().planPeriodStartDay
                val pp = computePlanPeriod(today = today, planStartDay = startDay)
                pp.start to pp.end
            }
            ExportPeriod.Last30Days -> today.minusDays(29) to today
            ExportPeriod.ThisYear -> today.withDayOfYear(1) to today
            ExportPeriod.AllTime -> LocalDate.of(1970, 1, 1) to today
        }
    }
}
