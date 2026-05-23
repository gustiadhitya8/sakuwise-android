package com.gustiadhitya.sakuwise.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gustiadhitya.sakuwise.core.cloud.GoogleDriveBackup
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Uploads the most-recent local .sakuwise backup to Google Drive once per day,
 * provided the user has enabled auto-backup and is signed in to Drive.
 *
 * A local backup must already exist — this worker only syncs it to Drive.
 * If no local backup is present the job completes silently (no-op).
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
    }

    override suspend fun doWork(): Result {
        return runCatching {
            val deps = EntryPointAccessors.fromApplication(applicationContext, Deps::class.java)
            val prefs = deps.prefsRepo().prefs.first()

            // Only run when auto-backup is enabled and user is signed in
            if (!prefs.driveBackupEnabled || prefs.driveAccountEmail == null) return Result.success()

            val dir = File(applicationContext.getExternalFilesDir(null), "backups")
            val latest = dir.listFiles { f -> f.isFile && f.name.endsWith(".sakuwise") }
                ?.maxByOrNull { it.lastModified() }
                ?: return Result.success()  // No local backup yet — nothing to sync

            deps.drive().upload(latest, latest.name).fold(
                onSuccess = {
                    deps.prefsRepo().markDriveBackupNow(System.currentTimeMillis())
                },
                onFailure = { return Result.retry() },
            )
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        private const val UNIQUE_NAME = "sakuwise-drive-autobackup-daily"

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
