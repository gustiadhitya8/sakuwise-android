package com.gustiadhitya.sakuwise.app

import android.app.Application
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.EntryPoint
import kotlinx.coroutines.flow.first
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class SakuwiseApplication : Application() {
    /** Hilt entry point — used by MainActivity.attachBaseContext to read the saved language pref. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PrefsEntryPoint {
        fun prefsRepo(): UserPreferencesRepository
    }

    override fun onCreate() {
        super.onCreate()
        // Reconcile per-app locale with the DataStore-saved language pref.
        //
        // Two directions, depending on which side has "newer" truth:
        //   1. If AppCompatDelegate has a non-empty per-app locale (set by the
        //      user via Android system settings, a `cmd locale set-app-locales`
        //      adb call, or a prior in-app pick), THAT wins — copy it back into
        //      prefs so the Settings → Language row + profile sub-label stay
        //      in sync with what the UI is actually rendering.
        //   2. If AppCompatDelegate is empty (e.g. cold first launch where
        //      onboarding has already written prefs.language but the system
        //      side hasn't been touched yet), push prefs → AppCompatDelegate.
        //
        // Either way, post-reconcile both sides match. Idempotent.
        try {
            val deps = dagger.hilt.android.EntryPointAccessors.fromApplication(
                this, PrefsEntryPoint::class.java,
            )
            val saved = kotlinx.coroutines.runBlocking {
                deps.prefsRepo().prefs.first().language
            }
            // Read from BOTH sources and prefer whichever is non-empty.
            //  • AppCompatDelegate: in-app writes from this app's Settings flow.
            //  • LocaleManager (API 33+): system-level per-app locale, set via
            //    OS Settings → App info → Language, or `adb cmd locale
            //    set-app-locales`. AppCompatDelegate does NOT see this.
            val fromAppCompat = androidx.appcompat.app.AppCompatDelegate
                .getApplicationLocales().toLanguageTags()
            val fromLocaleManager = if (android.os.Build.VERSION.SDK_INT >= 33) {
                getSystemService(android.app.LocaleManager::class.java)
                    ?.applicationLocales?.toLanguageTags().orEmpty()
            } else ""
            val system = when {
                fromAppCompat.isNotEmpty() -> fromAppCompat
                else -> fromLocaleManager
            }
            when {
                system.isNotEmpty() && system != saved -> {
                    // System wins — write back to prefs so UI labels match.
                    kotlinx.coroutines.runBlocking {
                        deps.prefsRepo().setLanguage(system)
                    }
                }
                system.isEmpty() && saved.isNotEmpty() -> {
                    // Prefs wins — push to AppCompatDelegate.
                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                        androidx.core.os.LocaleListCompat.forLanguageTags(saved),
                    )
                }
            }
        } catch (_: Throwable) {
            // Swallow — if the read fails (first run before DataStore init),
            // AppCompatDelegate falls back to system locale, which is fine.
        }
        // Schedule the daily net-worth snapshot worker. ExistingPeriodicWorkPolicy.KEEP
        // means re-launch is cheap — WorkManager dedupes by unique name.
        com.gustiadhitya.sakuwise.core.work.NetWorthSnapshotWorker.scheduleDaily(this)
        // Re-arm Drive auto-backup if the user had it enabled before this cold start.
        try {
            val deps = dagger.hilt.android.EntryPointAccessors.fromApplication(
                this, PrefsEntryPoint::class.java,
            )
            val autoBackupEnabled = kotlinx.coroutines.runBlocking {
                deps.prefsRepo().prefs.first().driveBackupEnabled
            }
            if (autoBackupEnabled) {
                com.gustiadhitya.sakuwise.core.work.DriveAutoBackupWorker.scheduleDaily(this)
            }
        } catch (_: Throwable) { /* swallow — prefs not yet init'd on first install */ }
    }
}
