package com.productions666.overlord.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Drawer Menu Items
 * 
 * All navigation is now in the sidebar (no bottom nav)
 * Organized into:
 * - Main tools (Home, Journeys, Tasks, Notes)
 * - App sections (Settings, Account, etc.)
 */
enum class DrawerMenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    val isMainSection: Boolean = false  // True for main tools, false for secondary
) {
    // =========================================================================
    // MAIN TOOLS
    // =========================================================================
    HOME(
        route = "home",
        label = "Home",
        icon = Icons.Default.Home,
        contentDescription = "Home dashboard",
        isMainSection = true
    ),
    JOURNEYS(
        route = "journeys",
        label = "Journeys",
        icon = Icons.Default.DirectionsTransit,
        contentDescription = "Journey planner",
        isMainSection = true
    ),
    TASKS(
        route = "tasks",
        label = "Tasks",
        icon = Icons.Default.CheckCircle,
        contentDescription = "To-do lists",
        isMainSection = true
    ),
    NOTES(
        route = "notes",
        label = "Notes",
        icon = Icons.Default.Description,
        contentDescription = "Quick notes",
        isMainSection = true
    ),
    
    // =========================================================================
    // APP SECTIONS
    // =========================================================================
    SETTINGS(
        route = "settings",
        label = "Settings",
        icon = Icons.Default.Settings,
        contentDescription = "App settings",
        isMainSection = false
    ),
    ACCOUNT(
        route = "account",
        label = "Account",
        icon = Icons.Default.Person,
        contentDescription = "Account settings",
        isMainSection = false
    ),
    SUPPORT(
        route = "support",
        label = "Support",
        icon = Icons.Default.Help,
        contentDescription = "Get support",
        isMainSection = false
    )
}

/**
 * Navigation Routes
 * 
 * All navigation destinations in the app
 */
object Routes {
    // Main screens
    const val HOME = "home"
    const val JOURNEYS = "journeys"
    const val TASKS = "tasks"
    const val NOTES = "notes"
    
    // Journey flow
    const val JOURNEY_PLANNER = "journey_planner"
    const val LOCATION_SEARCH = "location_search/{focus}"
    const val ROUTE_SELECTION = "route_selection"
    const val ALARM_SETUP = "alarm_setup/{routeId}"
    const val JOURNEY_DETAILS = "journey_details/{journeyId}"
    
    // Profile management
    const val PROFILE_LIST = "profile_list"
    const val PROFILE_EDITOR = "profile_editor/{profileId}"
    const val CREATE_PROFILE = "create_profile"
    
    // App sections
    const val SETTINGS = "settings"
    const val ACCOUNT = "account"
    const val SUPPORT = "support"
    
    // Auth/Onboarding
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    
    // Developer
    const val ALARM_TEST = "alarm_test"
    
    // Sub-screens
    const val FONT_SELECTION = "font_selection"
    
    // Helper functions for parameterized routes
    fun locationSearch(focus: String) = "location_search/$focus"
    fun alarmSetup(routeId: String) = "alarm_setup/$routeId"
    fun journeyDetails(journeyId: Long) = "journey_details/$journeyId"
    fun profileEditor(profileId: Long) = "profile_editor/$profileId"
}
