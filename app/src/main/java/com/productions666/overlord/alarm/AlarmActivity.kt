package com.productions666.overlord.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AlarmActivity : ComponentActivity() {
    private var alarmId: Long = -1
    private var alarmLabel: String = ""
    private var alarmType: String = ""
    private var requiresUserDismiss: Boolean = true
    private var autoStopAfterMillis: Long? = null
    private var countdownHandler: Handler? = null
    private var countdownRunnable: Runnable? = null

    private object ThemeColors {
        const val BACKGROUND = 0xFF0B0C10.toInt()
        const val PRIMARY = 0xFF00D4F5.toInt()
        const val SECONDARY = 0xFF9B7FFF.toInt()
        const val ON_SURFACE = 0xFFE6ECFF.toInt()
        const val ON_PRIMARY = 0xFF000000.toInt()
        const val ERROR = 0xFFCF6679.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get alarm data from intent
        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
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

        // Configure window for lock screen display
        configureWindowForLockScreen()

        // Create and show the alarm overlay UI
        showAlarmOverlay()
    }

    private fun configureWindowForLockScreen() {
        // Make activity full-screen and wake device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        // Hide system bars and make full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Set window flags to ensure it appears on top of everything
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
        )

        // Make background transparent
        window.decorView.setBackgroundColor(Color.TRANSPARENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showAlarmOverlay() {
        // Helper to convert dp to px
        fun dpToPx(dp: Float): Int = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        ).toInt()

        // Main layout
        val layout = FrameLayout(this).apply {
            setBackgroundColor(ThemeColors.BACKGROUND)
            keepScreenOn = true
        }

        // Content Container
        val contentContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            setPadding(dpToPx(32f), dpToPx(32f), dpToPx(32f), dpToPx(32f))
        }

        // Alarm Label
        val labelView = TextView(this).apply {
            text = alarmLabel
            textSize = 32f // sp
            setTextColor(ThemeColors.ON_SURFACE)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, dpToPx(16f))
        }
        contentContainer.addView(labelView)

        // Alarm Type
        val typeView = TextView(this).apply {
            text = alarmType.replace("_", " ")
            textSize = 18f
            setTextColor(ThemeColors.PRIMARY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(16f))
            letterSpacing = 0.1f
            isAllCaps = true
        }
        contentContainer.addView(typeView)

        // Countdown TextView (only if auto-stop is enabled)
        var countdownTextView: TextView? = null
        if (autoStopAfterMillis != null) {
            countdownTextView = TextView(this).apply {
                text = "Auto-stopping in ${autoStopAfterMillis!! / 1000} seconds"
                textSize = 18f
                val colorWithAlpha = Color.argb(
                    (255 * 0.6).toInt(),
                    Color.red(ThemeColors.ON_SURFACE),
                    Color.green(ThemeColors.ON_SURFACE),
                    Color.blue(ThemeColors.ON_SURFACE)
                )
                setTextColor(colorWithAlpha)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dpToPx(32f))
            }
            contentContainer.addView(countdownTextView)

            // Start countdown updates
            startCountdown(countdownTextView)
        } else {
            // Add spacing if no countdown
            val spacer = View(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(32f)
                )
            }
            contentContainer.addView(spacer)
        }

        // Buttons Container
        val buttonsContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Dismiss Button
        val dismissButton = Button(this).apply {
            text = "DISMISS"
            textSize = 16f
            setTextColor(ThemeColors.ON_PRIMARY)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(24f).toFloat() // Pill shape
                setColor(ThemeColors.ERROR) // Using Error color for Dismiss action
            }
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(56f)
            ).apply {
                bottomMargin = dpToPx(16f)
            }
            elevation = dpToPx(4f).toFloat()
            stateListAnimator = null // Disable default shadow behavior
            setOnClickListener {
                dismissAlarm()
            }
        }
        buttonsContainer.addView(dismissButton)

        // Snooze Button (only if user dismiss is required)
        if (requiresUserDismiss) {
            val snoozeButton = Button(this).apply {
                text = "SNOOZE"
                textSize = 16f
                setTextColor(ThemeColors.ON_PRIMARY)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(24f).toFloat() // Pill shape
                    setColor(ThemeColors.SECONDARY)
                }
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(56f)
                )
                elevation = dpToPx(4f).toFloat()
                stateListAnimator = null
                setOnClickListener {
                    snoozeAlarm()
                }
            }
            buttonsContainer.addView(snoozeButton)
        }

        contentContainer.addView(buttonsContainer)
        layout.addView(contentContainer)

        setContentView(layout)
    }

    private fun startCountdown(countdownTextView: TextView) {
        if (autoStopAfterMillis == null) return

        var remainingSeconds = (autoStopAfterMillis!! / 1000).toInt()
        countdownHandler = Handler(Looper.getMainLooper())
        countdownRunnable = object : Runnable {
            override fun run() {
                if (remainingSeconds > 0) {
                    countdownTextView.text = "Auto-stopping in $remainingSeconds seconds"
                    remainingSeconds--
                    countdownHandler?.postDelayed(this, 1000)
                } else {
                    countdownTextView.text = "Auto-stopping now..."
                    // The AlarmService will handle stopping the alarm
                    // We can finish this activity after a short delay
                    countdownHandler?.postDelayed({
                        finish()
                    }, 1000)
                }
            }
        }
        countdownHandler?.post(countdownRunnable!!)
    }

    private fun stopCountdown() {
        countdownHandler?.removeCallbacks(countdownRunnable ?: return)
        countdownHandler = null
        countdownRunnable = null
    }

    private fun dismissAlarm() {
        // Send dismiss action to AlarmService
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_DISMISS
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        startService(intent)
        finish()
    }

    private fun snoozeAlarm() {
        // TODO: Implement snooze functionality
        dismissAlarm()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCountdown()
        // Ensure we clear any lock screen flags when destroyed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        }
    }
}
