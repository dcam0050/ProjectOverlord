package com.productions666.overlord.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productions666.overlord.data.model.Place
import com.productions666.overlord.data.model.Route
import com.productions666.overlord.presentation.components.*
import com.productions666.overlord.presentation.viewmodel.AlarmSetupUiState
import com.productions666.overlord.presentation.viewmodel.AlarmSetupViewModel
import com.productions666.overlord.presentation.viewmodel.ProfileUiModel

/**
 * Alarm Setup Screen
 * 
 * The final step in the journey planning flow where users:
 * 1. See a summary of their selected route
 * 2. Select an alarm profile template
 * 3. Review and customize individual alarms
 * 4. Schedule all alarms
 * 
 * Visual layout:
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ [←]  Set Your Alarms                                                │
 * │      Departing 07:15, Tue 3 Dec                                     │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │                                                                     │
 * │  Journey Summary                                                    │
 * │  ┌───────────────────────────────────────────────────────────────┐  │
 * │  │  Sheffield → London St Pancras                                │  │
 * │  │  Depart 07:15 • Arrive 08:45 • 1 hour 30 mins                 │  │
 * │  │  🚂 Train                                                     │  │
 * │  └───────────────────────────────────────────────────────────────┘  │
 * │                                                                     │
 * │  Alarm Profile                                                      │
 * │  ┌───────────────────────────────────────────────────────────────┐  │
 * │  │  Full Morning Routine                             [Change]    │  │
 * │  │  6 alarms • First at 05:15                                    │  │
 * │  └───────────────────────────────────────────────────────────────┘  │
 * │                                                                     │
 * │  Your Alarms (tap to customize)                                     │
 * │  ┌───────────────────────────────────────────────────────────────┐  │
 * │  │ [✓]  05:15 AM     Wake Up                         🔊         │  │
 * │  │ [✓]  05:30 AM     Get Out of Bed                  🔊         │  │
 * │  │ [✓]  05:45 AM     Start Shower                    🔊         │  │
 * │  │ ...                                                           │  │
 * │  └───────────────────────────────────────────────────────────────┘  │
 * │                                                                     │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │  [          Schedule All Alarms          ]                          │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@Composable
fun AlarmSetupScreen(
    viewModel: AlarmSetupViewModel,
    route: Route,
    origin: Place,
    destination: Place,
    onSchedulingComplete: (Long) -> Unit,
    onCreateProfile: () -> Unit,
    onEditProfile: (Long) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    // Pending navigation after bottom sheet dismissal
    var pendingEditProfileId by remember { mutableStateOf<Long?>(null) }
    var pendingCreateProfile by remember { mutableStateOf(false) }
    
    // Track if we should re-show bottom sheet when returning from profile editor
    var shouldReshowBottomSheet by rememberSaveable { mutableStateOf(false) }
    
    // Re-show bottom sheet when returning from profile editor
    LaunchedEffect(Unit) {
        if (shouldReshowBottomSheet) {
            shouldReshowBottomSheet = false
            viewModel.showProfileSelection()
        }
    }
    
    // Handle delayed navigation after bottom sheet closes
    LaunchedEffect(pendingEditProfileId) {
        if (pendingEditProfileId != null) {
            shouldReshowBottomSheet = true // Remember to re-show when returning
            kotlinx.coroutines.delay(100) // Wait for bottom sheet animation
            onEditProfile(pendingEditProfileId!!)
            pendingEditProfileId = null
        }
    }
    
    LaunchedEffect(pendingCreateProfile) {
        if (pendingCreateProfile) {
            shouldReshowBottomSheet = true // Remember to re-show when returning
            kotlinx.coroutines.delay(100) // Wait for bottom sheet animation
            onCreateProfile()
            pendingCreateProfile = false
        }
    }
    
    // Initialize route data
    LaunchedEffect(route) {
        viewModel.setRouteData(route, origin, destination)
    }
    
    // Handle scheduling complete
    LaunchedEffect(uiState.schedulingComplete) {
        if (uiState.schedulingComplete && uiState.scheduledJourneyId != null) {
            onSchedulingComplete(uiState.scheduledJourneyId!!)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero section
            AlarmSetupHeroSection(route = route)
            
            // Journey Summary Card
            JourneySummaryCard(
                route = route,
                origin = origin,
                destination = destination
            )
            
            // Profile Selection Card
            val selectedProfileModel = uiState.selectedProfile?.let { profile ->
                ProfileUiModel(
                    id = profile.profile.id,
                    name = profile.profile.name,
                    description = profile.profile.description,
                    alarmCount = profile.alarms.size,
                    firstAlarmLabel = profile.alarms.minByOrNull { it.sortOrder }?.label,
                    firstAlarmOffset = profile.alarms.minByOrNull { it.sortOrder }?.offsetMinutes,
                    isSystemDefault = profile.profile.isSystemDefault
                )
            }
            
            ProfileSelectionCard(
                selectedProfile = selectedProfileModel,
                departureTime = route.departureTime,
                onChangeProfile = { viewModel.showProfileSelection() }
            )
            
            // Alarms Section
            if (uiState.alarms.isNotEmpty()) {
                AlarmsSection(
                    uiState = uiState,
                    onAlarmToggle = { id, enabled -> viewModel.toggleAlarm(id, enabled) },
                    onAlarmTimeClick = { id -> viewModel.showTimeAdjust(id) },
                    onAlarmLabelClick = { id -> viewModel.showLabelEdit(id) },
                    onAlarmSoundClick = { _ ->
                        // TODO: Implement sound picker
                    }
                )
            } else if (uiState.isLoadingProfiles) {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Error display
            uiState.error?.let { error ->
                ErrorBanner(
                    message = error,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
        
        // Schedule button (fixed at bottom)
        ScheduleButton(
            enabled = !uiState.isScheduling && uiState.alarms.any { it.isEnabled },
            isLoading = uiState.isScheduling,
            alarmCount = uiState.alarms.count { it.isEnabled },
            onClick = { viewModel.scheduleAllAlarms() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        )
    }
    
    // Profile Selection Bottom Sheet
    ProfileSelectionBottomSheet(
        visible = uiState.showProfileSelection,
        profiles = uiState.availableProfiles,
        selectedProfileId = uiState.selectedProfile?.profile?.id,
        departureTime = route.departureTime,
        onProfileSelected = { profileId -> viewModel.selectProfileById(profileId) },
        onEditProfile = { profileId ->
            viewModel.dismissProfileSelection()
            pendingEditProfileId = profileId  // Navigate after sheet closes
        },
        onCreateProfile = {
            viewModel.dismissProfileSelection()
            pendingCreateProfile = true  // Navigate after sheet closes
        },
        onDismiss = { viewModel.dismissProfileSelection() }
    )
    
    // Time Adjust Bottom Sheet
    AlarmTimeAdjustBottomSheet(
        visible = uiState.showTimeAdjustSheet,
        alarm = viewModel.getEditingAlarm(),
        onDismiss = { viewModel.dismissTimeAdjust() },
        onTimeSelected = { id, time -> viewModel.adjustAlarmTime(id, time) },
        onQuickAdjust = { id, minutes -> viewModel.adjustAlarmByMinutes(id, minutes) }
    )
    
    // Label Edit Dialog
    AlarmLabelEditDialog(
        visible = uiState.showLabelEditDialog,
        alarm = viewModel.getEditingAlarm(),
        onDismiss = { viewModel.dismissLabelEdit() },
        onSave = { id, label -> viewModel.updateAlarmLabel(id, label) }
    )
}

@Composable
private fun AlarmSetupHeroSection(route: Route) {
    val departureFormatted = route.departureTime
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm, EEE d MMM"))
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Set Your Alarms",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Departing $departureFormatted",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AlarmsSection(
    uiState: AlarmSetupUiState,
    onAlarmToggle: (Long, Boolean) -> Unit,
    onAlarmTimeClick: (Long) -> Unit,
    onAlarmLabelClick: (Long) -> Unit,
    onAlarmSoundClick: (Long) -> Unit
) {
    // Update current time every 60 seconds to keep past alarm check current
    var currentTime by remember { mutableStateOf(java.time.Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L) // 60 seconds
            currentTime = java.time.Instant.now()
        }
    }
    
    val pastAlarmsCount = uiState.alarms.count { it.scheduledTime.isBefore(currentTime) }
    val enabledFutureAlarms = uiState.alarms.count { it.isEnabled && !it.scheduledTime.isBefore(currentTime) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Alarms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "$enabledFutureAlarms of ${uiState.alarms.size - pastAlarmsCount} enabled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        
        // Show warning if there are past alarms
        if (pastAlarmsCount > 0) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$pastAlarmsCount alarm${if (pastAlarmsCount > 1) "s are" else " is"} in the past and will not fire",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Text(
            text = "Tap time to adjust, tap label to rename",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        
        AlarmReviewList(
            alarms = uiState.alarms,
            onAlarmToggle = onAlarmToggle,
            onAlarmTimeClick = onAlarmTimeClick,
            onAlarmLabelClick = onAlarmLabelClick,
            onAlarmSoundClick = onAlarmSoundClick
        )
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ScheduleButton(
    enabled: Boolean,
    isLoading: Boolean,
    alarmCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (alarmCount > 0) "Schedule $alarmCount Alarm${if (alarmCount != 1) "s" else ""}" else "Schedule Alarms",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Success screen shown after alarms are scheduled
 */
@Composable
fun AlarmSetupSuccessScreen(
    journeyId: Long,
    alarmCount: Int,
    firstAlarmTime: String,
    onViewJourney: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Alarms Scheduled!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "$alarmCount alarm${if (alarmCount != 1) "s" else ""} set",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "First alarm at $firstAlarmTime",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onViewJourney,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Text("View Journey Details")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Done")
        }
    }
}

