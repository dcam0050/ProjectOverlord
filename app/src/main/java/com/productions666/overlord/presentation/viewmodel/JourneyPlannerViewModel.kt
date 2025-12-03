package com.productions666.overlord.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productions666.overlord.data.model.Place
import com.productions666.overlord.data.model.Route
import com.productions666.overlord.data.repository.RoutingRepository
import com.productions666.overlord.presentation.components.TimeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * JourneyPlannerUiState — Trainline-inspired UX state
 * 
 * Key changes from v1:
 * - Destination-first (user thinks "where do I need to be?")
 * - Origin defaults to Home (most journeys start from home)
 * - TimeMode defaults to ARRIVE_BY (the question is "when do I need to arrive?")
 * - Removed LEAVE_NOW option (doesn't make sense for journey planning)
 * 
 * @param destination Primary input - where is the user going?
 * @param origin Secondary input - where are they leaving from? (defaults to Home)
 * @param homeAddress User's saved home address
 * @param timeMode ARRIVE_BY (default) or DEPART_AT
 * @param selectedDate Date of journey (defaults to today)
 * @param selectedTime Target arrival/departure time
 * @param showTimePicker Whether to show the time picker bottom sheet
 * @param showDatePicker Whether to show the date picker bottom sheet
 * @param showLocationSearch Whether to show location search
 * @param locationSearchFocus Which field to focus in location search
 * @param isLoading API loading state
 * @param routes Found routes from Google Transit API
 * @param error Error message to display
 */
data class JourneyPlannerUiState(
    // Location (destination-first)
    val destination: Place? = null,
    val origin: Place? = null,
    val homeAddress: Place? = null,
    
    // Time (arrive-by default)
    val timeMode: TimeMode = TimeMode.ARRIVE_BY,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now().plusHours(1).withMinute(0),
    
    // UI state
    val showTimePicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val showLocationSearch: Boolean = false,
    val locationSearchFocus: LocationField = LocationField.DESTINATION,
    
    // API state
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val error: String? = null
)

/**
 * Which location field is being edited
 */
enum class LocationField {
    DESTINATION,  // "Where are you going?"
    ORIGIN        // "Where are you leaving from?"
}

class JourneyPlannerViewModel(
    private val routingRepository: RoutingRepository = RoutingRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(JourneyPlannerUiState())
    val uiState: StateFlow<JourneyPlannerUiState> = _uiState.asStateFlow()
    
    init {
        // Set default origin to Home if available
        // TODO: Load from UserPreferences DataStore
        _uiState.value = _uiState.value.copy(
            origin = Place(
                name = "Home",
                address = "Set your home address",
                latitude = 0.0,
                longitude = 0.0
            )
        )
    }
    
    // ========================================================================
    // Location Actions (Destination-First)
    // ========================================================================
    
    fun setDestination(place: Place) {
        _uiState.value = _uiState.value.copy(
            destination = place,
            showLocationSearch = false
        )
    }
    
    fun setOrigin(place: Place) {
        _uiState.value = _uiState.value.copy(
            origin = place,
            showLocationSearch = false
        )
    }
    
    fun swapLocations() {
        val current = _uiState.value
        _uiState.value = current.copy(
            destination = current.origin,
            origin = current.destination
        )
    }
    
    // ========================================================================
    // Time Actions (Arrive-By Default)
    // ========================================================================
    
    fun setTimeMode(mode: TimeMode) {
        _uiState.value = _uiState.value.copy(timeMode = mode)
    }
    
    fun setSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }
    
    fun setSelectedTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }
    
    fun setTimeAndMode(mode: TimeMode, time: LocalTime) {
        _uiState.value = _uiState.value.copy(
            timeMode = mode,
            selectedTime = time,
            showTimePicker = false
        )
    }
    
    // ========================================================================
    // UI State Actions
    // ========================================================================
    
    fun showLocationSearch(focus: LocationField) {
        _uiState.value = _uiState.value.copy(
            showLocationSearch = true,
            locationSearchFocus = focus
        )
    }
    
    fun dismissLocationSearch() {
        _uiState.value = _uiState.value.copy(showLocationSearch = false)
    }
    
    fun showTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = true)
    }
    
    fun dismissTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = false)
    }
    
    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }
    
    fun dismissDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }
    
    fun confirmDateSelection(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            showDatePicker = false
        )
    }
    
    // ========================================================================
    // Route Finding
    // ========================================================================
    
    fun findRoutes() {
        val state = _uiState.value
        val origin = state.origin
        val destination = state.destination
        
        if (destination == null) {
            _uiState.value = state.copy(error = "Please select a destination")
            return
        }
        
        if (origin == null || origin.latitude == 0.0) {
            _uiState.value = state.copy(error = "Please set your starting location")
            return
        }
        
        _uiState.value = state.copy(isLoading = true, error = null, routes = emptyList())
        
        viewModelScope.launch {
            try {
                val targetDateTime = LocalDateTime.of(state.selectedDate, state.selectedTime)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                
                val arriveBy = if (state.timeMode == TimeMode.ARRIVE_BY) targetDateTime else null
                val departAt = if (state.timeMode == TimeMode.DEPART_AT) targetDateTime else null
                
                val result = routingRepository.findTransitRoutes(
                    origin = origin,
                    destination = destination,
                    arriveBy = arriveBy,
                    departAt = departAt
                )
                
                result.fold(
                    onSuccess = { routes ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            routes = routes,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            routes = emptyList(),
                            error = error.message ?: "Failed to find routes"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearRoutes() {
        _uiState.value = _uiState.value.copy(routes = emptyList())
    }
}



