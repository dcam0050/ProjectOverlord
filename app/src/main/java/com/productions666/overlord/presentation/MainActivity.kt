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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.productions666.overlord.data.model.Route
import com.productions666.overlord.data.preferences.AppFont
import com.productions666.overlord.data.preferences.UserPreferencesRepository
import com.productions666.overlord.presentation.navigation.Routes
import com.productions666.overlord.presentation.screen.AlarmSetupScreen
import com.productions666.overlord.presentation.screen.AlarmTestScreen
import com.productions666.overlord.presentation.screen.FontSelectionScreen
import com.productions666.overlord.presentation.screen.HomeScreen
import com.productions666.overlord.presentation.screen.JourneyPlannerScreen
import com.productions666.overlord.presentation.screen.PermissionRequestScreen
import com.productions666.overlord.presentation.screen.ProfileEditorScreen
import com.productions666.overlord.presentation.screen.ProfileEditorViewModel
import com.productions666.overlord.presentation.screen.RouteListScreen
import com.productions666.overlord.presentation.screen.SettingsScreen
import com.productions666.overlord.presentation.screen.createSampleJourneys
import com.productions666.overlord.presentation.theme.OverlordTheme
import com.productions666.overlord.presentation.viewmodel.AlarmSetupViewModel
import com.productions666.overlord.presentation.viewmodel.JourneyPlannerViewModel
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
            // Get font preference from DataStore
            val context = LocalContext.current
            val preferencesRepository = remember { UserPreferencesRepository.getInstance(context) }
            val selectedFont by preferencesRepository.selectedFontFlow.collectAsStateWithLifecycle(
                initialValue = AppFont.LEXEND
            )
            
            OverlordTheme(selectedFont = selectedFont) {
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
                        },
                        preferencesRepository = preferencesRepository,
                        currentFont = selectedFont
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
    onPermissionUpdateRequested: (() -> Unit) -> Unit,
    preferencesRepository: UserPreferencesRepository,
    currentFont: AppFont
) {
    val context = LocalContext.current
    
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
        // All permissions granted - show main app with navigation
        AppNavigation(
            preferencesRepository = preferencesRepository,
            currentFont = currentFont
        )
    }
}

@Composable
private fun AppNavigation(
    preferencesRepository: UserPreferencesRepository,
    currentFont: AppFont
) {
    val navController = rememberNavController()
    val viewModel: JourneyPlannerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // Track current route for navigation highlighting
    val currentRoute by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
    val currentDestination = currentRoute?.destination?.route
    
    // Sample journeys for testing (will be replaced with real data from ViewModel)
    val sampleJourneys = remember { createSampleJourneys() }
    
    com.productions666.overlord.presentation.navigation.OverlordScaffold(
        title = getScreenTitle(currentDestination),
        currentRoute = currentDestination,
        onNavigate = { route ->
            // Handle sidebar navigation
            when (route) {
                "home" -> navController.navigate(route) {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
                "journeys" -> navController.navigate("journey_planner") {
                    popUpTo("home")
                    launchSingleTop = true
                }
                "settings" -> navController.navigate("settings") {
                    launchSingleTop = true
                }
                "alarm_test" -> navController.navigate("alarm_test") {
                    launchSingleTop = true
                }
                // Not yet implemented - ignore navigation
                "tasks", "notes", "account", "support" -> {
                    // TODO: Implement these screens
                }
                else -> {
                    // Fallback: ignore undefined routes
                }
            }
        },
        showTopBar = shouldShowTopBar(currentDestination)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(100)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = { fadeOut(animationSpec = tween(100)) }
        ) {
            composable("home") {
                HomeScreen(
                    upcomingJourneys = sampleJourneys,
                    onPlanNewJourney = {
                        navController.navigate("journey_planner")
                    },
                    onViewJourneyDetails = { _ ->
                        // TODO: Navigate to journey details
                    },
                    onEditJourney = { _ ->
                        // TODO: Navigate to edit journey
                    },
                    onCancelJourney = { _ ->
                        // TODO: Show cancel confirmation dialog
                    }
                )
            }
            
            composable("journey_planner") {
                JourneyPlannerScreen(
                    viewModel = viewModel,
                    onRoutesFound = { _ ->
                        // Navigate to route list when routes are found
                        navController.navigate("route_list")
                    }
                )
            }
            
            composable("route_list") {
                RouteListScreen(
                    routes = uiState.routes,
                    onRouteSelected = { route ->
                        // Store selected route and navigate to alarm setup
                        viewModel.selectRoute(route)
                        navController.navigate("alarm_setup")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("alarm_setup") {
                val alarmSetupViewModel: AlarmSetupViewModel = viewModel()
                val selectedRoute = uiState.selectedRoute
                val origin = uiState.origin
                val destination = uiState.destination
                
                if (selectedRoute != null && origin != null && destination != null) {
                    AlarmSetupScreen(
                        viewModel = alarmSetupViewModel,
                        route = selectedRoute,
                        origin = origin,
                        destination = destination,
                        onSchedulingComplete = { journeyId ->
                            // Navigate to home and clear backstack
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            // Clear the selected route
                            viewModel.clearSelectedRoute()
                            viewModel.clearRoutes()
                        },
                        onCreateProfile = {
                            navController.navigate("create_profile")
                        },
                        onEditProfile = { profileId ->
                            navController.navigate("profile_editor/$profileId")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            
            composable("create_profile") {
                val profileEditorViewModel: ProfileEditorViewModel = viewModel()
                ProfileEditorScreen(
                    viewModel = profileEditorViewModel,
                    existingProfileId = null,
                    onSaveComplete = { profileId ->
                        // Go back to alarm setup - the profile will auto-refresh
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = "profile_editor/{profileId}",
                arguments = listOf(navArgument("profileId") { type = NavType.LongType })
            ) { backStackEntry ->
                val profileId = backStackEntry.arguments?.getLong("profileId") ?: return@composable
                val profileEditorViewModel: ProfileEditorViewModel = viewModel()
                ProfileEditorScreen(
                    viewModel = profileEditorViewModel,
                    existingProfileId = profileId,
                    onSaveComplete = { _ ->
                        // Go back after saving
                        navController.popBackStack()
                    },
                    onDelete = {
                        // Go back after deleting
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    currentFont = currentFont,
                    onFontSelectionClick = {
                        navController.navigate(Routes.FONT_SELECTION)
                    },
                    onAlarmTestClick = {
                        navController.navigate("alarm_test")
                    }
                )
            }
            
            composable(Routes.FONT_SELECTION) {
                FontSelectionScreen(
                    currentFont = currentFont,
                    onFontSelected = { font ->
                        scope.launch {
                            preferencesRepository.setSelectedFont(font)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("alarm_test") {
                AlarmTestScreen()
            }
        }
    }
}

private fun getScreenTitle(route: String?): String {
    return when (route) {
        "home" -> "Overlord"
        "journey_planner", "journeys" -> "Journey Planner"
        "route_list" -> "Select Route"
        "alarm_setup" -> "Set Your Alarms"
        "create_profile" -> "New Profile"
        "settings" -> "Settings"
        Routes.FONT_SELECTION -> "Fonts"
        "alarm_test" -> "Alarm Test"
        "tasks" -> "Tasks"
        "notes" -> "Notes"
        else -> "Overlord"
    }
}

private fun shouldShowTopBar(route: String?): Boolean {
    return true // Always show for now
}
