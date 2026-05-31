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
import com.gustiadhitya.sakuwise.R
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
        val publicVersion = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(applicationContext.getString(R.string.reminder_notif_public_text))
            .build()
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
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

        fun uniqueName(planItemId: String) = "reminder-$planItemId"

        /**
         * Schedule a monthly reminder. `dayOfMonth` (1..28) and `hourOfDay`
         * (0..23) determine when in the cycle the user wants the prompt; the
         * worker computes how many minutes from now to the next matching
         * occurrence and uses that as `setInitialDelay`. We do NOT manually
         * fire an immediate confirmation — the caller is expected to show a
         * Toast for feedback, and PeriodicWork's own first run was happening
         * almost immediately, so the combination produced 2 notifications
         * back-to-back. The Toast handles confirmation; the worker handles
         * the actual scheduled notifications.
         */
        fun scheduleMonthly(
            ctx: Context, planItemId: String,
            title: String, body: String,
            dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int = 0,
        ) {
            val data = androidx.work.workDataOf(
                KEY_TITLE to title,
                KEY_BODY to body,
            )
            val initialDelayMin = computeInitialDelayMinutes(dayOfMonth, hourOfDay, minuteOfHour)
            val request = PeriodicWorkRequestBuilder<RecurringPaymentReminderWorker>(
                30, TimeUnit.DAYS,
            )
                .setInitialDelay(initialDelayMin, TimeUnit.MINUTES)
                .addTag(uniqueName(planItemId))
                .setInputData(data)
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                uniqueName(planItemId),
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        private fun computeInitialDelayMinutes(day: Int, hour: Int, minute: Int): Long {
            val safeDay = day.coerceIn(1, 28)
            val safeHour = hour.coerceIn(0, 23)
            val safeMin = minute.coerceIn(0, 59)
            val now = java.time.LocalDateTime.now()
            var target = now.withDayOfMonth(safeDay)
                .withHour(safeHour).withMinute(safeMin).withSecond(0).withNano(0)
            if (!target.isAfter(now)) target = target.plusMonths(1)
            return java.time.Duration.between(now, target).toMinutes().coerceAtLeast(1L)
        }

        fun cancelFor(ctx: Context, planItemId: String) {
            val wm = WorkManager.getInstance(ctx)
            wm.cancelUniqueWork(uniqueName(planItemId))
            wm.cancelAllWorkByTag(uniqueName(planItemId))
        }
    }
}
