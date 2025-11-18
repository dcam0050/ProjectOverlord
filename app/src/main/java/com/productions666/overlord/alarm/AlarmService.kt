package com.productions666.overlord.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.productions666.overlord.R
import com.productions666.overlord.data.database.OverlordDatabase
import com.productions666.overlord.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant

class AlarmService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var autoStopHandler: Handler? = null
    private var autoStopRunnable: Runnable? = null
    private var countdownHandler: Handler? = null
    private var countdownRunnable: Runnable? = null

    private var alarmId: Long = -1
    private var profileId: Long = -1
    private var alarmLabel: String = "Alarm"
    private var alarmType: String = "WAKE_UP"
    private var requiresUserDismiss: Boolean = true
    private var autoStopAfterMillis: Long? = null

    private object ThemeColors {
        const val BACKGROUND = 0xFF0B0C10.toInt()
        const val PRIMARY = 0xFF00D4F5.toInt()
        const val SECONDARY = 0xFF9B7FFF.toInt()
        const val ON_SURFACE = 0xFFE6ECFF.toInt()
        const val ON_PRIMARY = 0xFF000000.toInt()
        const val ERROR = 0xFFCF6679.toInt()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Handle dismiss action
        if (intent.action == ACTION_DISMISS) {
            // Ensure we have the alarmId from the intent if it wasn't set yet
            val dismissAlarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
            if (dismissAlarmId > 0 && alarmId <= 0) {
                alarmId = dismissAlarmId
            }
            stopAlarm()
            return START_NOT_STICKY
        }

        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        profileId = intent.getLongExtra(AlarmScheduler.EXTRA_PROFILE_ID, -1)
        alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Alarm"
        alarmType = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TYPE) ?: "WAKE_UP"
        requiresUserDismiss = intent.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRES_USER_DISMISS, true)
        autoStopAfterMillis = intent.getLongExtra(AlarmScheduler.EXTRA_AUTO_STOP_MILLIS, -1).let {
            if (it > 0) it else null
        }

        // Safety check: if alarm should auto-stop but no duration was set, use 30 second default
        if (!requiresUserDismiss && autoStopAfterMillis == null) {
            autoStopAfterMillis = 30 * 1000L // 30 seconds in milliseconds
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        startAlarm()
        Log.d(TAG, "Alarm service started for alarm $alarmId: $alarmLabel")

        return START_NOT_STICKY
    }

    private fun startAlarm() {
        // Start vibration pattern
        startVibration()

        // Launch alarm activity
        launchAlarmActivity()

        // Start audio playback
        // TODO: Load profile and play Spotify or local sound
        // For now, play default local sound
        playLocalSound()

        // Schedule auto-stop if needed
        autoStopAfterMillis?.let { millis ->
            if (!requiresUserDismiss) {
                autoStopHandler = Handler(Looper.getMainLooper())
                autoStopRunnable = Runnable {
                    Log.d(TAG, "Auto-stopping alarm after $millis ms")
                    stopAlarm()
                }
                autoStopHandler?.postDelayed(autoStopRunnable!!, millis)
            }
        }
    }

    private fun launchAlarmActivity() {
        try {
            val intent = Intent(this, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
                putExtra(AlarmScheduler.EXTRA_ALARM_TYPE, alarmType)
                putExtra(AlarmScheduler.EXTRA_PROFILE_ID, profileId)
                putExtra(AlarmScheduler.EXTRA_REQUIRES_USER_DISMISS, requiresUserDismiss)
                autoStopAfterMillis?.let {
                    putExtra(AlarmScheduler.EXTRA_AUTO_STOP_MILLIS, it)
                }
            }
            startActivity(intent)
            Log.d(TAG, "Alarm activity launched")
        } catch (e: Exception) {
            Log.e(TAG, "Error launching alarm activity", e)
        }
    }

    // Countdown is now handled by AlarmActivity
    private fun stopCountdown() {
        countdownHandler?.removeCallbacks(countdownRunnable ?: return)
        countdownHandler = null
        countdownRunnable = null
    }

    private fun finishAlarmActivity() {
        // The AlarmActivity will finish itself when dismissed
        // No need to manually remove anything
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 500, 500, 500, 500, 500)
        val repeat = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, repeat)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, repeat)
        }
    }

    private fun playLocalSound() {
        try {
            // TODO: Load actual sound from profile
            // For now, use system default alarm sound
            // Note: android.R.raw resources vary by device, using a more common one
            val soundUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            mediaPlayer = MediaPlayer.create(this, soundUri)
            mediaPlayer?.apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
        }
    }

    fun stopAlarm() {
        // Mark alarm as inactive in database with acknowledged timestamp
        if (alarmId > 0) {
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val db = OverlordDatabase.getDatabase(this@AlarmService)
                    val acknowledgedAt = Instant.now().toEpochMilli()
                    db.alarmInstanceDao().deactivateAlarm(alarmId, acknowledgedAt)
                    Log.d(TAG, "Alarm $alarmId marked as inactive with acknowledgedAt=$acknowledgedAt")
                } catch (e: Exception) {
                    Log.e(TAG, "Error deactivating alarm in database", e)
                }
            }
        }

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()

        autoStopHandler?.removeCallbacks(autoStopRunnable ?: return)
        autoStopHandler = null
        autoStopRunnable = null

        stopCountdown()

        finishAlarmActivity()
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for transit alarms"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                // Allow full-screen intents for alarms
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Create intent for AlarmActivity
        val activityIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_ALARM_TYPE, alarmType)
            putExtra(AlarmScheduler.EXTRA_PROFILE_ID, profileId)
            putExtra(AlarmScheduler.EXTRA_REQUIRES_USER_DISMISS, requiresUserDismiss)
            autoStopAfterMillis?.let {
                putExtra(AlarmScheduler.EXTRA_AUTO_STOP_MILLIS, it)
            }
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Content intent (when notification is tapped)
        val contentIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt() + 10000,
            activityIntent,
            flags
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Active")
            .setContentText(alarmLabel)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Use MAX priority to ensure it appears on top
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_DISMISS = "com.productions666.overlord.ACTION_DISMISS"
        private const val TAG = "AlarmService"
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1
    }
}

