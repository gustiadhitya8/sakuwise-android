package com.gustiadhitya.sakuwise.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.datastore.UserPreferences
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    prefsRepo: UserPreferencesRepository,
) : ViewModel() {
    val prefs: StateFlow<UserPreferences> = prefsRepo.prefs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences.DEFAULTS,
    )
}
