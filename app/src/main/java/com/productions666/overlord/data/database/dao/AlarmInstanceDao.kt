package com.productions666.overlord.data.database.dao

import androidx.room.*
import com.productions666.overlord.data.database.entity.AlarmInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmInstanceDao {
    @Query("SELECT * FROM alarm_instances WHERE isActive = 1 ORDER BY scheduledTime ASC")
    fun getActiveAlarms(): Flow<List<AlarmInstanceEntity>>

    @Query("SELECT * FROM alarm_instances ORDER BY scheduledTime DESC")
    fun getAllAlarms(): Flow<List<AlarmInstanceEntity>>

    @Query("SELECT * FROM alarm_instances WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmInstanceEntity?

    @Query("SELECT * FROM alarm_instances WHERE scheduledTime >= :fromTime AND isActive = 1 ORDER BY scheduledTime ASC")
    suspend fun getFutureAlarms(fromTime: Long): List<AlarmInstanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmInstanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<AlarmInstanceEntity>)

    @Update
    suspend fun updateAlarm(alarm: AlarmInstanceEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmInstanceEntity)

    @Query("UPDATE alarm_instances SET isActive = 0, acknowledgedAt = :acknowledgedAt WHERE id = :id")
    suspend fun deactivateAlarm(id: Long, acknowledgedAt: Long? = null)

    @Query("UPDATE alarm_instances SET cancelledAt = :cancelledAt WHERE id = :id")
    suspend fun cancelAlarm(id: Long, cancelledAt: Long)

    @Query("UPDATE alarm_instances SET isActive = 0 WHERE journeyId = :journeyId")
    suspend fun deactivateAlarmsForJourney(journeyId: Long)

    @Query("DELETE FROM alarm_instances WHERE scheduledTime < :beforeTime AND isActive = 0")
    suspend fun deleteOldInactiveAlarms(beforeTime: Long)

    @Query("DELETE FROM alarm_instances")
    suspend fun deleteAllAlarms()
}

