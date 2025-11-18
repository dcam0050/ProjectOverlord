package com.productions666.overlord.data.database.dao

import androidx.room.*
import com.productions666.overlord.data.database.entity.JourneyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {
    @Query("SELECT * FROM journeys ORDER BY createdAt DESC")
    fun getAllJourneys(): Flow<List<JourneyEntity>>

    @Query("SELECT * FROM journeys WHERE id = :id")
    suspend fun getJourneyById(id: Long): JourneyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: JourneyEntity): Long

    @Delete
    suspend fun deleteJourney(journey: JourneyEntity)

    @Query("DELETE FROM journeys WHERE id = :id")
    suspend fun deleteJourneyById(id: Long)
}

