package com.productions666.overlord.presentation.screen

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.productions666.overlord.data.database.OverlordDatabase
import com.productions666.overlord.data.database.entity.AlarmProfileTemplateEntity
import com.productions666.overlord.data.database.entity.AlarmTemplateEntity
import com.productions666.overlord.data.model.AlarmSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Editable alarm for the profile editor
 */
data class EditableProfileAlarm(
    val tempId: Int,                     // Temporary ID for UI
    val label: String,
    val offsetMinutes: Int,              // Negative = before departure (calculated value)
    val soundSource: AlarmSource = AlarmSource.LOCAL,
    val soundUri: String? = null,
    val soundName: String? = null,
    // Dismissal settings
    val requiresUserDismiss: Boolean = true,
    val autoStopAfterMinutes: Int? = null,  // Only used if requiresUserDismiss is false
    // Gap relationship fields - if this alarm was defined relative to another
    val isGapBased: Boolean = false,
    val referenceTempId: Int? = null,    // tempId of the alarm this is relative to
    val gapMinutes: Int? = null,         // Gap from reference (negative = before reference)
    val gapIsBefore: Boolean = true      // True = before reference alarm, False = after
)

/**
 * Simple profile info for template selection
 */
data class ProfileTemplateInfo(
    val id: Long,
    val name: String,
    val alarmCount: Int
)

/**
 * UI state for profile editor
 */
data class ProfileEditorUiState(
    val profileName: String = "",
    val profileDescription: String = "",
    val alarms: List<EditableProfileAlarm> = emptyList(),
    val isEditing: Boolean = false,       // true if editing existing profile
    val existingProfileId: Long? = null,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val savedProfileId: Long? = null,
    val isDeleting: Boolean = false,
    val deleteComplete: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,        // Real-time name validation error
    val showAddAlarmDialog: Boolean = false,
    val editingAlarmIndex: Int? = null,
    // Template selection
    val availableTemplates: List<ProfileTemplateInfo> = emptyList(),
    val selectedTemplateName: String? = null  // Name of template used as starting point
)

/**
 * ViewModel for profile editor
 */
class ProfileEditorViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = OverlordDatabase.getDatabase(application, viewModelScope)
    private val profileDao = database.alarmProfileTemplateDao()
    
    private val _uiState = MutableStateFlow(ProfileEditorUiState())
    val uiState: StateFlow<ProfileEditorUiState> = _uiState.asStateFlow()
    
    private var nextTempId = 1
    
    init {
        // Load available templates
        loadAvailableTemplates()
    }
    
    private fun loadAvailableTemplates() {
        viewModelScope.launch {
            profileDao.getAllProfilesWithAlarms().collect { profiles ->
                val templates = profiles.map { pwa ->
                    ProfileTemplateInfo(
                        id = pwa.profile.id,
                        name = pwa.profile.name,
                        alarmCount = pwa.alarms.size
                    )
                }
                _uiState.update { it.copy(availableTemplates = templates) }
            }
        }
    }
    
    /**
     * Initialize with default alarms for a new profile
     * Sorted with closest to departure (highest offset) at the top
     */
    fun initNewProfile() {
        _uiState.update {
            it.copy(
                profileName = "",
                profileDescription = "",
                alarms = listOf(
                    EditableProfileAlarm(tempId = nextTempId++, label = "Wake Up", offsetMinutes = -60),
                    EditableProfileAlarm(tempId = nextTempId++, label = "Leave Now", offsetMinutes = 0)
                ).sortedByDescending { alarm -> alarm.offsetMinutes },
                isEditing = false,
                existingProfileId = null,
                selectedTemplateName = null
            )
        }
    }
    
    /**
     * Initialize new profile from an existing template
     */
    fun initFromTemplate(templateId: Long) {
        viewModelScope.launch {
            val template = profileDao.getProfileWithAlarms(templateId)
            if (template != null) {
                // First pass: create alarms with tempIds and track sortOrder -> tempId mapping
                val sortOrderToTempId = mutableMapOf<Int, Int>()
                val alarmsWithTempIds = template.alarms.map { alarm ->
                    val tempId = nextTempId++
                    sortOrderToTempId[alarm.sortOrder] = tempId
                    alarm to tempId
                }
                
                // Second pass: create EditableProfileAlarms with correct referenceTempId
                val editableAlarms = alarmsWithTempIds.map { (alarm, tempId) ->
                    val referenceTempId = alarm.referenceAlarmSortOrder?.let { sortOrderToTempId[it] }
                    EditableProfileAlarm(
                        tempId = tempId,
                        label = alarm.label,
                        offsetMinutes = alarm.offsetMinutes,
                        soundSource = alarm.soundSource,
                        soundUri = alarm.soundUri,
                        soundName = alarm.soundName,
                        requiresUserDismiss = alarm.requiresUserDismiss,
                        autoStopAfterMinutes = alarm.autoStopAfterMinutes,
                        isGapBased = alarm.referenceAlarmSortOrder != null && alarm.gapMinutes != null,
                        referenceTempId = referenceTempId,
                        gapMinutes = alarm.gapMinutes,
                        gapIsBefore = alarm.gapIsBefore
                    )
                }.sortedByDescending { it.offsetMinutes }
                
                _uiState.update {
                    it.copy(
                        profileName = "",  // User needs to give it a new name
                        profileDescription = "",
                        alarms = editableAlarms,
                        isEditing = false,
                        existingProfileId = null,
                        selectedTemplateName = template.profile.name
                    )
                }
            }
        }
    }
    
    /**
     * Load existing profile for editing
     * Sorted with closest to departure (highest offset) at the top
     */
    fun loadProfile(profileId: Long) {
        viewModelScope.launch {
            val profile = profileDao.getProfileWithAlarms(profileId)
            if (profile != null) {
                // First pass: create alarms with tempIds and track sortOrder -> tempId mapping
                val sortOrderToTempId = mutableMapOf<Int, Int>()
                val alarmsWithTempIds = profile.alarms.map { alarm ->
                    val tempId = nextTempId++
                    sortOrderToTempId[alarm.sortOrder] = tempId
                    alarm to tempId
                }
                
                // Second pass: create EditableProfileAlarms with correct referenceTempId
                val editableAlarms = alarmsWithTempIds.map { (alarm, tempId) ->
                    val referenceTempId = alarm.referenceAlarmSortOrder?.let { sortOrderToTempId[it] }
                    EditableProfileAlarm(
                        tempId = tempId,
                        label = alarm.label,
                        offsetMinutes = alarm.offsetMinutes,
                        soundSource = alarm.soundSource,
                        soundUri = alarm.soundUri,
                        soundName = alarm.soundName,
                        requiresUserDismiss = alarm.requiresUserDismiss,
                        autoStopAfterMinutes = alarm.autoStopAfterMinutes,
                        isGapBased = alarm.referenceAlarmSortOrder != null && alarm.gapMinutes != null,
                        referenceTempId = referenceTempId,
                        gapMinutes = alarm.gapMinutes,
                        gapIsBefore = alarm.gapIsBefore
                    )
                }.sortedByDescending { it.offsetMinutes }  // Closest to departure first
                
                _uiState.update {
                    it.copy(
                        profileName = profile.profile.name,
                        profileDescription = profile.profile.description ?: "",
                        alarms = editableAlarms,
                        isEditing = true,
                        existingProfileId = profileId
                    )
                }
            }
        }
    }
    
    fun setProfileName(name: String) {
        _uiState.update { it.copy(profileName = name, nameError = null) }
        
        // Validate name for duplicates (async)
        if (name.isNotBlank()) {
            viewModelScope.launch {
                val excludeId = _uiState.value.existingProfileId ?: 0
                val duplicateCount = profileDao.countProfilesWithName(name.trim(), excludeId)
                if (duplicateCount > 0) {
                    _uiState.update { it.copy(nameError = "A profile with this name already exists") }
                }
            }
        }
    }
    
    fun setProfileDescription(description: String) {
        _uiState.update { it.copy(profileDescription = description) }
    }
    
    fun showAddAlarmDialog() {
        _uiState.update { it.copy(showAddAlarmDialog = true, editingAlarmIndex = null) }
    }
    
    fun showEditAlarmDialog(index: Int) {
        _uiState.update { it.copy(showAddAlarmDialog = true, editingAlarmIndex = index) }
    }
    
    fun dismissAlarmDialog() {
        _uiState.update { it.copy(showAddAlarmDialog = false, editingAlarmIndex = null) }
    }
    
    /**
     * Add alarm with absolute offset (from departure)
     */
    fun addAlarm(label: String, offsetMinutes: Int, requiresUserDismiss: Boolean = true, autoStopAfterMinutes: Int? = null) {
        addAlarmWithRelationship(
            label = label,
            offsetMinutes = offsetMinutes,
            requiresUserDismiss = requiresUserDismiss,
            autoStopAfterMinutes = autoStopAfterMinutes,
            isGapBased = false,
            referenceTempId = null,
            gapMinutes = null,
            gapIsBefore = true
        )
    }
    
    /**
     * Add alarm with gap relationship to another alarm
     */
    fun addAlarmWithGap(
        label: String,
        referenceTempId: Int,
        gapMinutes: Int,
        gapIsBefore: Boolean,
        requiresUserDismiss: Boolean = true,
        autoStopAfterMinutes: Int? = null
    ) {
        _uiState.update { state ->
            val referenceAlarm = state.alarms.find { it.tempId == referenceTempId }
            if (referenceAlarm != null) {
                val calculatedOffset = if (gapIsBefore) {
                    referenceAlarm.offsetMinutes - gapMinutes
                } else {
                    referenceAlarm.offsetMinutes + gapMinutes
                }
                
                val newAlarm = EditableProfileAlarm(
                    tempId = nextTempId++,
                    label = label,
                    offsetMinutes = calculatedOffset,
                    requiresUserDismiss = requiresUserDismiss,
                    autoStopAfterMinutes = autoStopAfterMinutes,
                    isGapBased = true,
                    referenceTempId = referenceTempId,
                    gapMinutes = gapMinutes,
                    gapIsBefore = gapIsBefore
                )
                state.copy(
                    alarms = (state.alarms + newAlarm).sortedByDescending { it.offsetMinutes },
                    showAddAlarmDialog = false
                )
            } else {
                state.copy(error = "Reference alarm not found")
            }
        }
    }
    
    private fun addAlarmWithRelationship(
        label: String,
        offsetMinutes: Int,
        requiresUserDismiss: Boolean,
        autoStopAfterMinutes: Int?,
        isGapBased: Boolean,
        referenceTempId: Int?,
        gapMinutes: Int?,
        gapIsBefore: Boolean
    ) {
        _uiState.update { state ->
            val newAlarm = EditableProfileAlarm(
                tempId = nextTempId++,
                label = label,
                offsetMinutes = offsetMinutes,
                requiresUserDismiss = requiresUserDismiss,
                autoStopAfterMinutes = autoStopAfterMinutes,
                isGapBased = isGapBased,
                referenceTempId = referenceTempId,
                gapMinutes = gapMinutes,
                gapIsBefore = gapIsBefore
            )
            state.copy(
                alarms = (state.alarms + newAlarm).sortedByDescending { it.offsetMinutes },
                showAddAlarmDialog = false
            )
        }
    }
    
    /**
     * Update alarm - this also recalculates any alarms that depend on this one
     */
    fun updateAlarm(index: Int, label: String, offsetMinutes: Int, requiresUserDismiss: Boolean = true, autoStopAfterMinutes: Int? = null) {
        _uiState.update { state ->
            val alarmToUpdate = state.alarms.getOrNull(index) ?: return@update state
            val oldTempId = alarmToUpdate.tempId
            
            // Update the alarm itself (clears gap relationship since offset was manually changed)
            var updatedAlarms = state.alarms.toMutableList()
            updatedAlarms[index] = alarmToUpdate.copy(
                label = label,
                offsetMinutes = offsetMinutes,
                requiresUserDismiss = requiresUserDismiss,
                autoStopAfterMinutes = autoStopAfterMinutes,
                isGapBased = false,  // Clear gap relationship when offset is manually set
                referenceTempId = null,
                gapMinutes = null
            )
            
            // Recalculate all alarms that depend on this one
            updatedAlarms = recalculateDependentAlarms(updatedAlarms, oldTempId, offsetMinutes)
            
            state.copy(
                alarms = updatedAlarms.sortedByDescending { it.offsetMinutes },
                showAddAlarmDialog = false,
                editingAlarmIndex = null
            )
        }
    }
    
    /**
     * Update alarm while keeping its gap relationship intact
     */
    fun updateAlarmKeepingRelationship(
        index: Int,
        label: String,
        offsetMinutes: Int,
        isGapBased: Boolean,
        referenceTempId: Int?,
        gapMinutes: Int?,
        gapIsBefore: Boolean,
        requiresUserDismiss: Boolean = true,
        autoStopAfterMinutes: Int? = null
    ) {
        _uiState.update { state ->
            val alarmToUpdate = state.alarms.getOrNull(index) ?: return@update state
            val oldTempId = alarmToUpdate.tempId
            
            var updatedAlarms = state.alarms.toMutableList()
            updatedAlarms[index] = alarmToUpdate.copy(
                label = label,
                offsetMinutes = offsetMinutes,
                requiresUserDismiss = requiresUserDismiss,
                autoStopAfterMinutes = autoStopAfterMinutes,
                isGapBased = isGapBased,
                referenceTempId = referenceTempId,
                gapMinutes = gapMinutes,
                gapIsBefore = gapIsBefore
            )
            
            // Recalculate all alarms that depend on this one
            updatedAlarms = recalculateDependentAlarms(updatedAlarms, oldTempId, offsetMinutes)
            
            state.copy(
                alarms = updatedAlarms.sortedByDescending { it.offsetMinutes },
                showAddAlarmDialog = false,
                editingAlarmIndex = null
            )
        }
    }
    
    /**
     * Recursively recalculate all alarms that depend on the given alarm
     */
    private fun recalculateDependentAlarms(
        alarms: MutableList<EditableProfileAlarm>,
        changedTempId: Int,
        newOffset: Int
    ): MutableList<EditableProfileAlarm> {
        // Find all alarms that reference the changed alarm
        val dependentIndices = alarms.indices.filter { index ->
            alarms[index].isGapBased && alarms[index].referenceTempId == changedTempId
        }
        
        for (depIndex in dependentIndices) {
            val depAlarm = alarms[depIndex]
            val gap = depAlarm.gapMinutes ?: 0
            
            // Calculate new offset based on the changed reference
            val newDepOffset = if (depAlarm.gapIsBefore) {
                newOffset - gap
            } else {
                newOffset + gap
            }
            
            // Update the dependent alarm
            alarms[depIndex] = depAlarm.copy(offsetMinutes = newDepOffset)
            
            // Recursively update any alarms that depend on this one
            recalculateDependentAlarms(alarms, depAlarm.tempId, newDepOffset)
        }
        
        return alarms
    }
    
    fun removeAlarm(index: Int) {
        _uiState.update { state ->
            val alarmToRemove = state.alarms.getOrNull(index)
            val removedTempId = alarmToRemove?.tempId
            
            // Remove the alarm
            var updatedAlarms = state.alarms.filterIndexed { i, _ -> i != index }
            
            // Clear gap relationships for any alarms that referenced the removed one
            if (removedTempId != null) {
                updatedAlarms = updatedAlarms.map { alarm ->
                    if (alarm.referenceTempId == removedTempId) {
                        // Keep the current offset but clear the relationship
                        alarm.copy(
                            isGapBased = false,
                            referenceTempId = null,
                            gapMinutes = null
                        )
                    } else {
                        alarm
                    }
                }
            }
            
            state.copy(alarms = updatedAlarms)
        }
    }
    
    fun saveProfile() {
        val state = _uiState.value
        
        if (state.profileName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a profile name") }
            return
        }
        
        if (state.alarms.isEmpty()) {
            _uiState.update { it.copy(error = "Please add at least one alarm") }
            return
        }
        
        _uiState.update { it.copy(isSaving = true, error = null) }
        
        viewModelScope.launch {
            try {
                // Double-check for duplicate profile name (already validated in real-time)
                val excludeId = if (state.isEditing) state.existingProfileId ?: 0 else 0
                val duplicateCount = profileDao.countProfilesWithName(state.profileName.trim(), excludeId)
                if (duplicateCount > 0) {
                    _uiState.update { it.copy(
                        isSaving = false,
                        nameError = "A profile with this name already exists"
                    )}
                    return@launch
                }
                
                val profileId = if (state.isEditing && state.existingProfileId != null) {
                    // Update existing profile
                    profileDao.updateProfile(
                        AlarmProfileTemplateEntity(
                            id = state.existingProfileId,
                            name = state.profileName.trim(),
                            description = state.profileDescription.trim().ifBlank { null },
                            isSystemDefault = false,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    // Delete old alarms and insert new ones
                    profileDao.deleteAlarmsForProfile(state.existingProfileId)
                    state.existingProfileId
                } else {
                    // Create new profile
                    profileDao.insertProfile(
                        AlarmProfileTemplateEntity(
                            name = state.profileName.trim(),
                            description = state.profileDescription.trim().ifBlank { null },
                            isSystemDefault = false
                        )
                    )
                }
                
                // Create a map of tempId to sortOrder for gap relationships
                val tempIdToSortOrder = state.alarms.mapIndexed { index, alarm ->
                    alarm.tempId to (index + 1)
                }.toMap()
                
                // Insert alarms with gap relationship data
                val alarmEntities = state.alarms.mapIndexed { index, alarm ->
                    // Find the sortOrder of the reference alarm (if gap-based)
                    val referenceSortOrder = if (alarm.isGapBased && alarm.referenceTempId != null) {
                        tempIdToSortOrder[alarm.referenceTempId]
                    } else {
                        null
                    }
                    
                    AlarmTemplateEntity(
                        profileId = profileId,
                        label = alarm.label,
                        offsetMinutes = alarm.offsetMinutes,
                        sortOrder = index + 1,
                        soundSource = alarm.soundSource,
                        soundUri = alarm.soundUri,
                        soundName = alarm.soundName,
                        requiresUserDismiss = alarm.requiresUserDismiss,
                        autoStopAfterMinutes = alarm.autoStopAfterMinutes,
                        referenceAlarmSortOrder = referenceSortOrder,
                        gapMinutes = if (alarm.isGapBased) alarm.gapMinutes else null,
                        gapIsBefore = alarm.gapIsBefore
                    )
                }
                profileDao.insertAlarms(alarmEntities)
                
                _uiState.update { it.copy(
                    isSaving = false,
                    saveComplete = true,
                    savedProfileId = profileId
                )}
                
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save profile"
                )}
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }
    
    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }
    
    fun deleteProfile() {
        val state = _uiState.value
        
        if (!state.isEditing || state.existingProfileId == null) {
            return
        }
        
        _uiState.update { it.copy(isDeleting = true, showDeleteConfirmation = false) }
        
        viewModelScope.launch {
            try {
                // Delete alarms first, then the profile
                profileDao.deleteAlarmsForProfile(state.existingProfileId)
                profileDao.deleteProfileById(state.existingProfileId)
                
                _uiState.update { it.copy(
                    isDeleting = false,
                    deleteComplete = true
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isDeleting = false,
                    error = e.message ?: "Failed to delete profile"
                )}
            }
        }
    }
}

