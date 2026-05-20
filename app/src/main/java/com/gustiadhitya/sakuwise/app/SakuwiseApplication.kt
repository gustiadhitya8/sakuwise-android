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
        // Sync AppCompatDelegate's per-app locale with the DataStore-saved
        // language pref on cold start. AppCompatDelegate persists its own
        // locale (API 33+) but for the FIRST cold launch after pref change —
        // or any case where DataStore disagrees with system locale — this
        // ensures the displayed UI matches what the user picked. Idempotent.
        try {
            val deps = dagger.hilt.android.EntryPointAccessors.fromApplication(
                this, PrefsEntryPoint::class.java,
            )
            val saved = kotlinx.coroutines.runBlocking {
                deps.prefsRepo().prefs.first().language
            }
            val current = androidx.appcompat.app.AppCompatDelegate
                .getApplicationLocales().toLanguageTags()
            if (current != saved) {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    androidx.core.os.LocaleListCompat.forLanguageTags(saved),
                )
            }
        } catch (_: Throwable) {
            // Swallow — if the read fails (first run before DataStore init),
            // AppCompatDelegate falls back to system locale, which is fine.
        }
        // Schedule the daily net-worth snapshot worker. ExistingPeriodicWorkPolicy.KEEP
        // means re-launch is cheap — WorkManager dedupes by unique name.
        com.gustiadhitya.sakuwise.core.work.NetWorthSnapshotWorker.scheduleDaily(this)
    }
}
