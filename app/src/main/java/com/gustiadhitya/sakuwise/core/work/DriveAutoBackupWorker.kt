package com.gustiadhitya.sakuwise.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gustiadhitya.sakuwise.core.cloud.GoogleDriveBackup
import com.gustiadhitya.sakuwise.core.crypto.AutoBackupPinStorage
import com.gustiadhitya.sakuwise.core.crypto.BackupService
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Creates a fresh encrypted .sakuwise backup directly from the live DB and
 * uploads it to Google Drive once per day.
 *
 * This worker is self-contained — it does NOT rely on a pre-existing local
 * backup file. The backup PIN is loaded from [AutoBackupPinStorage] (stored
 * encrypted in Android Keystore when the user first enables auto-backup).
 *
 * Pre-conditions for the worker to run:
 *   1. User enabled auto-backup (driveBackupEnabled = true)
 *   2. User is signed in to Drive (driveAccountEmail != null)
 *   3. A backup PIN has been stored ([AutoBackupPinStorage.hasPin])
 *
 * If any condition is not met the job completes silently without retry so
 * WorkManager does not spam the user with errors.
 *
 * After a successful upload:
 *   - [UserPreferencesRepository.markDriveBackupNow] is updated (Drive ts)
 *   - [UserPreferencesRepository.markBackupNow] is updated (local ts)
 *   - Local backup directory is pruned to the most-recent [MAX_LOCAL_BACKUPS] files
 */
class DriveAutoBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun drive(): GoogleDriveBackup
        fun prefsRepo(): UserPreferencesRepository
        fun backupService(): BackupService
        fun pinStorage(): AutoBackupPinStorage
    }

    override suspend fun doWork(): Result {
        return runCatching {
            val deps = EntryPointAccessors.fromApplication(applicationContext, Deps::class.java)
            val prefs = deps.prefsRepo().prefs.first()

            // Only run when auto-backup is enabled and user is signed in.
            if (!prefs.driveBackupEnabled || prefs.driveAccountEmail == null) return Result.success()

            // Load PIN — if missing (user re-installed / wiped), skip without retry.
            // The user needs to re-enable auto-backup which will prompt for a new PIN.
            val pin = deps.pinStorage().loadPin() ?: return Result.success()

            try {
                // Create a fresh encrypted backup from the live DB.
                val localDir = File(applicationContext.getExternalFilesDir(null), "backups")
                val backupFile = deps.backupService().backup(pin, localDir)

                // Keep only the most recent backups locally.
                pruneLocalBackups(localDir)

                // Upload to Drive.
                deps.drive().upload(backupFile, backupFile.name).fold(
                    onSuccess = {
                        val now = System.currentTimeMillis()
                        deps.prefsRepo().markDriveBackupNow(now)
                        deps.prefsRepo().markBackupNow(now)
                    },
                    onFailure = { return Result.retry() },
                )
                Result.success()
            } finally {
                pin.fill(' ')
            }
        }.getOrElse { Result.retry() }
    }

    /** Delete all but the [MAX_LOCAL_BACKUPS] most-recently-modified .sakuwise files. */
    private fun pruneLocalBackups(dir: File) {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".sakuwise") }
            ?.sortedByDescending { it.lastModified() } ?: return
        files.drop(MAX_LOCAL_BACKUPS).forEach { it.delete() }
    }

    companion object {
        private const val UNIQUE_NAME = "sakuwise-drive-autobackup-daily"
        private const val MAX_LOCAL_BACKUPS = 3

        fun scheduleDaily(ctx: Context) {
            val request = PeriodicWorkRequestBuilder<DriveAutoBackupWorker>(
                1, TimeUnit.DAYS,
            ).build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancel(ctx: Context) {
            WorkManager.getInstance(ctx).cancelUniqueWork(UNIQUE_NAME)
        }
    }
}
