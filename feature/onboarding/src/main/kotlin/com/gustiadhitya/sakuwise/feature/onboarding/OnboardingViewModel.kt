package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.CompleteOnboardingUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetLanguageUseCase
import com.gustiadhitya.sakuwise.core.model.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val selectedLanguage: String = "id",
    val nickname: String = "",
    val pin: String = "",
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val accountName: String = "Tunai",
    val accountType: AccountType = AccountType.CASH,
    val initialBalance: Long = 0L,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setLanguage: SetLanguageUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase,
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

    fun setNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(nickname = nickname.take(30))
    }

    fun setPin(pin: String) {
        _uiState.value = _uiState.value.copy(pin = pin.take(6))
    }

    fun setBiometricAvailable(available: Boolean) {
        _uiState.value = _uiState.value.copy(biometricAvailable = available)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
    }

    fun setAccountName(name: String) {
        _uiState.value = _uiState.value.copy(accountName = name)
    }

    fun setAccountType(type: AccountType) {
        _uiState.value = _uiState.value.copy(accountType = type)
    }

    fun setInitialBalance(balance: Long) {
        _uiState.value = _uiState.value.copy(initialBalance = balance)
    }

    fun confirmIdentity(onNext: () -> Unit) {
        onNext()
    }

    fun finishOnboarding(onDone: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            completeOnboarding(
                languageCode = state.selectedLanguage,
                nickname = state.nickname,
                biometricEnabled = state.biometricEnabled,
                devicePin = state.pin,
                accountName = state.accountName.ifBlank { "Tunai" },
                accountType = state.accountType,
                initialBalance = state.initialBalance,
            )
            onDone()
        }
    }
}
