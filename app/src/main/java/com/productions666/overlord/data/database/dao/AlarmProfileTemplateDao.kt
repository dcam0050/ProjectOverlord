package com.productions666.overlord.data.database.dao

import androidx.room.*
import com.productions666.overlord.data.database.entity.AlarmProfileTemplateEntity
import com.productions666.overlord.data.database.entity.AlarmTemplateEntity
import com.productions666.overlord.data.database.entity.ProfileWithAlarms
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmProfileTemplateDao {
    
    // =========================================================================
    // PROFILE TEMPLATES
    // =========================================================================
    
    @Query("SELECT * FROM alarm_profile_templates ORDER BY isSystemDefault DESC, name ASC")
    fun getAllProfiles(): Flow<List<AlarmProfileTemplateEntity>>
    
    @Query("SELECT * FROM alarm_profile_templates WHERE id = :profileId")
    suspend fun getProfileById(profileId: Long): AlarmProfileTemplateEntity?
    
    @Query("SELECT * FROM alarm_profile_templates WHERE id = :profileId")
    fun getProfileByIdFlow(profileId: Long): Flow<AlarmProfileTemplateEntity?>
    
    @Transaction
    @Query("SELECT * FROM alarm_profile_templates WHERE id = :profileId")
    suspend fun getProfileWithAlarms(profileId: Long): ProfileWithAlarms?
    
    @Transaction
    @Query("SELECT * FROM alarm_profile_templates WHERE id = :profileId")
    fun getProfileWithAlarmsFlow(profileId: Long): Flow<ProfileWithAlarms?>
    
    @Transaction
    @Query("SELECT * FROM alarm_profile_templates ORDER BY isSystemDefault DESC, name ASC")
    fun getAllProfilesWithAlarms(): Flow<List<ProfileWithAlarms>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: AlarmProfileTemplateEntity): Long
    
    @Update
    suspend fun updateProfile(profile: AlarmProfileTemplateEntity)
    
    @Delete
    suspend fun deleteProfile(profile: AlarmProfileTemplateEntity)
    
    @Query("DELETE FROM alarm_profile_templates WHERE id = :profileId")
    suspend fun deleteProfileById(profileId: Long)
    
    // =========================================================================
    // ALARM TEMPLATES
    // =========================================================================
    
    @Query("SELECT * FROM alarm_templates WHERE profileId = :profileId ORDER BY sortOrder ASC")
    fun getAlarmsForProfile(profileId: Long): Flow<List<AlarmTemplateEntity>>
    
    @Query("SELECT * FROM alarm_templates WHERE profileId = :profileId ORDER BY sortOrder ASC")
    suspend fun getAlarmsForProfileSync(profileId: Long): List<AlarmTemplateEntity>
    
    @Query("SELECT * FROM alarm_templates WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Long): AlarmTemplateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmTemplateEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<AlarmTemplateEntity>)
    
    @Update
    suspend fun updateAlarm(alarm: AlarmTemplateEntity)
    
    @Delete
    suspend fun deleteAlarm(alarm: AlarmTemplateEntity)
    
    @Query("DELETE FROM alarm_templates WHERE profileId = :profileId")
    suspend fun deleteAlarmsForProfile(profileId: Long)
    
    // =========================================================================
    // UTILITY
    // =========================================================================
    
    /**
     * Duplicate a profile with all its alarms
     */
    @Transaction
    suspend fun duplicateProfile(
        sourceProfileId: Long,
        newName: String
    ): Long {
        val sourceProfile = getProfileById(sourceProfileId) ?: return -1
        val sourceAlarms = getAlarmsForProfileSync(sourceProfileId)
        
        // Create new profile
        val newProfile = sourceProfile.copy(
            id = 0,
            name = newName,
            isSystemDefault = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newProfileId = insertProfile(newProfile)
        
        // Copy all alarms
        val newAlarms = sourceAlarms.map { alarm ->
            alarm.copy(
                id = 0,
                profileId = newProfileId
            )
        }
        insertAlarms(newAlarms)
        
        return newProfileId
    }
    
    /**
     * Check if default profiles exist
     */
    @Query("SELECT COUNT(*) FROM alarm_profile_templates WHERE isSystemDefault = 1")
    suspend fun getDefaultProfileCount(): Int
}

