package com.explapp.marketpulse

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class DailySummaryReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("market_pulse", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("daily_enabled", false)) return
        val summary = prefs.getString("daily_summary", null)?.takeIf { it.isNotBlank() }
            ?: "افتح مؤشر الأسواق لمشاهدة ملخص الذهب والعملات اليوم."
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = "daily_market_summary"
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel(channel, "ملخص الأسواق اليومي", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val open = PendingIntent.getActivity(
            context, 7002, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(context, channel)
        else android.app.Notification.Builder(context)
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("ملخص مؤشر الأسواق")
            .setContentText(summary)
            .setStyle(android.app.Notification.BigTextStyle().bigText(summary))
            .setContentIntent(open)
            .setAutoCancel(true)
        nm.notify(7003, builder.build())
    }
}
