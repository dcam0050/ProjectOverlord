package com.productions666.overlord.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.productions666.overlord.presentation.screen.AlarmTestScreen
import com.productions666.overlord.presentation.screen.PermissionRequestScreen
import com.productions666.overlord.presentation.theme.OverlordTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var permissionUpdateCallback: (() -> Unit)? = null
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Update permission state after a short delay
        lifecycleScope.launch {
            delay(100)
            permissionUpdateCallback?.invoke()
        }
    }
    
    private val exactAlarmSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Update permission state after returning from settings
        lifecycleScope.launch {
            delay(300)
            permissionUpdateCallback?.invoke()
        }
    }
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Update permission state after returning from settings
        lifecycleScope.launch {
            delay(300)
            permissionUpdateCallback?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            OverlordTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(
                        notificationPermissionLauncher = notificationPermissionLauncher,
                        exactAlarmSettingsLauncher = exactAlarmSettingsLauncher,
                        overlayPermissionLauncher = overlayPermissionLauncher,
                        openExactAlarmSettings = { openExactAlarmSettings() },
                        openFullScreenIntentSettings = { openFullScreenIntentSettings() },
                        openOverlaySettings = { openOverlaySettings() },
                        onPermissionUpdateRequested = {
                            permissionUpdateCallback = it
                        }
                    )
                }
            }
        }
    }
    
    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
            exactAlarmSettingsLauncher.launch(intent)
        } else {
            // For older versions, open app settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
    
    private fun openFullScreenIntentSettings() {
        if (Build.VERSION.SDK_INT >= 34) { // Android 14 (API 34)
            // Android 14+ - open full-screen intent settings
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to app settings if specific intent doesn't exist
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(fallbackIntent)
            }
        } else {
            // On older versions, this permission is granted automatically
            // But we can still open app settings as a fallback
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
    
    private fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            overlayPermissionLauncher.launch(intent)
        }
    }
}

@Composable
private fun MainContent(
    notificationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    exactAlarmSettingsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    overlayPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    openExactAlarmSettings: () -> Unit,
    openFullScreenIntentSettings: () -> Unit,
    openOverlaySettings: () -> Unit,
    onPermissionUpdateRequested: (() -> Unit) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var hasNotificationPermission by remember {
        mutableStateOf(PermissionHandler.hasNotificationPermission(context))
    }
    var hasExactAlarmPermission by remember {
        mutableStateOf(PermissionHandler.hasExactAlarmPermission(context))
    }
    var hasFullScreenIntentPermission by remember {
        mutableStateOf(PermissionHandler.hasFullScreenIntentPermission(context))
    }
    var hasOverlayPermission by remember {
        mutableStateOf(PermissionHandler.hasOverlayPermission(context))
    }
    
    fun updatePermissions() {
        val newNotificationPermission = PermissionHandler.hasNotificationPermission(context)
        val newExactAlarmPermission = PermissionHandler.hasExactAlarmPermission(context)
        val newFullScreenIntentPermission = PermissionHandler.hasFullScreenIntentPermission(context)
        val newOverlayPermission = PermissionHandler.hasOverlayPermission(context)
        
        android.util.Log.d("MainActivity", "Permission check - Notification: $newNotificationPermission, ExactAlarm: $newExactAlarmPermission, FullScreenIntent: $newFullScreenIntentPermission, Overlay: $newOverlayPermission")
        
        hasNotificationPermission = newNotificationPermission
        hasExactAlarmPermission = newExactAlarmPermission
        hasFullScreenIntentPermission = newFullScreenIntentPermission
        hasOverlayPermission = newOverlayPermission
    }
    
    // Register callback for permission updates
    androidx.compose.runtime.LaunchedEffect(Unit) {
        onPermissionUpdateRequested { updatePermissions() }
        updatePermissions()
    }
    
    // Re-check permissions when activity resumes (e.g., returning from settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updatePermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Show permission screen if any permission is missing
    val allPermissionsGranted = hasNotificationPermission && hasExactAlarmPermission && hasFullScreenIntentPermission && hasOverlayPermission
    
    if (!allPermissionsGranted) {
        PermissionRequestScreen(
            hasNotificationPermission = hasNotificationPermission,
            hasExactAlarmPermission = hasExactAlarmPermission,
            hasFullScreenIntentPermission = hasFullScreenIntentPermission,
            hasOverlayPermission = hasOverlayPermission,
            onRequestNotificationPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // On older versions, permission is granted by default
                    updatePermissions()
                }
            },
            onRequestExactAlarmPermission = {
                openExactAlarmSettings()
            },
            onRequestFullScreenIntentPermission = {
                openFullScreenIntentSettings()
            },
            onRequestOverlayPermission = {
                openOverlaySettings()
            },
            onContinueAnyway = {
                // Re-check permissions before continuing
                updatePermissions()
                // If still not granted, this won't proceed (button should be disabled anyway)
            }
        )
    } else {
        // All permissions granted - show main app
        AlarmTestScreen()
    }
}

