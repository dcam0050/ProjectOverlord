package com.productions666.overlord.data.database.dao

import androidx.room.*
import com.productions666.overlord.data.database.entity.JourneyStatus
import com.productions666.overlord.data.database.entity.JourneyWithAlarms
import com.productions666.overlord.data.database.entity.ScheduledAlarmEntity
import com.productions666.overlord.data.database.entity.ScheduledJourneyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledJourneyDao {
    
    // =========================================================================
    // JOURNEYS
    // =========================================================================
    
    @Query("SELECT * FROM scheduled_journeys ORDER BY departureTime ASC")
    fun getAllJourneys(): Flow<List<ScheduledJourneyEntity>>
    
    @Query("SELECT * FROM scheduled_journeys WHERE status = :status ORDER BY departureTime ASC")
    fun getJourneysByStatus(status: JourneyStatus): Flow<List<ScheduledJourneyEntity>>
    
    @Query("""
        SELECT * FROM scheduled_journeys 
        WHERE status IN ('UPCOMING', 'IN_PROGRESS') 
        ORDER BY departureTime ASC
    """)
    fun getActiveJourneys(): Flow<List<ScheduledJourneyEntity>>
    
    @Query("SELECT * FROM scheduled_journeys WHERE id = :journeyId")
    suspend fun getJourneyById(journeyId: Long): ScheduledJourneyEntity?
    
    @Query("SELECT * FROM scheduled_journeys WHERE id = :journeyId")
    fun getJourneyByIdFlow(journeyId: Long): Flow<ScheduledJourneyEntity?>
    
    @Transaction
    @Query("SELECT * FROM scheduled_journeys WHERE id = :journeyId")
    suspend fun getJourneyWithAlarms(journeyId: Long): JourneyWithAlarms?
    
    @Transaction
    @Query("SELECT * FROM scheduled_journeys WHERE id = :journeyId")
    fun getJourneyWithAlarmsFlow(journeyId: Long): Flow<JourneyWithAlarms?>
    
    @Transaction
    @Query("""
        SELECT * FROM scheduled_journeys 
        WHERE status IN ('UPCOMING', 'IN_PROGRESS') 
        ORDER BY departureTime ASC
    """)
    fun getActiveJourneysWithAlarms(): Flow<List<JourneyWithAlarms>>
    
    @Transaction
    @Query("SELECT * FROM scheduled_journeys ORDER BY departureTime DESC")
    fun getAllJourneysWithAlarms(): Flow<List<JourneyWithAlarms>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: ScheduledJourneyEntity): Long
    
    @Update
    suspend fun updateJourney(journey: ScheduledJourneyEntity)
    
    @Delete
    suspend fun deleteJourney(journey: ScheduledJourneyEntity)
    
    @Query("DELETE FROM scheduled_journeys WHERE id = :journeyId")
    suspend fun deleteJourneyById(journeyId: Long)
    
    @Query("UPDATE scheduled_journeys SET status = :status, updatedAt = :updatedAt WHERE id = :journeyId")
    suspend fun updateJourneyStatus(journeyId: Long, status: JourneyStatus, updatedAt: Long = System.currentTimeMillis())
    
    // =========================================================================
    // SCHEDULED ALARMS
    // =========================================================================
    
    @Query("SELECT * FROM scheduled_alarms WHERE journeyId = :journeyId ORDER BY sortOrder ASC")
    fun getAlarmsForJourney(journeyId: Long): Flow<List<ScheduledAlarmEntity>>
    
    @Query("SELECT * FROM scheduled_alarms WHERE journeyId = :journeyId ORDER BY sortOrder ASC")
    suspend fun getAlarmsForJourneySync(journeyId: Long): List<ScheduledAlarmEntity>
    
    @Query("SELECT * FROM scheduled_alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Long): ScheduledAlarmEntity?
    
    @Query("""
        SELECT * FROM scheduled_alarms 
        WHERE isEnabled = 1 AND isFired = 0 
        ORDER BY scheduledTime ASC 
        LIMIT 1
    """)
    suspend fun getNextPendingAlarm(): ScheduledAlarmEntity?
    
    @Query("""
        SELECT * FROM scheduled_alarms 
        WHERE isEnabled = 1 AND isFired = 0 
        ORDER BY scheduledTime ASC
    """)
    fun getAllPendingAlarms(): Flow<List<ScheduledAlarmEntity>>
    
    @Query("""
        SELECT sa.* FROM scheduled_alarms sa
        INNER JOIN scheduled_journeys sj ON sa.journeyId = sj.id
        WHERE sa.isEnabled = 1 
        AND sa.isFired = 0 
        AND sj.status IN ('UPCOMING', 'IN_PROGRESS')
        ORDER BY sa.scheduledTime ASC
    """)
    fun getActiveAlarmsForActiveJourneys(): Flow<List<ScheduledAlarmEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: ScheduledAlarmEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<ScheduledAlarmEntity>)
    
    @Update
    suspend fun updateAlarm(alarm: ScheduledAlarmEntity)
    
    @Delete
    suspend fun deleteAlarm(alarm: ScheduledAlarmEntity)
    
    @Query("DELETE FROM scheduled_alarms WHERE journeyId = :journeyId")
    suspend fun deleteAlarmsForJourney(journeyId: Long)
    
    // =========================================================================
    // ALARM STATE UPDATES
    // =========================================================================
    
    @Query("UPDATE scheduled_alarms SET isEnabled = :enabled WHERE id = :alarmId")
    suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean)
    
    @Query("UPDATE scheduled_alarms SET isFired = 1, firedAt = :firedAt WHERE id = :alarmId")
    suspend fun markAlarmAsFired(alarmId: Long, firedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE scheduled_alarms SET dismissedAt = :dismissedAt WHERE id = :alarmId")
    suspend fun markAlarmAsDismissed(alarmId: Long, dismissedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE scheduled_alarms SET snoozedUntil = :snoozedUntil WHERE id = :alarmId")
    suspend fun snoozeAlarm(alarmId: Long, snoozedUntil: Long)
    
    @Query("UPDATE scheduled_alarms SET scheduledTime = :newTime WHERE id = :alarmId")
    suspend fun updateAlarmTime(alarmId: Long, newTime: Long)
    
    @Query("UPDATE scheduled_alarms SET label = :newLabel WHERE id = :alarmId")
    suspend fun updateAlarmLabel(alarmId: Long, newLabel: String)
    
    @Query("""
        UPDATE scheduled_alarms 
        SET soundSource = :source, soundUri = :uri, soundName = :name 
        WHERE id = :alarmId
    """)
    suspend fun updateAlarmSound(
        alarmId: Long,
        source: String,
        uri: String?,
        name: String?
    )
    
    // =========================================================================
    // CLEANUP
    // =========================================================================
    
    @Query("""
        DELETE FROM scheduled_journeys 
        WHERE status = 'COMPLETED' 
        AND updatedAt < :olderThan
    """)
    suspend fun deleteOldCompletedJourneys(olderThan: Long)
    
    @Query("""
        UPDATE scheduled_journeys 
        SET status = 'COMPLETED', updatedAt = :now
        WHERE status = 'IN_PROGRESS'
        AND departureTime < :cutoffTime
    """)
    suspend fun markOldJourneysAsCompleted(cutoffTime: Long, now: Long = System.currentTimeMillis())
}

