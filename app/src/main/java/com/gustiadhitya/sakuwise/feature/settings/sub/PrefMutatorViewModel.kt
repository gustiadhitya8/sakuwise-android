package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrefMutatorViewModel @Inject constructor(
    private val repo: UserPreferencesRepository,
) : ViewModel() {
    private fun fire(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
    fun setLanguage(code: String) = fire {
        repo.setLanguage(code)
        // Apply the locale at runtime so the UI actually switches — AppCompat
        // sends a config change and Compose recomposes with the new resources.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    }
    fun setBiometric(enabled: Boolean) = fire { repo.setBiometricEnabled(enabled) }
    fun setUsePassphrase(enabled: Boolean) = fire { repo.setUsePassphrase(enabled) }
    fun setCurrentCredentialIsPassphrase(isPassphrase: Boolean) =
        fire { repo.setCurrentCredentialIsPassphrase(isPassphrase) }
    fun setThemeMode(mode: String) = fire { repo.setThemeMode(mode) }
    fun setAutoLock(minutes: Int) = fire { repo.setAutoLockMinutes(minutes) }
    fun setPeriodStart(day: Int) = fire { repo.setPlanPeriodStartDay(day) }
    fun setNickname(name: String) = fire { repo.setNickname(name) }
    fun setGoldPrice(price: Long) = fire { repo.setGoldPriceGlobal(price) }
    fun setGoldPriceDigital(price: Long) = fire { repo.setGoldPriceDigital(price) }
    fun setAllocations(n: Int, w: Int, i: Int) = fire { repo.setAllocationPercentages(n, w, i) }
    fun replayOnboarding() = fire { repo.setOnboardingIncomplete() }
}
