package com.gustiadhitya.sakuwise.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.feature.lock.AppLockController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

/**
 * MainActivity — extends AppCompatActivity so:
 *  - BiometricPrompt works (AppCompatActivity IS a FragmentActivity).
 *  - AppCompatDelegate.setApplicationLocales is correctly applied to this
 *    Activity's resources/Configuration. The previous FragmentActivity base
 *    silently ignored per-app locale changes, making the language toggle a
 *    no-op until the next cold start.
 *
 * POST_NOTIFICATIONS is intentionally NOT requested on cold start — that
 * would flash a system permission dialog before the user understands what
 * the app does. Reminder-scheduling code paths request it lazily when needed.
 *
 * The custom attachBaseContext + DataStore-driven Configuration wrap was
 * removed in favour of letting AppCompat handle the locale. AppCompatDelegate
 * persists the per-app locale internally (API 33+) so cold-restart reads it
 * back automatically; for older APIs AppCompat falls back to LocaleManagerCompat.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var appLock: AppLockController

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) { appLock.onAppForegrounded() }
        override fun onStop(owner: LifecycleOwner) { appLock.onAppBackgrounded() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        setContent {
            // Resolve themeMode pref → darkTheme flag. "system" follows device,
            // "light"/"dark" force a specific mode.
            val deps = EntryPointAccessors.fromApplication(
                applicationContext, SakuwiseApplication.PrefsEntryPoint::class.java,
            )
            val prefs by deps.prefsRepo().prefs
                .collectAsState(initial = com.gustiadhitya.sakuwise.core.datastore.UserPreferences.DEFAULTS)
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val darkTheme = when (prefs.themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }
            SakuwiseTheme(darkTheme = darkTheme) {
                SakuwiseApp()
            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        super.onDestroy()
    }
}
