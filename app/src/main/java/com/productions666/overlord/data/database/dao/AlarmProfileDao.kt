package com.productions666.overlord.data.database.dao

import androidx.room.*
import com.productions666.overlord.data.database.entity.AlarmProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmProfileDao {
    @Query("SELECT * FROM alarm_profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<AlarmProfileEntity>>

    @Query("SELECT * FROM alarm_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): AlarmProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: AlarmProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: AlarmProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: AlarmProfileEntity)
}

