package com.productions666.overlord.presentation

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHandler {
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission granted by default on older versions
        }
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val hasPermission = alarmManager.canScheduleExactAlarms()
            android.util.Log.d("PermissionHandler", "Exact alarm permission check (Android ${Build.VERSION.SDK_INT}): $hasPermission")
            hasPermission
        } else {
            android.util.Log.d("PermissionHandler", "Exact alarm permission: granted by default (Android ${Build.VERSION.SDK_INT} < 31)")
            true // Permission granted by default on older versions
        }
    }

    fun hasFullScreenIntentPermission(context: Context): Boolean {
        // On Android 14+ (API 34+), USE_FULL_SCREEN_INTENT must be explicitly granted
        // On Android 12-13, it's granted automatically if SCHEDULE_EXACT_ALARM is granted
        return if (Build.VERSION.SDK_INT >= 34) { // Android 14 (API 34)
            // Android 14+ - check using NotificationManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val hasPermission = notificationManager.canUseFullScreenIntent()
            android.util.Log.d("PermissionHandler", "Full-screen intent permission check (Android ${Build.VERSION.SDK_INT}): $hasPermission")
            hasPermission
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12-13 - granted if exact alarm permission is granted
            val exactAlarmGranted = hasExactAlarmPermission(context)
            android.util.Log.d("PermissionHandler", "Full-screen intent permission (Android ${Build.VERSION.SDK_INT}): follows exact alarm = $exactAlarmGranted")
            exactAlarmGranted
        } else {
            // Android 11 and below - granted by default
            android.util.Log.d("PermissionHandler", "Full-screen intent permission: granted by default (Android ${Build.VERSION.SDK_INT} < 31)")
            true
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return permissions
    }
}

