package com.gustiadhitya.sakuwise.core.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Worker that fires a "recurring payment due" notification. Content is
 * pre-computed at scheduling time and stored in the WorkRequest's input data —
 * per Tech Solution §10.3, this lets us notify while the DB is locked.
 */
class RecurringPaymentReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()
        ensureChannel(applicationContext)
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()
        // Notification permission is checked at scheduling time, not here.
        NotificationManagerCompat.from(applicationContext).notify(System.currentTimeMillis().toInt(), notif)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "sakuwise_reminder"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"

        private fun ensureChannel(ctx: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat Sakuwise",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Pengingat pembayaran berulang."
                }
                mgr.createNotificationChannel(ch)
            }
        }

        private fun uniqueName(planItemId: String) = "reminder-$planItemId"

        fun scheduleMonthly(ctx: Context, planItemId: String, title: String, body: String) {
            val data = androidx.work.workDataOf(
                KEY_TITLE to title,
                KEY_BODY to body,
            )
            val request = PeriodicWorkRequestBuilder<RecurringPaymentReminderWorker>(
                30, TimeUnit.DAYS,
            ).addTag(uniqueName(planItemId)).setInputData(data).build()
            // Replace any prior schedule for the same plan item — prevents
            // duplicate periodic work stacking up across taps.
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                uniqueName(planItemId),
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
            // Immediate confirmation notification so the user can see the
            // pipeline actually works (periodic fires only after ~30 days).
            ensureChannel(ctx)
            val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .build()
            try {
                NotificationManagerCompat.from(ctx).notify(planItemId.hashCode(), notif)
            } catch (_: SecurityException) {
                // POST_NOTIFICATIONS revoked between check and call — ignore.
            }
        }

        fun cancelFor(ctx: Context, planItemId: String) {
            val wm = WorkManager.getInstance(ctx)
            wm.cancelUniqueWork(uniqueName(planItemId))
            wm.cancelAllWorkByTag(uniqueName(planItemId))
        }
    }
}
