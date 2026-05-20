package com.gustiadhitya.sakuwise.debugtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gustiadhitya.sakuwise.core.crypto.PinStore
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Debug-only utility. Lets the test/adb harness skip the onboarding flow and
 * land directly on the lock-or-dashboard path so it can validate downstream
 * screens without fighting the hidden-IME PIN composer.
 *
 * Usage:
 *   adb shell am broadcast \
 *     -n com.gustiadhitya.sakuwise.debug/com.gustiadhitya.sakuwise.debugtools.DebugSkipOnboardingReceiver \
 *     -a com.gustiadhitya.sakuwise.debug.SKIP_ONBOARDING \
 *     --es nick "TestUser" --es pin "123456"
 *
 * Lives in app/src/debug — never compiled into release.
 */
class DebugSkipOnboardingReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun prefsRepo(): UserPreferencesRepository
        fun pinStore(): PinStore
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val deps = EntryPointAccessors.fromApplication(
            context.applicationContext, Deps::class.java,
        )
        val nick = intent.getStringExtra("nick") ?: "TestUser"
        val pin = intent.getStringExtra("pin") ?: "123456"
        val language = intent.getStringExtra("lang") ?: "id"

        scope.launch {
            deps.prefsRepo().completeOnboarding(
                nickname = nick, language = language, biometricEnabled = false,
            )
            val chars = pin.toCharArray()
            try {
                deps.pinStore().setPin(chars)
            } finally {
                chars.fill(' ')
            }
            Log.i(
                TAG,
                "Onboarding bypassed: nick=$nick lang=$language pinSet=${pin.length == 6}",
            )
        }
    }

    companion object {
        private const val TAG = "SwDebugSkipOnboard"
    }
}
