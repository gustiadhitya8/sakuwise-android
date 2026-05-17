package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetLanguageUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val selectedLanguage: String = "id",
    val privacyAcknowledged: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setLanguage: SetLanguageUseCase,
    private val setOnboardingCompleted: SetOnboardingCompletedUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectLanguage(code: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = code)
    }

    fun confirmLanguage(onNext: () -> Unit) {
        viewModelScope.launch {
            setLanguage(_uiState.value.selectedLanguage)
            onNext()
        }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            setOnboardingCompleted(true)
            onDone()
        }
    }
}