/**
 * Profile Editor Screen
 * 
 * Allows users to create or edit alarm profile templates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    viewModel: ProfileEditorViewModel = viewModel(),
    existingProfileId: Long? = null,
    onSaveComplete: (Long) -> Unit,
    onDelete: () -> Unit = {},
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Initialize on first composition
    LaunchedEffect(existingProfileId) {
        if (existingProfileId != null) {
            viewModel.loadProfile(existingProfileId)
        } else {
            viewModel.initNewProfile()
        }
    }
    
    // Handle save complete
    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete && uiState.savedProfileId != null) {
            onSaveComplete(uiState.savedProfileId!!)
        }
    }
    
    // Handle delete complete
    LaunchedEffect(uiState.deleteComplete) {
        if (uiState.deleteComplete) {
            onDelete()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Edit Profile" else "New Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    // Delete button (only when editing)
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = { viewModel.showDeleteConfirmation() },
                            enabled = !uiState.isSaving && !uiState.isDeleting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete profile",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = { viewModel.saveProfile() },
                        enabled = !uiState.isSaving && !uiState.isDeleting && uiState.profileName.isNotBlank() && uiState.alarms.isNotEmpty() && uiState.nameError == null
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Template selector (only for new profiles)
            if (!uiState.isEditing && uiState.availableTemplates.isNotEmpty()) {
                var showTemplateDropdown by remember { mutableStateOf(false) }
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Start from",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.selectedTemplateName == null,
                            onClick = { viewModel.initNewProfile() },
                            label = { Text("Scratch") }
                        )
                        
                        Box {
                            FilterChip(
                                selected = uiState.selectedTemplateName != null,
                                onClick = { showTemplateDropdown = true },
                                label = { 
                                    Text(uiState.selectedTemplateName ?: "Existing Profile")
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            
                            DropdownMenu(
                                expanded = showTemplateDropdown,
                                onDismissRequest = { showTemplateDropdown = false }
                            ) {
                                uiState.availableTemplates.forEach { template ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(template.name)
                                                Text(
                                                    text = "${template.alarmCount} alarms",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.initFromTemplate(template.id)
                                            showTemplateDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Profile Name
            OutlinedTextField(
                value = uiState.profileName,
                onValueChange = { viewModel.setProfileName(it) },
                label = { Text("Profile Name") },
                placeholder = { Text("e.g., Morning Routine") },
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = if (uiState.nameError != null) {
                    { Text(uiState.nameError!!, color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Profile Description (optional)
            OutlinedTextField(
                value = uiState.profileDescription,
                onValueChange = { viewModel.setProfileDescription(it) },
                label = { Text("Description (optional)") },
                placeholder = { Text("e.g., For early mornings with prep time") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Alarms Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alarms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(onClick = { viewModel.showAddAlarmDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Alarm")
                }
            }
            
            // Column headers (only show if there are alarms)
            var showColumnHelp by remember { mutableStateOf(false) }
            
            if (uiState.alarms.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(32.dp)) // Icon space
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Interval",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.width(56.dp)
                    )
                    Text(
                        text = "Depart",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.width(52.dp)
                    )
                    // Help icon
                    IconButton(
                        onClick = { showColumnHelp = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Help dialog
            if (showColumnHelp) {
                AlertDialog(
                    onDismissRequest = { showColumnHelp = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = { Text("Understanding the columns") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text(
                                    text = "Depart",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Minutes before (-) or after (+) your departure time. For example, -60m means this alarm fires 60 minutes before you need to leave.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column {
                                Text(
                                    text = "Interval",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Minutes between this alarm and the one above it. Helps you see how much time you have between each alarm.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column {
                                Text(
                                    text = "Linked Alarms",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Alarms with a 🔗 icon are linked to another alarm. When the reference alarm changes, linked alarms automatically adjust to maintain their gap.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showColumnHelp = false }) {
                            Text("Got it")
                        }
                    }
                )
            }
            
            // Alarms List
            if (uiState.alarms.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No alarms yet. Tap \"Add Alarm\" to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.alarms) { index, alarm ->
                        // Calculate gap from PREVIOUS alarm (previous in list = closer to departure)
                        val previousAlarm = uiState.alarms.getOrNull(index - 1)
                        val gapFromPrevious = if (previousAlarm != null) {
                            // Gap is absolute difference between this alarm and previous
                            // e.g., previous = 0m (Leave Now), current = -30m → gap = 30
                            kotlin.math.abs(alarm.offsetMinutes - previousAlarm.offsetMinutes)
                        } else {
                            // First item (closest to departure) has no gap
                            0
                        }
                        
                        ProfileAlarmItem(
                            alarm = alarm,
                            allAlarms = uiState.alarms,
                            gapFromPrevious = gapFromPrevious,
                            onTap = { viewModel.showEditAlarmDialog(index) }
                        )
                    }
                }
            }
            
            // Error display
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Delete Profile Confirmation Dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Profile?") },
            text = {
                Text("Are you sure you want to delete \"${uiState.profileName}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteProfile() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Add/Edit Alarm Dialog
    if (uiState.showAddAlarmDialog) {
        AddEditAlarmDialog(
            existingAlarm = uiState.editingAlarmIndex?.let { uiState.alarms.getOrNull(it) },
            allAlarms = uiState.alarms,
            editingIndex = uiState.editingAlarmIndex,
            isEditing = uiState.editingAlarmIndex != null,
            onDismiss = { viewModel.dismissAlarmDialog() },
            onSaveOffset = { label, offsetMinutes, requiresUserDismiss, autoStopAfterMinutes ->
                if (uiState.editingAlarmIndex != null) {
                    viewModel.updateAlarm(uiState.editingAlarmIndex!!, label, offsetMinutes, requiresUserDismiss, autoStopAfterMinutes)
                } else {
                    viewModel.addAlarm(label, offsetMinutes, requiresUserDismiss, autoStopAfterMinutes)
                }
            },
            onSaveGap = { label, referenceTempId, gapMinutes, gapIsBefore, calculatedOffset, requiresUserDismiss, autoStopAfterMinutes ->
                if (uiState.editingAlarmIndex != null) {
                    viewModel.updateAlarmKeepingRelationship(
                        index = uiState.editingAlarmIndex!!,
                        label = label,
                        offsetMinutes = calculatedOffset,
                        isGapBased = true,
                        referenceTempId = referenceTempId,
                        gapMinutes = gapMinutes,
                        gapIsBefore = gapIsBefore,
                        requiresUserDismiss = requiresUserDismiss,
                        autoStopAfterMinutes = autoStopAfterMinutes
                    )
                } else {
                    viewModel.addAlarmWithGap(label, referenceTempId, gapMinutes, gapIsBefore, requiresUserDismiss, autoStopAfterMinutes)
                }
            },
            onDelete = {
                uiState.editingAlarmIndex?.let { index ->
                    viewModel.removeAlarm(index)
                    viewModel.dismissAlarmDialog()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileAlarmItem(
    alarm: EditableProfileAlarm,
    allAlarms: List<EditableProfileAlarm>,
    gapFromPrevious: Int,
    onTap: () -> Unit
) {
    // Find the reference alarm name if this is gap-based
    val referenceAlarmName = if (alarm.isGapBased && alarm.referenceTempId != null) {
        allAlarms.find { it.tempId == alarm.referenceTempId }?.label
    } else {
        null
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onTap
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon column - dismissal type icon, with link icon below if gap-based
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dismissal type icon (TouchApp or Timer) in yellow
                Icon(
                    imageVector = if (alarm.requiresUserDismiss) 
                        Icons.Default.TouchApp 
                    else 
                        Icons.Default.Timer,
                    contentDescription = if (alarm.requiresUserDismiss) 
                        "Requires manual dismiss" 
                    else 
                        "Auto-stops after ${alarm.autoStopAfterMinutes ?: 1} min",
                    tint = Color(0xFFFFD54F), // Yellow
                    modifier = Modifier.size(20.dp)
                )
                // Link icon if gap-based
                if (alarm.isGapBased) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Linked alarm",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Alarm label and relationship info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                // Show relationship if gap-based
                if (referenceAlarmName != null && alarm.gapMinutes != null) {
                    Text(
                        text = "${alarm.gapMinutes}m ${if (alarm.gapIsBefore) "before" else "after"} $referenceAlarmName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            // Gap from previous alarm column
            Text(
                text = "${gapFromPrevious}m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.width(56.dp)
            )
            
            // Minutes to departure column (far right)
            Text(
                text = formatOffsetCompact(alarm.offsetMinutes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(52.dp)
            )
        }
    }
}

/**
 * Format offset as compact string for the column display
 * Always in minutes, e.g., "-60m", "0m", "+15m"
 */
