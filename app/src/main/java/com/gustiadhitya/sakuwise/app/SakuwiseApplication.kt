package com.gustiadhitya.sakuwise.app

import android.app.Application
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.EntryPoint
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
        // Schedule the daily net-worth snapshot worker. ExistingPeriodicWorkPolicy.KEEP
        // means re-launch is cheap — WorkManager dedupes by unique name.
        com.gustiadhitya.sakuwise.core.work.NetWorthSnapshotWorker.scheduleDaily(this)
    }
}
