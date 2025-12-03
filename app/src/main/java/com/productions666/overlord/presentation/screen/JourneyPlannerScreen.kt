package com.productions666.overlord.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.productions666.overlord.data.model.Route
import com.productions666.overlord.presentation.components.ArriveByCard
import com.productions666.overlord.presentation.components.DatePickerBottomSheet
import com.productions666.overlord.presentation.components.TimePickerBottomSheet
import com.productions666.overlord.presentation.components.WhereCard
import com.productions666.overlord.presentation.viewmodel.JourneyPlannerViewModel
import com.productions666.overlord.presentation.viewmodel.LocationField

/**
 * JourneyPlannerScreen — Trainline-Inspired UX
 * 
 * Key UX patterns:
 * 1. Destination FIRST ("Where are you going?")
 * 2. Origin SECOND (defaults to Home)
 * 3. "Arrive by" as primary time mode
 * 4. Clean bottom sheets for time/date selection
 * 
 * Visual layout:
 * ┌─────────────────────────────────────────┐
 * │         Where are you going?            │
 * │   Plan your journey and I'll make sure  │
 * │   you don't miss it                     │
 * ├─────────────────────────────────────────┤
 * │                                         │
 * │  Where                                  │
 * │  ● Enter destination              [...] │  ← Destination FIRST
 * │  ┊                                      │
 * │  ○ Home                             ↕   │  ← Origin (default: Home)
 * │                                         │
 * ├─────────────────────────────────────────┤
 * │                                         │
 * │  Arrive by                              │
 * │  → [Today ▼]        [Select time ▼]     │
 * │                                         │
 * ├─────────────────────────────────────────┤
 * │                                         │
 * │  [        Find Routes        ]          │
 * │                                         │
 * └─────────────────────────────────────────┘
 */
@Composable
fun JourneyPlannerScreen(
    viewModel: JourneyPlannerViewModel = viewModel(),
    onRoutesFound: (List<Route>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    // Handle routes found
    LaunchedEffect(uiState.routes) {
        if (uiState.routes.isNotEmpty()) {
            onRoutesFound(uiState.routes)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero section
            HeroSection()
            
            // Where Card (destination-first)
            WhereCard(
                destination = uiState.destination,
                origin = uiState.origin,
                onDestinationClick = {
                    viewModel.showLocationSearch(LocationField.DESTINATION)
                },
                onOriginClick = {
                    viewModel.showLocationSearch(LocationField.ORIGIN)
                },
                onSwapClick = {
                    viewModel.swapLocations()
                }
            )
            
            // Arrive By Card
            ArriveByCard(
                selectedDate = uiState.selectedDate,
                selectedTime = uiState.selectedTime,
                timeMode = uiState.timeMode,
                onDateClick = { viewModel.showDatePicker() },
                onTimeClick = { viewModel.showTimePicker() },
                onTimeModeToggle = {
                    // Toggle between ARRIVE_BY and DEPART_AT
                    val newMode = if (uiState.timeMode == com.productions666.overlord.presentation.components.TimeMode.ARRIVE_BY) {
                        com.productions666.overlord.presentation.components.TimeMode.DEPART_AT
                    } else {
                        com.productions666.overlord.presentation.components.TimeMode.ARRIVE_BY
                    }
                    viewModel.setTimeMode(newMode)
                }
            )
            
            // Error message
            uiState.error?.let { error ->
                ErrorCard(
                    message = error,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
        
        // Find Routes Button (fixed at bottom)
        FindRoutesButton(
            enabled = !uiState.isLoading && uiState.destination != null,
            isLoading = uiState.isLoading,
            onClick = { viewModel.findRoutes() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        )
    }
    
    // Time Picker Bottom Sheet
    TimePickerBottomSheet(
        visible = uiState.showTimePicker,
        initialMode = uiState.timeMode,
        initialTime = uiState.selectedTime,
        onDismiss = { viewModel.dismissTimePicker() },
        onConfirm = { mode, time ->
            viewModel.setTimeAndMode(mode, time)
        }
    )
    
    // Date Picker Bottom Sheet
    DatePickerBottomSheet(
        visible = uiState.showDatePicker,
        initialDate = uiState.selectedDate,
        onDismiss = { viewModel.dismissDatePicker() },
        onConfirm = { date ->
            viewModel.confirmDateSelection(date)
        }
    )
    
    // Location Search (TODO: Implement full-screen search with Places API)
    if (uiState.showLocationSearch) {
        LocationSearchDialog(
            focus = uiState.locationSearchFocus,
            onDismiss = { viewModel.dismissLocationSearch() },
            onPlaceSelected = { place ->
                when (uiState.locationSearchFocus) {
                    LocationField.DESTINATION -> viewModel.setDestination(place)
                    LocationField.ORIGIN -> viewModel.setOrigin(place)
                }
            }
        )
    }
}

@Composable
private fun HeroSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Where are you going?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Plan your journey and Daddy will make sure you don't miss it",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorCard(
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
                Icons.Default.Error,
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
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun FindRoutesButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
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
            Text(
                text = "Find Routes",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Temporary location search dialog
 * TODO: Replace with full LocationSearchScreen using Google Places API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSearchDialog(
    focus: LocationField,
    onDismiss: () -> Unit,
    onPlaceSelected: (Place) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (focus) {
                    LocationField.DESTINATION -> "Where are you going?"
                    LocationField.ORIGIN -> "Where are you leaving from?"
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search for a place...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                
                // Quick selection buttons
                if (focus == LocationField.ORIGIN) {
                    Text(
                        text = "Quick select",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                onPlaceSelected(
                                    Place(
                                        name = "Home",
                                        address = "Your home address",
                                        latitude = 51.5074,
                                        longitude = -0.1278
                                    )
                                )
                            },
                            label = { Text("🏠 Home") }
                        )
                        
                        AssistChip(
                            onClick = {
                                onPlaceSelected(
                                    Place(
                                        name = "Work",
                                        address = "Your work address",
                                        latitude = 51.5074,
                                        longitude = -0.1278
                                    )
                                )
                            },
                            label = { Text("💼 Work") }
                        )
                        
                        AssistChip(
                            onClick = {
                                onPlaceSelected(
                                    Place(
                                        name = "Current Location",
                                        address = "Using GPS",
                                        latitude = 51.5074,
                                        longitude = -0.1278
                                    )
                                )
                            },
                            label = { Text("📍 Current") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        // For now, create a mock place
                        // TODO: Use Google Places API
                        onPlaceSelected(
                            Place(
                                name = searchQuery,
                                address = searchQuery,
                                latitude = 51.5074 + kotlin.random.Random.nextDouble(-0.1, 0.1),
                                longitude = -0.1278 + kotlin.random.Random.nextDouble(-0.1, 0.1)
                            )
                        )
                    }
                },
                enabled = searchQuery.isNotBlank()
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