private fun formatOffsetCompact(offsetMinutes: Int): String {
    return when {
        offsetMinutes == 0 -> "0m"
        offsetMinutes > 0 -> "+${offsetMinutes}m"
        else -> "${offsetMinutes}m"
    }
}

/**
 * Input mode for specifying alarm time
 */
private enum class AlarmInputMode {
    OFFSET,  // Minutes from departure
    GAP      // Minutes from another alarm
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditAlarmDialog(
    existingAlarm: EditableProfileAlarm?,
    allAlarms: List<EditableProfileAlarm>,
    editingIndex: Int?,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSaveOffset: (label: String, offsetMinutes: Int, requiresUserDismiss: Boolean, autoStopAfterMinutes: Int?) -> Unit,
    onSaveGap: (label: String, referenceTempId: Int, gapMinutes: Int, gapIsBefore: Boolean, calculatedOffset: Int, requiresUserDismiss: Boolean, autoStopAfterMinutes: Int?) -> Unit,
    onDelete: () -> Unit
) {
    var label by remember(existingAlarm) { mutableStateOf(existingAlarm?.label ?: "") }
    
    // Dismissal settings
    var requiresUserDismiss by remember(existingAlarm) { 
        mutableStateOf(existingAlarm?.requiresUserDismiss ?: true) 
    }
    var autoStopText by remember(existingAlarm) { 
        mutableStateOf(existingAlarm?.autoStopAfterMinutes?.toString() ?: "1") 
    }
    
    // Available alarms for reference (exclude the one being edited)
    val availableReferences = remember(allAlarms, editingIndex) {
        allAlarms.filterIndexed { index, _ -> index != editingIndex }
    }
    
    // Input mode - default to GAP if editing a gap-based alarm
    var inputMode by remember(existingAlarm) { 
        mutableStateOf(
            if (existingAlarm?.isGapBased == true) AlarmInputMode.GAP else AlarmInputMode.OFFSET
        ) 
    }
    
    // Offset mode state
    var offsetText by remember(existingAlarm) { 
        mutableStateOf(
            if (existingAlarm != null && !existingAlarm.isGapBased) {
                kotlin.math.abs(existingAlarm.offsetMinutes).toString()
            } else {
                "30"
            }
        ) 
    }
    var isBefore by remember(existingAlarm) { 
        mutableStateOf(existingAlarm?.offsetMinutes?.let { it <= 0 } ?: true) 
    }
    
    // Gap mode state - initialize from existing alarm if it's gap-based
    var gapText by remember(existingAlarm) { 
        mutableStateOf(existingAlarm?.gapMinutes?.toString() ?: "15") 
    }
    var gapIsBefore by remember(existingAlarm) { 
        mutableStateOf(existingAlarm?.gapIsBefore ?: true) 
    }
    
    // Find the index in availableReferences for the existing reference
    var selectedReferenceIndex by remember(existingAlarm, availableReferences) { 
        mutableStateOf(
            if (existingAlarm?.referenceTempId != null) {
                availableReferences.indexOfFirst { it.tempId == existingAlarm.referenceTempId }
                    .takeIf { it >= 0 }
            } else {
                null
            }
        )
    }
    var showReferenceDropdown by remember { mutableStateOf(false) }
    
    // Calculate the final offset based on mode
    val calculatedOffset = remember(inputMode, offsetText, isBefore, gapText, gapIsBefore, selectedReferenceIndex, availableReferences) {
        when (inputMode) {
            AlarmInputMode.OFFSET -> {
                val minutes = offsetText.toIntOrNull() ?: 0
                if (isBefore) -minutes else minutes
            }
            AlarmInputMode.GAP -> {
                val referenceAlarm = selectedReferenceIndex?.let { availableReferences.getOrNull(it) }
                if (referenceAlarm != null) {
                    val gap = gapText.toIntOrNull() ?: 0
                    // Before means earlier alarm (more negative), After means later (closer to departure)
                    if (gapIsBefore) {
                        referenceAlarm.offsetMinutes - gap
                    } else {
                        referenceAlarm.offsetMinutes + gap
                    }
                } else {
                    null
                }
            }
        }
    }
    
    // Get the selected reference alarm for saving
    val selectedReferenceAlarm = selectedReferenceIndex?.let { availableReferences.getOrNull(it) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Edit Alarm" else "Add Alarm")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Label input
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    placeholder = { Text("e.g., Wake Up, Pack Bag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Mode selector (only show if there are other alarms to reference)
                if (availableReferences.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Relative to",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(
                                selected = inputMode == AlarmInputMode.OFFSET,
                                onClick = { inputMode = AlarmInputMode.OFFSET },
                                label = { Text("Departure") }
                            )
                            FilterChip(
                                selected = inputMode == AlarmInputMode.GAP,
                                onClick = { inputMode = AlarmInputMode.GAP },
                                label = { Text("Alarm") }
                            )
                        }
                    }
                }
                
                when (inputMode) {
                    AlarmInputMode.OFFSET -> {
                        // Minutes input with Before/After on the right
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = offsetText,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        offsetText = newValue
                                    }
                                },
                                label = { Text("Minutes") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(100.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Before/After stacked to match text field height
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = isBefore,
                                    onClick = { isBefore = true },
                                    label = { 
                                        Text(
                                            "Before",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        ) 
                                    },
                                    modifier = Modifier.height(32.dp).width(80.dp)
                                )
                                FilterChip(
                                    selected = !isBefore,
                                    onClick = { isBefore = false },
                                    label = { 
                                        Text(
                                            "After",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        ) 
                                    },
                                    modifier = Modifier.height(32.dp).width(80.dp)
                                )
                            }
                        }
                        
