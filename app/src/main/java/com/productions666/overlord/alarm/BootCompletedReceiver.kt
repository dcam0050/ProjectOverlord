package com.productions666.overlord.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.productions666.overlord.data.database.OverlordDatabase
import com.productions666.overlord.data.database.entity.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant

class BootCompletedReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.TIME_SET",
            Intent.ACTION_TIMEZONE_CHANGED -> {
                Log.d(TAG, "Received ${intent.action}, rescheduling alarms")
                rescheduleAlarms(context)
            }
        }
    }

    private fun rescheduleAlarms(context: Context) {
        scope.launch {
            try {
                val db = OverlordDatabase.getDatabase(context)
                val alarmDao = db.alarmInstanceDao()
                val scheduler = AlarmScheduler(context)

                val now = Instant.now().toEpochMilli()
                val futureAlarms = alarmDao.getFutureAlarms(now)

                futureAlarms.forEach { entity ->
                    if (entity.isActive) {
                        val alarm = entity.toDomain()
                        scheduler.scheduleAlarm(alarm)
                        Log.d(TAG, "Rescheduled alarm ${alarm.id} for ${alarm.scheduledTime}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms", e)
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}

