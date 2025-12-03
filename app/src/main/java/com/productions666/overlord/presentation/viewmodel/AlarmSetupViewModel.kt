package com.productions666.overlord.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.productions666.overlord.data.database.OverlordDatabase
import com.productions666.overlord.data.database.entity.*
import com.productions666.overlord.data.model.AlarmSource
import com.productions666.overlord.data.model.Place
import com.productions666.overlord.data.model.Route
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * UI state for a scheduled alarm (editable version)
 * This is a UI model that can be customized before scheduling
 */
data class EditableAlarm(
    val id: Long = 0,                           // Temporary ID for UI tracking
    val templateAlarmId: Long? = null,          // Reference to template alarm
    val label: String,
    val scheduledTime: Instant,
    val originalOffsetMinutes: Int,
    val sortOrder: Int,
    val soundSource: AlarmSource = AlarmSource.LOCAL,
    val soundUri: String? = null,
    val soundName: String? = null,
    val requiresUserDismiss: Boolean = true,
    val autoStopAfterMinutes: Int? = null,
    val vibrationEnabled: Boolean = true,
    val isEnabled: Boolean = true,
    val isEdited: Boolean = false               // Track if user modified this alarm
)

/**
 * UI state for profile selection
 */
data class ProfileUiModel(
    val id: Long,
    val name: String,
    val description: String?,
    val alarmCount: Int,
    val firstAlarmLabel: String?,
    val firstAlarmOffset: Int?,                 // Offset in minutes from departure
    val isSystemDefault: Boolean
)

/**
 * Alarm Setup Screen UI State
 */
data class AlarmSetupUiState(
    // Selected route info
    val route: Route? = null,
    val origin: Place? = null,
    val destination: Place? = null,
    
    // Profile selection
    val availableProfiles: List<ProfileUiModel> = emptyList(),
    val selectedProfile: ProfileWithAlarms? = null,
    val isLoadingProfiles: Boolean = true,
    
    // Calculated alarms (editable)
    val alarms: List<EditableAlarm> = emptyList(),
    
    // UI state
    val showProfileSelection: Boolean = false,
    val editingAlarmId: Long? = null,           // Which alarm is being edited
    val showTimeAdjustSheet: Boolean = false,
    val showLabelEditDialog: Boolean = false,
    val showSoundPicker: Boolean = false,
    
    // Scheduling state
    val isScheduling: Boolean = false,
    val schedulingComplete: Boolean = false,
    val scheduledJourneyId: Long? = null,
    val error: String? = null
)

class AlarmSetupViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = OverlordDatabase.getDatabase(application, viewModelScope)
    private val profileDao = database.alarmProfileTemplateDao()
    private val journeyDao = database.scheduledJourneyDao()
    
    private val _uiState = MutableStateFlow(AlarmSetupUiState())
    val uiState: StateFlow<AlarmSetupUiState> = _uiState.asStateFlow()
    
    init {
        loadProfiles()
    }
    
    // =========================================================================
    // Initialization
    // =========================================================================
    
    private fun loadProfiles() {
        viewModelScope.launch {
            profileDao.getAllProfilesWithAlarms().collect { profilesWithAlarms ->
                val profileModels = profilesWithAlarms.map { pwa ->
                    val sortedAlarms = pwa.alarms.sortedBy { it.sortOrder }
                    val firstAlarm = sortedAlarms.firstOrNull()
                    
                    ProfileUiModel(
                        id = pwa.profile.id,
                        name = pwa.profile.name,
                        description = pwa.profile.description,
                        alarmCount = pwa.alarms.size,
                        firstAlarmLabel = firstAlarm?.label,
                        firstAlarmOffset = firstAlarm?.offsetMinutes,
                        isSystemDefault = pwa.profile.isSystemDefault
                    )
                }
                
                _uiState.update { it.copy(
                    availableProfiles = profileModels,
                    isLoadingProfiles = false
                )}
                
                // Check if currently selected profile still exists
                val currentSelectedId = _uiState.value.selectedProfile?.profile?.id
                val updatedProfile = profilesWithAlarms.find { it.profile.id == currentSelectedId }
                
                if (updatedProfile != null) {
                    // Refresh the selected profile with latest data (e.g., new alarms added)
                    selectProfile(updatedProfile)
                } else if (profilesWithAlarms.isNotEmpty()) {
                    // Selected profile was deleted or none selected - auto-select first
                    selectProfile(profilesWithAlarms.first())
                } else {
                    // No profiles left - clear selection and alarms
                    _uiState.update { it.copy(
                        selectedProfile = null,
                        alarms = emptyList()
                    )}
                }
            }
        }
    }
    
    /**
     * Initialize with route data from journey planner
     */
    fun setRouteData(route: Route, origin: Place, destination: Place) {
        _uiState.update { it.copy(
            route = route,
            origin = origin,
            destination = destination
        )}
        
        // Recalculate alarm times if profile already selected
        _uiState.value.selectedProfile?.let { profile ->
            calculateAlarmTimes(profile)
        }
    }
    
    // =========================================================================
    // Profile Selection
    // =========================================================================
    
    fun showProfileSelection() {
        _uiState.update { it.copy(showProfileSelection = true) }
    }
    
    fun dismissProfileSelection() {
        _uiState.update { it.copy(showProfileSelection = false) }
    }
    
    fun selectProfileById(profileId: Long) {
        viewModelScope.launch {
            val profile = profileDao.getProfileWithAlarms(profileId)
            if (profile != null) {
                selectProfile(profile)
            }
        }
        dismissProfileSelection()
    }
    
    private fun selectProfile(profile: ProfileWithAlarms) {
        _uiState.update { it.copy(selectedProfile = profile) }
        calculateAlarmTimes(profile)
    }
    
    private fun calculateAlarmTimes(profile: ProfileWithAlarms) {
        val route = _uiState.value.route ?: return
        val departureTime = route.departureTime
        
        val editableAlarms = profile.alarms
            .sortedBy { it.sortOrder }
            .mapIndexed { index, template ->
                // Calculate actual alarm time based on offset from departure
                val alarmTime = departureTime.plusSeconds(template.offsetMinutes * 60L)
                
                EditableAlarm(
                    id = index.toLong(),            // Temporary UI ID
                    templateAlarmId = template.id,
                    label = template.label,
                    scheduledTime = alarmTime,
                    originalOffsetMinutes = template.offsetMinutes,
                    sortOrder = template.sortOrder,
                    soundSource = template.soundSource,
                    soundUri = template.soundUri,
                    soundName = template.soundName,
                    requiresUserDismiss = template.requiresUserDismiss,
                    autoStopAfterMinutes = template.autoStopAfterMinutes,
                    vibrationEnabled = template.vibrationEnabled,
                    isEnabled = true,
                    isEdited = false
                )
            }
        
        _uiState.update { it.copy(alarms = editableAlarms) }
    }
    
    // =========================================================================
    // Alarm Customization
    // =========================================================================
    
    fun toggleAlarm(alarmId: Long, enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                alarms = state.alarms.map { alarm ->
                    if (alarm.id == alarmId) {
                        alarm.copy(isEnabled = enabled, isEdited = true)
                    } else {
                        alarm
                    }
                }
            )
        }
    }
    
    fun showTimeAdjust(alarmId: Long) {
        _uiState.update { it.copy(
            editingAlarmId = alarmId,
            showTimeAdjustSheet = true
        )}
    }
    
    fun dismissTimeAdjust() {
        _uiState.update { it.copy(
            editingAlarmId = null,
            showTimeAdjustSheet = false
        )}
    }
    
    fun adjustAlarmTime(alarmId: Long, newTime: LocalTime) {
        val route = _uiState.value.route ?: return
        val departureDate = route.departureTime.atZone(ZoneId.systemDefault()).toLocalDate()
        val newInstant = newTime.atDate(departureDate).atZone(ZoneId.systemDefault()).toInstant()
        
        _uiState.update { state ->
            state.copy(
                alarms = state.alarms.map { alarm ->
                    if (alarm.id == alarmId) {
                        alarm.copy(
                            scheduledTime = newInstant,
                            isEdited = true
                        )
                    } else {
                        alarm
                    }
                },
                showTimeAdjustSheet = false,
                editingAlarmId = null
            )
        }
    }
    
    fun adjustAlarmByMinutes(alarmId: Long, minutesDelta: Int) {
        _uiState.update { state ->
            state.copy(
                alarms = state.alarms.map { alarm ->
                    if (alarm.id == alarmId) {
                        alarm.copy(
                            scheduledTime = alarm.scheduledTime.plusSeconds(minutesDelta * 60L),
                            isEdited = true
                        )
                    } else {
                        alarm
                    }
                }
            )
        }
    }
    
    fun showLabelEdit(alarmId: Long) {
        _uiState.update { it.copy(
            editingAlarmId = alarmId,
            showLabelEditDialog = true
        )}
    }
    
    fun dismissLabelEdit() {
        _uiState.update { it.copy(
            editingAlarmId = null,
            showLabelEditDialog = false
        )}
    }
    
    fun updateAlarmLabel(alarmId: Long, newLabel: String) {
        _uiState.update { state ->
            state.copy(
                alarms = state.alarms.map { alarm ->
                    if (alarm.id == alarmId) {
                        alarm.copy(
                            label = newLabel,
                            isEdited = true
                        )
                    } else {
                        alarm
                    }
                },
                showLabelEditDialog = false,
                editingAlarmId = null
            )
        }
    }
    
    // =========================================================================
    // Scheduling
    // =========================================================================
    
    fun scheduleAllAlarms() {
        val state = _uiState.value
        val route = state.route ?: return
        val origin = state.origin ?: return
        val destination = state.destination ?: return
        val profile = state.selectedProfile ?: return
        val enabledAlarms = state.alarms.filter { it.isEnabled }
        
        if (enabledAlarms.isEmpty()) {
            _uiState.update { it.copy(error = "Please enable at least one alarm") }
            return
        }
        
        _uiState.update { it.copy(isScheduling = true, error = null) }
        
        viewModelScope.launch {
            try {
                // Create the journey entity
                val journeyEntity = ScheduledJourneyEntity(
                    originName = origin.name,
                    originAddress = origin.address,
                    originLatitude = origin.latitude,
                    originLongitude = origin.longitude,
                    destinationName = destination.name,
                    destinationAddress = destination.address,
                    destinationLatitude = destination.latitude,
                    destinationLongitude = destination.longitude,
                    routeId = route.id,
                    routeSummary = route.summary,
                    transportModes = route.legs.map { it.type.name }.distinct().joinToString(","),
                    departureTime = route.departureTime.toEpochMilli(),
                    arrivalTime = route.arrivalTime.toEpochMilli(),
                    durationMinutes = route.durationMinutes,
                    profileUsedId = profile.profile.id,
                    profileUsedName = profile.profile.name,
                    status = JourneyStatus.UPCOMING
                )
                
                val journeyId = journeyDao.insertJourney(journeyEntity)
                
                // Create alarm entities
                val alarmEntities = enabledAlarms.map { alarm ->
                    ScheduledAlarmEntity(
                        journeyId = journeyId,
                        templateAlarmId = alarm.templateAlarmId,
                        label = alarm.label,
                        scheduledTime = alarm.scheduledTime.toEpochMilli(),
                        originalOffsetMinutes = alarm.originalOffsetMinutes,
                        sortOrder = alarm.sortOrder,
                        soundSource = alarm.soundSource,
                        soundUri = alarm.soundUri,
                        soundName = alarm.soundName,
                        requiresUserDismiss = alarm.requiresUserDismiss,
                        autoStopAfterMinutes = alarm.autoStopAfterMinutes,
                        vibrationEnabled = alarm.vibrationEnabled,
                        isEnabled = true
                    )
                }
                
                journeyDao.insertAlarms(alarmEntities)
                
                // TODO: Schedule actual Android alarms using AlarmManager
                // This will be handled by the alarm scheduling service
                
                _uiState.update { it.copy(
                    isScheduling = false,
                    schedulingComplete = true,
                    scheduledJourneyId = journeyId
                )}
                
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isScheduling = false,
                    error = e.message ?: "Failed to schedule alarms"
                )}
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun resetState() {
        _uiState.update { AlarmSetupUiState() }
        loadProfiles()
    }
    
    // =========================================================================
    // Helpers
    // =========================================================================
    
    /**
     * Get the alarm currently being edited
     */
    fun getEditingAlarm(): EditableAlarm? {
        val editingId = _uiState.value.editingAlarmId ?: return null
        return _uiState.value.alarms.find { it.id == editingId }
    }
}

// =========================================================================
// Extension Functions
// =========================================================================

/**
 * Format an Instant as a time string (e.g., "07:15 AM")
 */
fun Instant.formatAsTime(): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return this.atZone(ZoneId.systemDefault()).format(formatter)
}

/**
 * Format an Instant as a full date-time string
 */
fun Instant.formatAsDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("EEE d MMM, hh:mm a")
    return this.atZone(ZoneId.systemDefault()).format(formatter)
}

/**
 * Get LocalTime from Instant
 */
fun Instant.toLocalTime(): LocalTime {
    return this.atZone(ZoneId.systemDefault()).toLocalTime()
}

