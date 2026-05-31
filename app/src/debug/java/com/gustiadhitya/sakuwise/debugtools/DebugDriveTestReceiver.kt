package com.gustiadhitya.sakuwise.debugtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.work.DriveAutoBackupWorker
import com.gustiadhitya.sakuwise.feature.lock.AppLockController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

/**
 * Debug-only receiver to set up Drive backup test state without needing
 * to go through the UI (no Google account required on emulator).
 *
 * Usage:
 *   # Set drive prefs and create a fake local backup file:
 *   adb shell am broadcast \
 *     -n com.gustiadhitya.sakuwise.debug/com.gustiadhitya.sakuwise.debugtools.DebugDriveTestReceiver \
 *     -a com.gustiadhitya.sakuwise.debug.SETUP_DRIVE_TEST \
 *     --es email "test@test.com"
 *
 * Lives in app/src/debug — never compiled into release.
 */
class DebugDriveTestReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun prefsRepo(): UserPreferencesRepository
        fun appLockController(): AppLockController
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val deps = EntryPointAccessors.fromApplication(
            context.applicationContext, Deps::class.java,
        )
        val email = intent.getStringExtra("email") ?: "test@test.com"

        // Unlock the app immediately (on the main thread — AppLockController is safe to call from any thread)
        deps.appLockController().unlock()

        scope.launch {
            // 1. Enable Drive backup + set fake account email
            deps.prefsRepo().setDriveBackupEnabled(true)
            deps.prefsRepo().setDriveAccountEmail(email)

            // 2. Create a fake .sakuwise file in the backup dir so the worker has something to find
            val backupDir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
            val fakeFile = File(backupDir, "sakuwise-backup-debug-test.sakuwise")
            fakeFile.writeText("DEBUG_FAKE_BACKUP_PAYLOAD")

            // 3. Schedule the daily worker (normally done by BackupViewModel.setDriveAutoBackupEnabled)
            DriveAutoBackupWorker.scheduleDaily(context.applicationContext)

            Log.i(TAG, "Drive test state set: driveEnabled=true email=$email fakeFile=${fakeFile.absolutePath} exists=${fakeFile.exists()}")
        }
    }

    companion object {
        private const val TAG = "SwDebugDriveTest"
    }
}
