package com.productions666.overlord.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productions666.overlord.data.model.Place
import com.productions666.overlord.data.model.Route
import com.productions666.overlord.data.repository.RoutingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class JourneyPlannerUiState(
    val origin: Place? = null,
    val destination: Place? = null,
    val timeMode: TimeMode = TimeMode.LEAVE_NOW,
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val error: String? = null
)

enum class TimeMode {
    LEAVE_NOW,
    ARRIVE_BY,
    DEPART_AT
}

class JourneyPlannerViewModel(
    private val routingRepository: RoutingRepository = RoutingRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(JourneyPlannerUiState())
    val uiState: StateFlow<JourneyPlannerUiState> = _uiState.asStateFlow()
    
    fun setOrigin(place: Place) {
        _uiState.value = _uiState.value.copy(origin = place)
    }
    
    fun setDestination(place: Place) {
        _uiState.value = _uiState.value.copy(destination = place)
    }
    
    fun setTimeMode(mode: TimeMode) {
        _uiState.value = _uiState.value.copy(
            timeMode = mode,
            selectedDate = if (mode == TimeMode.LEAVE_NOW) null else LocalDate.now(),
            selectedTime = if (mode == TimeMode.LEAVE_NOW) null else LocalTime.now().plusHours(1)
        )
    }
    
    fun setSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }
    
    fun setSelectedTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }
    
    fun findRoutes() {
        val state = _uiState.value
        val origin = state.origin
        val destination = state.destination
        
        if (origin == null || destination == null) {
            _uiState.value = state.copy(error = "Please select both origin and destination")
            return
        }
        
        _uiState.value = state.copy(isLoading = true, error = null, routes = emptyList())
        
        viewModelScope.launch {
            try {
                val arriveBy = if (state.timeMode == TimeMode.ARRIVE_BY && state.selectedDate != null && state.selectedTime != null) {
                    LocalDateTime.of(state.selectedDate, state.selectedTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                } else null
                
                val departAt = if (state.timeMode == TimeMode.DEPART_AT && state.selectedDate != null && state.selectedTime != null) {
                    LocalDateTime.of(state.selectedDate, state.selectedTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                } else null
                
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


