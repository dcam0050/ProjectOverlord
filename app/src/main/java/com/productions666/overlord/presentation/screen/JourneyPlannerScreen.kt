package com.productions666.overlord.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.productions666.overlord.data.model.Place
import com.productions666.overlord.presentation.viewmodel.JourneyPlannerViewModel
import com.productions666.overlord.presentation.viewmodel.TimeMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyPlannerScreen(
    viewModel: JourneyPlannerViewModel = viewModel(),
    onRoutesFound: (List<com.productions666.overlord.data.model.Route>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero section
        Text(
            text = "Where are you going?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Plan your journey and we'll make sure you don't miss it",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Origin card
        OriginDestinationCard(
            title = "From",
            place = uiState.origin,
            placeholder = "Use current location",
            icon = Icons.Default.LocationOn,
            onClick = {
                // For now, use a mock current location
                viewModel.setOrigin(
                    Place(
                        name = "Current Location",
                        address = "123 Main St, City",
                        latitude = 51.5074,
                        longitude = -0.1278
                    )
                )
            }
        )
        
        // Destination card
        var destinationText by remember { mutableStateOf("") }
        var showDestinationInput by remember { mutableStateOf(false) }
        
        if (showDestinationInput) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    OutlinedTextField(
                        value = destinationText,
                        onValueChange = { destinationText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter destination") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            Row {
                                if (destinationText.isNotEmpty()) {
                                    IconButton(onClick = { destinationText = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                                IconButton(onClick = {
                                    if (destinationText.isNotEmpty()) {
                                        viewModel.setDestination(
                                            Place(
                                                name = destinationText,
                                                address = destinationText,
                                                latitude = 51.5074 + kotlin.random.Random.nextDouble(-0.1, 0.1),
                                                longitude = -0.1278 + kotlin.random.Random.nextDouble(-0.1, 0.1)
                                            )
                                        )
                                        showDestinationInput = false
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Confirm",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        } else {
            OriginDestinationCard(
                title = "To",
                place = uiState.destination,
                placeholder = "Enter destination",
                icon = Icons.Default.Search,
                onClick = { showDestinationInput = true }
            )
        }
        
        // Time selection card
        TimeSelectionCard(
            timeMode = uiState.timeMode,
            selectedDate = uiState.selectedDate,
            selectedTime = uiState.selectedTime,
            onTimeModeChanged = { viewModel.setTimeMode(it) },
            onDateChanged = { date -> viewModel.setSelectedDate(date) },
            onTimeChanged = { time -> viewModel.setSelectedTime(time) }
        )
        
        // Error message
        uiState.error?.let { error ->
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
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Find routes button
        Button(
            onClick = {
                viewModel.findRoutes()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading && uiState.origin != null && uiState.destination != null,
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Find Routes",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Show routes if available
        if (uiState.routes.isNotEmpty()) {
            LaunchedEffect(uiState.routes) {
                onRoutesFound(uiState.routes)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OriginDestinationCard(
    title: String,
    place: Place?,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Text(
                    text = place?.name ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (place != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                
                place?.address?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun TimeSelectionCard(
    timeMode: TimeMode,
    selectedDate: LocalDate?,
    selectedTime: LocalTime?,
    onTimeModeChanged: (TimeMode) -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    onTimeChanged: (LocalTime) -> Unit
) {
    var dateText by remember { mutableStateOf(selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "") }
    var timeText by remember { mutableStateOf(selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "") }
    
    LaunchedEffect(selectedDate) {
        dateText = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: ""
    }
    
    LaunchedEffect(selectedTime) {
        timeText = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "When?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            // Time mode selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeModeChip(
                    label = "Leave now",
                    selected = timeMode == TimeMode.LEAVE_NOW,
                    onClick = { onTimeModeChanged(TimeMode.LEAVE_NOW) }
                )
                TimeModeChip(
                    label = "Arrive by",
                    selected = timeMode == TimeMode.ARRIVE_BY,
                    onClick = { onTimeModeChanged(TimeMode.ARRIVE_BY) }
                )
                TimeModeChip(
                    label = "Depart at",
                    selected = timeMode == TimeMode.DEPART_AT,
                    onClick = { onTimeModeChanged(TimeMode.DEPART_AT) }
                )
            }
            
            // Date and time selection (if not "Leave now")
            if (timeMode != TimeMode.LEAVE_NOW) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { 
                            dateText = it
                            try {
                                val date = LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                onDateChanged(date)
                            } catch (e: Exception) {
                                // Invalid date format, ignore
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("YYYY-MM-DD") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { 
                            timeText = it
                            try {
                                val time = LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                                onTimeChanged(time)
                            } catch (e: Exception) {
                                // Invalid time format, ignore
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("HH:mm") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.small
    )
}


