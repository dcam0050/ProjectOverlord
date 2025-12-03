package com.productions666.overlord.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * TimeMode enum for Arrive/Depart selection
 * 
 * Note: LEAVE_NOW is removed per Trainline-inspired UX.
 * The question is always "when do I need to arrive/leave?" not "now"
 */
enum class TimeMode {
    ARRIVE_BY,    // "I need to arrive by this time" (DEFAULT)
    DEPART_AT     // "I want to leave at this time"
}

/**
 * ArriveByCard — Time selection with Arrive/Depart toggle
 * 
 * Visual layout:
 * ┌─────────────────────────────────────────┐
 * │ Arrive by                               │  ← Section title changes with mode
 * │                                         │
 * │ →  [Today ▼]        [09:00 ▼]           │  ← Date and time dropdowns
 * │                                         │
 * └─────────────────────────────────────────┘
 */
@Composable
fun ArriveByCard(
    selectedDate: LocalDate?,
    selectedTime: LocalTime?,
    timeMode: TimeMode,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onTimeModeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section title (clickable to toggle mode)
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onTimeModeToggle)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when (timeMode) {
                        TimeMode.ARRIVE_BY -> "Arrive by"
                        TimeMode.DEPART_AT -> "Depart at"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle time mode",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Date and time selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date selector
                TimeSelector(
                    label = selectedDate?.let { formatDateLabel(it) } ?: "Select date",
                    icon = Icons.Default.CalendarToday,
                    onClick = onDateClick,
                    modifier = Modifier.weight(1f)
                )
                
                // Time selector
                TimeSelector(
                    label = selectedTime?.format(timeFormatter) ?: "Select time",
                    icon = Icons.Default.Schedule,
                    onClick = onTimeClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TimeSelector(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Format date label with smart text:
 * - "Today" for current date
 * - "Tomorrow" for next day
 * - "Wed, 4 Dec" for other dates
 */
private fun formatDateLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
    }
}

