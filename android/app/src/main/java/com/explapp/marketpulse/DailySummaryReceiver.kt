package com.explapp.marketpulse

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import org.json.JSONArray

class DailySummaryReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("market_pulse", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("daily_enabled", false)) return

        val selected = prefs.getString("notify_items", "gold,silver,bitcoin")
            ?.split(",")?.filter { it.isNotBlank() }?.toSet().orEmpty()
        val snapshot = prefs.getString("market_snapshot", null)
        val base = prefs.getString("base_currency", "USD") ?: "USD"

        val summary = buildSummary(snapshot, selected)
            ?: prefs.getString("daily_summary", null)?.takeIf { it.isNotBlank() }
            ?: "افتح مؤشر الأسواق لمشاهدة الأسعار التي اخترتها."

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_market_summary_high"
        if (Build.VERSION.SDK_INT >= 26) {
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            val channel = NotificationChannel(
                channelId,
                "إشعارات الأسواق المختارة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات الأسعار التي يحددها المستخدم وفي الوقت الذي يختاره"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 120, 250)
                setSound(sound, attrs)
                setShowBadge(true)
            }
            nm.createNotificationChannel(channel)
        }

        val open = PendingIntent.getActivity(
            context, 7002,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(context, channelId)
        else android.app.Notification.Builder(context)

        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📊 أسعارك المختارة • $base")
            .setContentText(summary.lineSequence().firstOrNull() ?: summary)
            .setStyle(android.app.Notification.BigTextStyle().bigText(summary))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(android.app.Notification.CATEGORY_REMINDER)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setDefaults(android.app.Notification.DEFAULT_SOUND or android.app.Notification.DEFAULT_VIBRATE)
            .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)

        nm.notify(7003, builder.build())

        if (Build.VERSION.SDK_INT < 26) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0,250,120,250), -1))
            else @Suppress("DEPRECATION") vibrator.vibrate(longArrayOf(0,250,120,250), -1)
        }

        val hm = prefs.getString("daily_time", "20:00")!!.split(":")
        val h = hm.getOrNull(0)?.toIntOrNull() ?: 20
        val m = hm.getOrNull(1)?.toIntOrNull() ?: 0
        NotificationScheduler.scheduleNext(context, h, m)
    }

    private fun buildSummary(snapshot: String?, selected: Set<String>): String? {
        if (snapshot.isNullOrBlank()) return null
        return runCatching {
            val arr = JSONArray(snapshot)
            val includeAll = "all" in selected
            val lines = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val key = o.optString("key")
                if (!includeAll && key !in selected) continue
                val name = o.optString("name", key)
                val price = o.optString("price")
                val change = o.optString("change")
                if (price.isBlank()) continue
                lines += buildString {
                    append(name).append(": ").append(price)
                    if (change.isNotBlank() && change != "—") append("  ").append(change)
                }
            }
            lines.takeIf { it.isNotEmpty() }?.joinToString("\n")
        }.getOrNull()
    }
}
