package com.productions666.overlord.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmScheduler.ACTION_ALARM) {
            val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
            val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Unknown"
            Log.d(TAG, "Alarm received: ID=$alarmId, Label=$alarmLabel")

            val alarmIntent = Intent(context, AlarmService::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1))
                putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL))
                putExtra(AlarmScheduler.EXTRA_ALARM_TYPE, intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TYPE))
                putExtra(AlarmScheduler.EXTRA_PROFILE_ID, intent.getLongExtra(AlarmScheduler.EXTRA_PROFILE_ID, -1))
                putExtra(AlarmScheduler.EXTRA_REQUIRES_USER_DISMISS, intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRES_USER_DISMISS, true))
                intent.getLongExtra(AlarmScheduler.EXTRA_AUTO_STOP_MILLIS, -1).let {
                    if (it > 0) {
                        putExtra(AlarmScheduler.EXTRA_AUTO_STOP_MILLIS, it)
                    }
                }
            }

            // Start foreground service
            // The service will show a full-screen overlay and create an ongoing notification
            ContextCompat.startForegroundService(context, alarmIntent)
        }
    }

    companion object {
        private const val TAG = "AlarmBroadcastReceiver"
    }
}

