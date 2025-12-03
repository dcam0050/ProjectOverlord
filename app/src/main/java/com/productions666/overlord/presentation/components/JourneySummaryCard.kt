package com.productions666.overlord.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.productions666.overlord.data.model.LegType
import com.productions666.overlord.data.model.Place
import com.productions666.overlord.data.model.Route
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Journey Summary Card
 * 
 * Displays a compact summary of the selected route at the top of the Alarm Setup screen.
 * Shows:
 * - Origin → Destination
 * - Departure and arrival times
 * - Duration
 * - Transport modes
 * 
 * Visual layout:
 * ┌───────────────────────────────────────────────────────────────┐
 * │  Sheffield → London St Pancras                                │
 * │  Depart 07:15 • Arrive 08:45 • 1 hour 30 mins                │
 * │  🚂 Train                                                     │
 * └───────────────────────────────────────────────────────────────┘
 */
@Composable
fun JourneySummaryCard(
    route: Route,
    origin: Place,
    destination: Place,
    modifier: Modifier = Modifier
) {
    val departureTime = route.departureTime
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    
    val arrivalTime = route.arrivalTime
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    
    val duration = formatDuration(route.durationMinutes)
    
    val transportModes = route.legs
        .map { it.type }
        .distinct()
        .filter { it != LegType.WALK }  // Don't show walking as primary mode
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Route: Origin → Destination
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = origin.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "to",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Times and duration
            Text(
                text = "Depart $departureTime • Arrive $arrivalTime • $duration",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            
            // Transport modes
            if (transportModes.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    transportModes.forEach { mode ->
                        TransportModeChip(mode = mode)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransportModeChip(mode: LegType) {
    val (icon, label) = when (mode) {
        LegType.TRAIN -> Icons.Default.Train to "Train"
        LegType.BUS -> Icons.Default.DirectionsBus to "Bus"
        LegType.TRAM -> Icons.Default.Tram to "Tram"
        LegType.SUBWAY -> Icons.Default.Subway to "Metro"
        LegType.FERRY -> Icons.Default.DirectionsBoat to "Ferry"
        LegType.WALK -> Icons.Default.DirectionsWalk to "Walk"
        LegType.OTHER -> Icons.Default.Commute to "Transit"
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun formatDuration(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes mins"
        minutes % 60 == 0 -> "${minutes / 60} ${if (minutes == 60) "hour" else "hours"}"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}

