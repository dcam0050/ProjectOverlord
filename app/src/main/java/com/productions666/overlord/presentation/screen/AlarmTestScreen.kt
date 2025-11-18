package com.productions666.overlord.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.productions666.overlord.OverlordApplication
import com.productions666.overlord.alarm.AlarmScheduler
import com.productions666.overlord.data.database.entity.AlarmInstanceEntity
import com.productions666.overlord.data.database.entity.AlarmProfileEntity
import com.productions666.overlord.data.database.entity.toDomain
import com.productions666.overlord.data.model.AlarmType
import com.productions666.overlord.domain.model.AlarmInstance
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTestScreen() {
    val context = LocalContext.current
    val database = (context.applicationContext as OverlordApplication).database
    val alarmDao = database.alarmInstanceDao()
    val profileDao = database.alarmProfileDao()
    val scope = rememberCoroutineScope()
    
    var activeAlarms by remember { mutableStateOf<List<AlarmInstanceEntity>>(emptyList()) }
    var allAlarms by remember { mutableStateOf<List<AlarmInstanceEntity>>(emptyList()) }
    var profiles by remember { mutableStateOf<List<AlarmProfileEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    
    // Form state
    var minutesFromNow by remember { mutableStateOf("1") }
    var selectedAlarmType by remember { mutableStateOf(AlarmType.WAKE_UP) }
    var selectedProfileId by remember { mutableStateOf<Long?>(null) }
    var requiresUserDismiss by remember { mutableStateOf(true) }
    var autoStopSeconds by remember { mutableStateOf("") }
    var alarmLabel by remember { mutableStateOf("Test Alarm") }
    
    // Load initial data
    LaunchedEffect(Unit) {
        scope.launch {
            alarmDao.getActiveAlarms().collect { alarms ->
                activeAlarms = alarms
            }
        }
        scope.launch {
            alarmDao.getAllAlarms().collect { alarms ->
                allAlarms = alarms
            }
        }
        scope.launch {
            profileDao.getAllProfiles().collect { profileList ->
                profiles = profileList
                if (profileList.isNotEmpty() && selectedProfileId == null) {
                    selectedProfileId = profileList.first().id
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarm Engine Test") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Schedule Test Alarm",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            // Alarm scheduling form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Minutes from now
                        OutlinedTextField(
                            value = minutesFromNow,
                            onValueChange = { minutesFromNow = it },
                            label = { Text("Minutes from now") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            supportingText = {
                                val minutes = minutesFromNow.toIntOrNull() ?: 0
                                if (minutes > 0) {
                                    val triggerTime = Instant.now().plusSeconds(minutes * 60L)
                                    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                                    val timeStr = LocalDateTime.ofInstant(triggerTime, ZoneId.systemDefault())
                                        .format(formatter)
                                    Text("Will trigger at $timeStr")
                                }
                            }
                        )
                        
                        // Alarm type selector
                        Text(
                            text = "Alarm Type",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AlarmType.values().forEach { type ->
                                FilterChip(
                                    selected = selectedAlarmType == type,
                                    onClick = { selectedAlarmType = type },
                                    label = { Text(type.name.replace("_", " ")) }
                                )
                            }
                        }
                        
                        // Profile selector
                        if (profiles.isNotEmpty()) {
                            Text(
                                text = "Alarm Profile",
                                style = MaterialTheme.typography.labelLarge
                            )
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = profiles.find { it.id == selectedProfileId }?.name ?: "Select profile",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    profiles.forEach { profile ->
                                        DropdownMenuItem(
                                            text = { Text(profile.name) },
                                            onClick = {
                                                selectedProfileId = profile.id
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Label input
                        OutlinedTextField(
                            value = alarmLabel,
                            onValueChange = { alarmLabel = it },
                            label = { Text("Alarm Label") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // Requires user dismiss toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Requires user dismiss")
                            Switch(
                                checked = requiresUserDismiss,
                                onCheckedChange = { requiresUserDismiss = it }
                            )
                        }
                        
                        // Auto-stop seconds (only if requiresUserDismiss is false)
                        if (!requiresUserDismiss) {
                            OutlinedTextField(
                                value = autoStopSeconds,
                                onValueChange = { autoStopSeconds = it },
                                label = { Text("Auto-stop after (seconds)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                supportingText = {
                                    val seconds = autoStopSeconds.toIntOrNull() ?: 0
                                    if (seconds > 0) {
                                        Text("Alarm will auto-stop after ${seconds}s")
                                    }
                                }
                            )
                        }
                        
                        // Schedule button
                        Button(
                            onClick = {
                                val minutes = minutesFromNow.toIntOrNull() ?: 0
                                if (minutes <= 0) {
                                    return@Button
                                }
                                
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val scheduledTime = Instant.now().plusSeconds(minutes * 60L)
                                        val profileId = selectedProfileId ?: profiles.firstOrNull()?.id ?: 1L
                                        val autoStopMillis = if (!requiresUserDismiss && autoStopSeconds.isNotBlank()) {
                                            autoStopSeconds.toLongOrNull()?.times(1000)
                                        } else null
                                        
                                        val alarm = AlarmInstance(
                                            id = 0,
                                            journeyId = null,
                                            scheduledTime = scheduledTime,
                                            type = selectedAlarmType,
                                            profileId = profileId,
                                            requiresUserDismiss = requiresUserDismiss,
                                            autoStopAfterMillis = autoStopMillis,
                                            label = alarmLabel.ifBlank { "Test Alarm" }
                                        )
                                        
                                        val entity = AlarmInstanceEntity(
                                            id = 0,
                                            journeyId = null,
                                            scheduledTime = alarm.scheduledTime.toEpochMilli(),
                                            type = alarm.type,
                                            profileId = alarm.profileId,
                                            requiresUserDismiss = alarm.requiresUserDismiss,
                                            autoStopAfterMillis = alarm.autoStopAfterMillis,
                                            label = alarm.label,
                                            isActive = true
                                        )
                                        
                                        val alarmId = alarmDao.insertAlarm(entity)
                                        val scheduler = AlarmScheduler(context)
                                        scheduler.scheduleAlarm(alarm.copy(id = alarmId))
                                        
                                        // Reset form
                                        minutesFromNow = "1"
                                        alarmLabel = "Test Alarm"
                                        autoStopSeconds = ""
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading && minutesFromNow.toIntOrNull()?.let { it > 0 } == true && selectedProfileId != null,
                            shape = MaterialTheme.shapes.large
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Schedule Alarm")
                            }
                        }
                    }
                }
            }
            
            // Active alarms list
            item {
                Text(
                    text = "Active Alarms (${activeAlarms.size})",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            if (activeAlarms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active alarms",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(activeAlarms) { alarmEntity ->
                    AlarmCard(
                        alarm = alarmEntity.toDomain(),
                        onCancel = {
                            scope.launch {
                                alarmDao.deactivateAlarm(alarmEntity.id)
                                // Also cancel from AlarmManager
                                val scheduler = AlarmScheduler(context)
                                scheduler.cancelAlarm(alarmEntity.id)
                            }
                        }
                    )
                }
            }
            
            // All alarms section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "All Alarms in Database (${allAlarms.size})",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (allAlarms.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear All Alarms")
                        }
                    }
                }
            }
            
            // Clear confirmation dialog
            if (showClearConfirmDialog) {
                item {
                    AlertDialog(
                        onDismissRequest = { showClearConfirmDialog = false },
                        title = { Text("Clear All Alarms?") },
                        text = { 
                            Text("This will delete all alarms from the database and cancel any scheduled alarms. This action cannot be undone.")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val scheduler = AlarmScheduler(context)
                                            // Cancel all active alarms from AlarmManager
                                            activeAlarms.forEach { alarm ->
                                                scheduler.cancelAlarm(alarm.id)
                                            }
                                            // Delete all alarms from database
                                            alarmDao.deleteAllAlarms()
                                            showClearConfirmDialog = false
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Clear All")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showClearConfirmDialog = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
            
            if (allAlarms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No alarms in database",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(allAlarms) { alarmEntity ->
                    AlarmDetailCard(alarmEntity = alarmEntity)
                }
            }
        }
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmInstance,
    onCancel: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val timeStr = LocalDateTime.ofInstant(alarm.scheduledTime, ZoneId.systemDefault())
        .format(formatter)
    val now = Instant.now()
    val isPast = alarm.scheduledTime.isBefore(now)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = alarm.type.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("Profile ID: ${alarm.profileId}") }
                )
                if (!alarm.requiresUserDismiss) {
                    AssistChip(
                        onClick = {},
                        label = {
                            val autoStopSec = alarm.autoStopAfterMillis?.div(1000) ?: 0
                            Text("Auto-stop: ${autoStopSec}s")
                        }
                    )
                } else {
                    AssistChip(
                        onClick = {},
                        label = { Text("Manual dismiss") }
                    )
                }
            }
            
            if (isPast) {
                Text(
                    text = "⚠ Alarm time has passed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cancel Alarm")
            }
        }
    }
}

@Composable
fun AlarmDetailCard(
    alarmEntity: AlarmInstanceEntity
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val scheduledTime = Instant.ofEpochMilli(alarmEntity.scheduledTime)
    val scheduledTimeStr = LocalDateTime.ofInstant(scheduledTime, ZoneId.systemDefault())
        .format(formatter)
    
    val acknowledgedTimeStr = alarmEntity.acknowledgedAt?.let { acknowledgedAt ->
        val acknowledgedTime = Instant.ofEpochMilli(acknowledgedAt)
        LocalDateTime.ofInstant(acknowledgedTime, ZoneId.systemDefault())
            .format(formatter)
    } ?: "Not acknowledged"

    val cancelledTimeStr = alarmEntity.cancelledAt?.let { cancelledAt ->
        val cancelledTime = Instant.ofEpochMilli(cancelledAt)
        LocalDateTime.ofInstant(cancelledTime, ZoneId.systemDefault())
            .format(formatter)
    } ?: "Not cancelled"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (alarmEntity.isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row with ID and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ID: ${alarmEntity.id}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (alarmEntity.isActive) "Active" else "Inactive",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (alarmEntity.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                }
                Text(
                    text = alarmEntity.type.name.replace("_", " "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Label
            Text(
                text = alarmEntity.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Details grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(label = "Scheduled Time", value = scheduledTimeStr)
                DetailRow(label = "Profile ID", value = alarmEntity.profileId.toString())
                DetailRow(
                    label = "Requires User Dismiss",
                    value = if (alarmEntity.requiresUserDismiss) "Yes" else "No"
                )
                alarmEntity.autoStopAfterMillis?.let { millis ->
                    DetailRow(
                        label = "Auto-stop After",
                        value = "${millis / 1000} seconds"
                    )
                }
                alarmEntity.journeyId?.let { journeyId ->
                    DetailRow(label = "Journey ID", value = journeyId.toString())
                }
                DetailRow(
                    label = "Acknowledged At",
                    value = acknowledgedTimeStr,
                    valueColor = if (alarmEntity.acknowledgedAt != null) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                DetailRow(
                    label = "Cancelled At",
                    value = cancelledTimeStr,
                    valueColor = if (alarmEntity.cancelledAt != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

