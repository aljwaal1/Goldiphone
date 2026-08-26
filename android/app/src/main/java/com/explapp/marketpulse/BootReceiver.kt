package com.explapp.marketpulse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("market_pulse", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("daily_enabled", false)) return

        val hm = prefs.getString("daily_time", "20:00")!!.split(":")
        val h = hm.getOrNull(0)?.toIntOrNull() ?: 20
        val m = hm.getOrNull(1)?.toIntOrNull() ?: 0
        NotificationScheduler.scheduleNext(context, h, m)
    }
}
