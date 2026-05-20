package com.gustiadhitya.sakuwise.feature.lock

import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AppLockController — singleton tracking app foreground/background and
 * deciding when to show the lock screen.
 *
 * Rules (PRD §7.15 + Tech Solution §6.2 Daily Unlock):
 *  - Default auto-lock = 5 minutes.
 *  - "Langsung" (0 min) → lock immediately on background.
 *  - First launch (post-onboarding) → also locked.
 *  - Onboarding flow itself never locks.
 */
@Singleton
class AppLockController @Inject constructor(
    private val prefsRepo: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _locked = MutableStateFlow(true) // start locked
    val locked: StateFlow<Boolean> = _locked

    private var lastBackgroundedAt: Long = 0L

    /** Combined: true when the lock screen should overlay the app. */
    val shouldShowLock: StateFlow<Boolean> = combine(
        prefsRepo.prefs.map { it.onboardingCompleted },
        _locked,
    ) { onboarded, locked -> onboarded && locked }
        .stateIn(scope, SharingStarted.Eagerly, false)

    fun onAppBackgrounded() {
        lastBackgroundedAt = System.currentTimeMillis()
    }

    fun onAppForegrounded() {
        scope.launch {
            val autoLockMin = prefsRepo.prefs.first().autoLockMinutes
            val elapsedMin = (System.currentTimeMillis() - lastBackgroundedAt) / 60_000
            if (lastBackgroundedAt > 0 && elapsedMin >= autoLockMin) {
                _locked.value = true
            }
        }
    }

    fun unlock() { _locked.value = false }
    fun lock() { _locked.value = true }
}
