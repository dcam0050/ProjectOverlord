package com.productions666.overlord.data.database

import android.content.Context
import com.productions666.overlord.data.database.entity.AlarmProfileEntity
import com.productions666.overlord.data.model.AlarmSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object DatabaseInitializer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize(context: Context) {
        val db = OverlordDatabase.getDatabase(context)

        scope.launch {
            try {
                // Check if profiles already exist
                val existingProfiles = db.alarmProfileDao().getAllProfiles().first()

                // If no profiles exist, create default ones
                if (existingProfiles.isEmpty()) {
                    val defaultProfiles = listOf(
                        AlarmProfileEntity(
                            id = 0,
                            name = "Default Alarm",
                            source = AlarmSource.LOCAL,
                            localResName = "alarm_klaxon",
                            spotifyUri = null
                        ),
                        AlarmProfileEntity(
                            id = 0,
                            name = "Gentle Wake",
                            source = AlarmSource.LOCAL,
                            localResName = "alarm_beep",
                            spotifyUri = null
                        )
                    )

                    defaultProfiles.forEach { profile ->
                        db.alarmProfileDao().insertProfile(profile)
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash - profiles can be created later
                android.util.Log.e("DatabaseInitializer", "Error initializing database", e)
            }
        }
    }
}

