package com.explapp.marketpulse

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationScheduler {
    private const val REQUEST_CODE = 7001

    fun scheduleNext(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences("market_pulse", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("daily_enabled", false)) return

        val selectedDays = prefs.getString("notify_days", "1,2,3,4,5,6,7")
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            .orEmpty()
            .ifEmpty { setOf(1,2,3,4,5,6,7) }

        val startDateText = prefs.getString("notify_start_date", null)
        val startMillis = runCatching {
            if (startDateText.isNullOrBlank()) 0L
            else SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(startDateText)?.time ?: 0L
        }.getOrDefault(0L)

        val now = System.currentTimeMillis()
        var triggerAt = 0L
        for (offset in 0..21) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            if (cal.timeInMillis > now && dayStart >= startMillis && cal.get(Calendar.DAY_OF_WEEK) in selectedDays) {
                triggerAt = cal.timeInMillis
                break
            }
        }
        if (triggerAt == 0L) return

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context)
        alarm.cancel(pi)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarm.canScheduleExactAlarms()) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarm.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, DailySummaryReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
