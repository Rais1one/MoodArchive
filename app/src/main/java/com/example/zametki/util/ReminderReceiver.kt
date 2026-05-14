package com.example.zametki.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showNotification(context)

        val hour = intent.getIntExtra("hour", 20)
        val minute = intent.getIntExtra("minute", 0)
        ReminderManager(context).setReminder(hour, minute)
    }
}