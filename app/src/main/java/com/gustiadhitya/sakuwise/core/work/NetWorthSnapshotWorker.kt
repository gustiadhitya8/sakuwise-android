package com.gustiadhitya.sakuwise.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gustiadhitya.sakuwise.core.domain.usecase.SnapshotNetWorthTodayUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

/**
 * Periodically snapshots today's net worth. Run once per day; the use case
 * itself is idempotent via the epoch-day primary key (last write wins).
 *
 * No HiltWorker dep — uses EntryPointAccessors to resolve the use case at
 * runtime to avoid pulling in androidx.hilt:hilt-work for one worker.
 */
class NetWorthSnapshotWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun snapshotNetWorth(): SnapshotNetWorthTodayUseCase
    }

    override suspend fun doWork(): Result {
        return runCatching {
            val deps = EntryPointAccessors.fromApplication(
                applicationContext, Deps::class.java,
            )
            deps.snapshotNetWorth().invoke().getOrThrow()
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        private const val UNIQUE_NAME = "sakuwise-nw-snapshot-daily"

        fun scheduleDaily(ctx: Context) {
            val request = PeriodicWorkRequestBuilder<NetWorthSnapshotWorker>(
                1, TimeUnit.DAYS,
            ).build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
