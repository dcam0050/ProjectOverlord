package com.productions666.overlord.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.productions666.overlord.presentation.viewmodel.EditableAlarm
import com.productions666.overlord.presentation.viewmodel.formatAsTime
import java.time.Instant

/**
 * Alarm Row Item
 * 
 * Individual alarm row with customization options.
 * Each row supports multiple tap targets for different actions.
 * 
 * Visual layout:
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ [✓]  05:15 AM                                           🔊  🎵     │
 * │      Wake Up                                                        │
 * └─────────────────────────────────────────────────────────────────────┘
 * 
 * Interaction Points:
 * - Checkbox: Toggle alarm on/off
 * - Time: Edit alarm time
 * - Label: Edit alarm label
 * - Sound icons: Change sound / Spotify
 */
@Composable
fun AlarmRowItem(
    alarm: EditableAlarm,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    onLabelClick: () -> Unit,
    onSoundClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Check if alarm is in the past
    val isInPast = alarm.scheduledTime.isBefore(Instant.now())
    val effectivelyDisabled = !alarm.isEnabled || isInPast
    
    val alpha by animateFloatAsState(
        targetValue = if (!effectivelyDisabled) 1f else 0.5f,
        label = "alpha"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isInPast -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            alarm.isEnabled -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        label = "background"
    )
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for toggle (disabled if in past)
            Checkbox(
                checked = alarm.isEnabled && !isInPast,
                onCheckedChange = if (isInPast) null else onToggle,
                enabled = !isInPast,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    disabledCheckedColor = MaterialTheme.colorScheme.outline,
                    disabledUncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Time (clickable, disabled if in past)
            Surface(
                modifier = Modifier
                    .clickable(enabled = alarm.isEnabled && !isInPast, onClick = onTimeClick),
                shape = MaterialTheme.shapes.small,
                color = when {
                    isInPast -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    alarm.isEdited && alarm.isEnabled -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                }
            ) {
                Text(
                    text = alarm.scheduledTime.formatAsTime(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isInPast -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        alarm.isEnabled -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    },
                    textDecoration = if (effectivelyDisabled) TextDecoration.LineThrough else null,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Label (clickable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = alarm.isEnabled && !isInPast, onClick = onLabelClick)
                    .alpha(alpha)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isInPast) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textDecoration = if (effectivelyDisabled) TextDecoration.LineThrough else null
                    )
                    
                    // Show edit indicator if label was edited
                    if (alarm.isEdited && alarm.isEnabled && !isInPast) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edited",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Show "In the past" message if applicable, otherwise show offset
                Text(
                    text = if (isInPast) "⚠ In the past - will not fire" else formatOffset(alarm.originalOffsetMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isInPast) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
            
            // Sound button (disabled if in past)
            IconButton(
                onClick = onSoundClick,
                enabled = alarm.isEnabled && !isInPast,
                modifier = Modifier.alpha(alpha)
            ) {
                Icon(
                    imageVector = if (alarm.soundUri != null) {
                        Icons.Default.VolumeUp
                    } else {
                        Icons.Default.NotificationsActive
                    },
                    contentDescription = "Sound settings",
                    tint = if (isInPast) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            }
        }
    }
}

/**
 * Compact alarm row for list display
 */
@Composable
fun AlarmRowCompact(
    alarm: EditableAlarm,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (alarm.isEnabled) 1f else 0.5f,
        label = "alpha"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = alarm.isEnabled,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = alarm.scheduledTime.formatAsTime(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alpha(alpha)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = alarm.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier
                .weight(1f)
                .alpha(alpha)
        )
    }
}

/**
 * Alarm list with all alarms for the journey
 */
@Composable
fun AlarmReviewList(
    alarms: List<EditableAlarm>,
    onAlarmToggle: (Long, Boolean) -> Unit,
    onAlarmTimeClick: (Long) -> Unit,
    onAlarmLabelClick: (Long) -> Unit,
    onAlarmSoundClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        alarms.forEach { alarm ->
            AlarmRowItem(
                alarm = alarm,
                onToggle = { enabled -> onAlarmToggle(alarm.id, enabled) },
                onTimeClick = { onAlarmTimeClick(alarm.id) },
                onLabelClick = { onAlarmLabelClick(alarm.id) },
                onSoundClick = { onAlarmSoundClick(alarm.id) }
            )
        }
    }
}

/**
 * Format offset minutes as human-readable string
 */
private fun formatOffset(offsetMinutes: Int): String {
    return when {
        offsetMinutes == 0 -> "At departure"
        offsetMinutes > 0 -> "${offsetMinutes}m after departure"
        offsetMinutes >= -60 -> "${-offsetMinutes}m before departure"
        else -> {
            val hours = -offsetMinutes / 60
            val mins = -offsetMinutes % 60
            if (mins == 0) {
                "${hours}h before departure"
            } else {
                "${hours}h ${mins}m before departure"
            }
        }
    }
}

/**
 * Time Adjust Bottom Sheet
 * 
 * Allows users to adjust individual alarm times with quick adjust buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTimeAdjustBottomSheet(
    visible: Boolean,
    alarm: EditableAlarm?,
    onDismiss: () -> Unit,
    onTimeSelected: (Long, java.time.LocalTime) -> Unit,
    onQuickAdjust: (Long, Int) -> Unit
) {
    if (!visible || alarm == null) return
    
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Adjust Alarm Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = alarm.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = formatOffset(alarm.originalOffsetMinutes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Current time display
            Text(
                text = alarm.scheduledTime.formatAsTime(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quick adjust buttons
            Text(
                text = "Quick adjust:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAdjustButton("-15m") { onQuickAdjust(alarm.id, -15) }
                QuickAdjustButton("-5m") { onQuickAdjust(alarm.id, -5) }
                QuickAdjustButton("+5m") { onQuickAdjust(alarm.id, 5) }
                QuickAdjustButton("+15m") { onQuickAdjust(alarm.id, 15) }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Done button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun QuickAdjustButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * Label Edit Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmLabelEditDialog(
    visible: Boolean,
    alarm: EditableAlarm?,
    onDismiss: () -> Unit,
    onSave: (Long, String) -> Unit
) {
    if (!visible || alarm == null) return
    
    var editedLabel by remember(alarm.id) {
        mutableStateOf(alarm.label)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Alarm Label")
        },
        text = {
            OutlinedTextField(
                value = editedLabel,
                onValueChange = { editedLabel = it },
                label = { Text("Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(alarm.id, editedLabel) },
                enabled = editedLabel.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

