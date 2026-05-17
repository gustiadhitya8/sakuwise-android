package com.gustiadhitya.sakuwise

import android.app.Application
import com.gustiadhitya.sakuwise.core.crypto.KeyManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SakuwiseApplication : Application() {

    @Inject
    lateinit var keyManager: KeyManager

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        keyManager.setupKeyOnFirstLaunch()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
