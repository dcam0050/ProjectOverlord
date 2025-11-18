package com.productions666.overlord.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.productions666.overlord.data.database.OverlordDatabase
import com.productions666.overlord.data.model.AlarmType
import com.productions666.overlord.domain.model.AlarmInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: AlarmInstance) {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = ACTION_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_ALARM_TYPE, alarm.type.name)
            putExtra(EXTRA_PROFILE_ID, alarm.profileId)
            putExtra(EXTRA_REQUIRES_USER_DISMISS, alarm.requiresUserDismiss)
            alarm.autoStopAfterMillis?.let {
                putExtra(EXTRA_AUTO_STOP_MILLIS, it)
            }
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, alarm.id.toInt(), intent, flags)

        val triggerAtMillis = alarm.scheduledTime.toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm if permission not granted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = ACTION_ALARM
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            flags
        )

        alarmManager.cancel(pendingIntent)

        // Mark alarm as cancelled in database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = OverlordDatabase.getDatabase(context)
                val cancelledAt = Instant.now().toEpochMilli()
                db.alarmInstanceDao().cancelAlarm(alarmId, cancelledAt)
            } catch (e: Exception) {
                // Log error but don't crash - cancelling system alarm is more important
                android.util.Log.e("AlarmScheduler", "Error marking alarm as cancelled in database", e)
            }
        }
    }

    companion object {
        const val ACTION_ALARM = "com.productions666.overlord.ACTION_ALARM"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_ALARM_TYPE = "alarm_type"
        const val EXTRA_PROFILE_ID = "profile_id"
        const val EXTRA_REQUIRES_USER_DISMISS = "requires_user_dismiss"
        const val EXTRA_AUTO_STOP_MILLIS = "auto_stop_millis"
    }
}

