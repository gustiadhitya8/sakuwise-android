package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.GetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SplashDestination {
    data object None : SplashDestination
    data object Onboarding : SplashDestination
    data object Home : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    getOnboardingCompleted: GetOnboardingCompletedUseCase,
) : ViewModel() {

    val destination: StateFlow<SplashDestination> = getOnboardingCompleted()
        .map { completed ->
            if (completed) SplashDestination.Home else SplashDestination.Onboarding
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SplashDestination.None,
        )
}
