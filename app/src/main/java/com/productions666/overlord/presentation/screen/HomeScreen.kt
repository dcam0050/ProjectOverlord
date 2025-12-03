package com.productions666.overlord.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productions666.overlord.data.database.entity.JourneyStatus
import com.productions666.overlord.data.database.entity.JourneyWithAlarms
import com.productions666.overlord.data.database.entity.ScheduledAlarmEntity
import com.productions666.overlord.data.database.entity.ScheduledJourneyEntity
import com.productions666.overlord.presentation.navigation.FeatureCard
import com.productions666.overlord.presentation.navigation.FeatureHeader
import com.productions666.overlord.presentation.navigation.NeutralCard
import com.productions666.overlord.presentation.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Home Screen - Dashboard
 * 
 * Shows upcoming journey cards and quick actions.
 * Follows Visual Identity v2.0 with deep red/gold theme.
 */
@Composable
fun HomeScreen(
    upcomingJourneys: List<JourneyWithAlarms>,
    onPlanNewJourney: () -> Unit,
    onViewJourneyDetails: (Long) -> Unit,
    onEditJourney: (Long) -> Unit,
    onCancelJourney: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xl)
    ) {
        // Header
        item {
            FeatureHeader(
                title = "Welcome back",
                description = "Your upcoming journeys and alarms at a glance"
            )
        }
        
        // Upcoming Journeys Section
        item {
            Text(
                text = "Upcoming Journeys",
                style = MaterialTheme.typography.titleLarge,
                color = OnBackground,
                modifier = Modifier.padding(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.lg,
                    bottom = Spacing.sm
                )
            )
        }
        
        if (upcomingJourneys.isEmpty()) {
            // Empty state
            item {
                EmptyJourneysCard(
                    onPlanNewJourney = onPlanNewJourney,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )
            }
        } else {
            // Journey cards
            items(
                items = upcomingJourneys,
                key = { it.journey.id }
            ) { journeyWithAlarms ->
                JourneyCard(
                    journey = journeyWithAlarms.journey,
                    alarms = journeyWithAlarms.alarms,
                    onViewDetails = { onViewJourneyDetails(journeyWithAlarms.journey.id) },
                    onEdit = { onEditJourney(journeyWithAlarms.journey.id) },
                    onCancel = { onCancelJourney(journeyWithAlarms.journey.id) },
                    modifier = Modifier.padding(
                        horizontal = Spacing.lg,
                        vertical = Spacing.xs
                    )
                )
            }
        }
        
        // Plan New Journey Button
        item {
            PlanNewJourneyCard(
                onClick = onPlanNewJourney,
                modifier = Modifier.padding(
                    horizontal = Spacing.lg,
                    vertical = Spacing.md
                )
            )
        }
        
        // Quick Stats (optional future feature)
        item {
            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun EmptyJourneysCard(
    onPlanNewJourney: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeutralCard(
        modifier = modifier,
        onClick = onPlanNewJourney
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsTransit,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Text(
                text = "No upcoming journeys",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            Text(
                text = "Plan a journey and we'll make sure you don't miss it",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            Button(
                onClick = onPlanNewJourney,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = OnSecondary
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Plan a journey")
            }
        }
    }
}

@Composable
private fun JourneyCard(
    journey: ScheduledJourneyEntity,
    alarms: List<ScheduledAlarmEntity>,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabledAlarms = alarms.filter { it.isEnabled }
    val nextAlarm = enabledAlarms
        .filter { !it.isFired }
        .minByOrNull { it.scheduledTime }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = DeepRed
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            // Destination with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = journey.destinationName,
                    style = MaterialTheme.typography.titleLarge,
                    color = OnPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.xxs))
            
            // Date and arrival time
            Text(
                text = formatJourneyDateTime(journey),
                style = MaterialTheme.typography.bodyLarge,
                color = OnPrimary.copy(alpha = 0.85f)
            )
            
            Divider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = OnPrimary.copy(alpha = 0.2f)
            )
            
            // Alarm summary
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Column {
                    Text(
                        text = "${enabledAlarms.size} alarms scheduled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnPrimary
                    )
                    nextAlarm?.let {
                        Text(
                            text = "Next: ${formatAlarmTime(it.scheduledTime)} (${it.label})",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = OnPrimary.copy(alpha = 0.2f)
            )
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onViewDetails,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Gold
                    )
                ) {
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                Row {
                    TextButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = OnPrimary.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Error
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanNewJourneyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Text(
                text = "Plan a new journey",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface
            )
        }
    }
}

// ============================================================================
// Helper Functions
// ============================================================================

private fun formatJourneyDateTime(journey: ScheduledJourneyEntity): String {
    val departureInstant = Instant.ofEpochMilli(journey.departureTime)
    val arrivalInstant = Instant.ofEpochMilli(journey.arrivalTime)
    
    val departureDateTime = LocalDateTime.ofInstant(departureInstant, ZoneId.systemDefault())
    val arrivalDateTime = LocalDateTime.ofInstant(arrivalInstant, ZoneId.systemDefault())
    val now = LocalDateTime.now()
    
    val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    val dateStr = when {
        departureDateTime.toLocalDate() == now.toLocalDate() -> "Today"
        departureDateTime.toLocalDate() == now.toLocalDate().plusDays(1) -> "Tomorrow"
        else -> departureDateTime.format(dateFormatter)
    }
    
    val arriveTimeStr = arrivalDateTime.format(timeFormatter)
    
    return "$dateStr • Arrive $arriveTimeStr"
}

private fun formatAlarmTime(epochMillis: Long): String {
    val instant = Instant.ofEpochMilli(epochMillis)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return dateTime.format(formatter)
}

// ============================================================================
// Preview Data (for testing)
// ============================================================================

fun createSampleJourneys(): List<JourneyWithAlarms> {
    val now = System.currentTimeMillis()
    val tomorrow9am = now + (24 * 60 * 60 * 1000) // roughly tomorrow
    
    val journey1 = ScheduledJourneyEntity(
        id = 1,
        originName = "Home",
        originAddress = "123 Main St, Sheffield",
        destinationName = "London Kings Cross",
        destinationAddress = "London, UK",
        routeSummary = "Train via Sheffield Station",
        transportModes = "[\"train\"]",
        departureTime = tomorrow9am - (2 * 60 * 60 * 1000), // 7am
        arrivalTime = tomorrow9am, // 9am
        durationMinutes = 120,
        profileUsedName = "Full Morning Routine",
        status = JourneyStatus.UPCOMING
    )
    
    val alarms1 = listOf(
        ScheduledAlarmEntity(
            id = 1,
            journeyId = 1,
            label = "Wake Up",
            scheduledTime = tomorrow9am - (4 * 60 * 60 * 1000), // 5am
            originalOffsetMinutes = -120,
            sortOrder = 1
        ),
        ScheduledAlarmEntity(
            id = 2,
            journeyId = 1,
            label = "Get Ready",
            scheduledTime = tomorrow9am - (3 * 60 * 60 * 1000), // 6am
            originalOffsetMinutes = -60,
            sortOrder = 2
        ),
        ScheduledAlarmEntity(
            id = 3,
            journeyId = 1,
            label = "Leave Home",
            scheduledTime = tomorrow9am - (2 * 60 * 60 * 1000) - (10 * 60 * 1000), // 6:50am
            originalOffsetMinutes = -10,
            sortOrder = 3
        )
    )
    
    return listOf(
        JourneyWithAlarms(journey = journey1, alarms = alarms1)
    )
}