                        // Preview text
                        Text(
                            text = "Fires ${offsetText.ifEmpty { "0" }}m ${if (isBefore) "before" else "after"} departure",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    AlarmInputMode.GAP -> {
                        // Reference alarm selector
                        ExposedDropdownMenuBox(
                            expanded = showReferenceDropdown,
                            onExpandedChange = { showReferenceDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = selectedReferenceIndex?.let { 
                                    availableReferences.getOrNull(it)?.label 
                                } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Reference Alarm") },
                                placeholder = { Text("Select an alarm") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showReferenceDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showReferenceDropdown,
                                onDismissRequest = { showReferenceDropdown = false }
                            ) {
                                availableReferences.forEachIndexed { index, alarm ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(alarm.label)
                                                Text(
                                                    text = formatOffsetCompact(alarm.offsetMinutes),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedReferenceIndex = index
                                            showReferenceDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Gap input with Before/After on the right
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = gapText,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        gapText = newValue
                                    }
                                },
                                label = { Text("Gap (min)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(100.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Before/After stacked
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = gapIsBefore,
                                    onClick = { gapIsBefore = true },
                                    label = { 
                                        Text(
                                            "Before",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        ) 
                                    },
                                    modifier = Modifier.height(32.dp).width(80.dp)
                                )
                                FilterChip(
                                    selected = !gapIsBefore,
                                    onClick = { gapIsBefore = false },
                                    label = { 
                                        Text(
                                            "After",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        ) 
                                    },
                                    modifier = Modifier.height(32.dp).width(80.dp)
                                )
                            }
                        }
                        
                        // Preview text
                        val refAlarm = selectedReferenceIndex?.let { availableReferences.getOrNull(it) }
                        if (refAlarm != null && calculatedOffset != null) {
                            Text(
                                text = "${gapText.ifEmpty { "0" }}m ${if (gapIsBefore) "before" else "after"} \"${refAlarm.label}\" → ${formatOffsetCompact(calculatedOffset)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Text(
                                text = "Select a reference alarm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Dismissal settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Requires manual dismiss",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = requiresUserDismiss,
                        onCheckedChange = { requiresUserDismiss = it }
                    )
                }
                
                // Auto-stop duration (only shown if not requiring manual dismiss)
                if (!requiresUserDismiss) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Auto-stop after",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = autoStopText,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 2)) {
                                    autoStopText = newValue
                                }
                            },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
                
                // Delete button (only when editing)
                if (isEditing) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Alarm")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    calculatedOffset?.let { offset ->
                        val autoStop = if (!requiresUserDismiss) {
                            autoStopText.toIntOrNull()?.coerceIn(1, 30) ?: 1
                        } else null
                        
                        when (inputMode) {
                            AlarmInputMode.OFFSET -> {
                                onSaveOffset(label.trim(), offset, requiresUserDismiss, autoStop)
                            }
                            AlarmInputMode.GAP -> {
                                selectedReferenceAlarm?.let { refAlarm ->
                                    val gap = gapText.toIntOrNull() ?: 0
                                    onSaveGap(
                                        label.trim(),
                                        refAlarm.tempId,
                                        gap,
                                        gapIsBefore,
                                        offset,
                                        requiresUserDismiss,
                                        autoStop
                                    )
                                }
                            }
                        }
                    }
                },
                enabled = label.isNotBlank() && calculatedOffset != null && 
                    (inputMode == AlarmInputMode.OFFSET || selectedReferenceAlarm != null)
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


