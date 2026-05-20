package com.gustiadhitya.sakuwise.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.feature.lock.AppLockController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

/**
 * MainActivity — extends FragmentActivity (not ComponentActivity) so the
 * androidx.biometric BiometricPrompt can attach. Compose's setContent works
 * fine on FragmentActivity. The splash-screen API also remains compatible.
 *
 * POST_NOTIFICATIONS is intentionally NOT requested on cold start — that
 * would flash a system permission dialog before the user understands what
 * the app does. Reminder-scheduling code paths request it lazily when needed.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var appLock: AppLockController

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) { appLock.onAppForegrounded() }
        override fun onStop(owner: LifecycleOwner) { appLock.onAppBackgrounded() }
    }

    /**
     * Apply the saved language pref BEFORE Compose inflates any string resources.
     * AppCompatDelegate.setApplicationLocales is a silent no-op when called from
     * Application.onCreate on API 33+ (no Activity attached yet), so we wrap the
     * base Context here with a Configuration carrying the desired Locale.
     *
     * Hilt's @AndroidEntryPoint also overrides attachBaseContext to set up its
     * component graph — by calling super last we let Hilt see our wrapped Context.
     */
    override fun attachBaseContext(newBase: Context) {
        val deps = EntryPointAccessors.fromApplication(
            newBase.applicationContext,
            SakuwiseApplication.PrefsEntryPoint::class.java,
        )
        val lang = runCatching {
            runBlocking { deps.prefsRepo().prefs.first().language }
        }.getOrDefault("id")
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration).apply { setLocale(locale) }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        setContent {
            SakuwiseTheme {
                SakuwiseApp()
            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        super.onDestroy()
    }
}
