package com.productions666.overlord.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.productions666.overlord.data.database.dao.*
import com.productions666.overlord.data.database.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        // Legacy entities (kept for migration)
        JourneyEntity::class,
        AlarmInstanceEntity::class,
        AlarmProfileEntity::class,
        // New entities
        AlarmProfileTemplateEntity::class,
        AlarmTemplateEntity::class,
        ScheduledJourneyEntity::class,
        ScheduledAlarmEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OverlordDatabase : RoomDatabase() {
    // Legacy DAOs
    abstract fun journeyDao(): JourneyDao
    abstract fun alarmInstanceDao(): AlarmInstanceDao
    abstract fun alarmProfileDao(): AlarmProfileDao
    
    // New DAOs
    abstract fun alarmProfileTemplateDao(): AlarmProfileTemplateDao
    abstract fun scheduledJourneyDao(): ScheduledJourneyDao

    companion object {
        @Volatile
        private var INSTANCE: OverlordDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE alarm_instances ADD COLUMN acknowledgedAt INTEGER")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create alarm_profile_templates table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS alarm_profile_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        isSystemDefault INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // Create alarm_templates table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS alarm_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        label TEXT NOT NULL,
                        offsetMinutes INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        soundSource TEXT NOT NULL DEFAULT 'LOCAL',
                        soundUri TEXT,
                        soundName TEXT,
                        requiresUserDismiss INTEGER NOT NULL DEFAULT 1,
                        autoStopAfterMinutes INTEGER,
                        vibrationEnabled INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY (profileId) REFERENCES alarm_profile_templates(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_alarm_templates_profileId ON alarm_templates(profileId)")
                
                // Create scheduled_journeys table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS scheduled_journeys (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        originName TEXT NOT NULL,
                        originAddress TEXT,
                        originPlaceId TEXT,
                        originLatitude REAL,
                        originLongitude REAL,
                        destinationName TEXT NOT NULL,
                        destinationAddress TEXT,
                        destinationPlaceId TEXT,
                        destinationLatitude REAL,
                        destinationLongitude REAL,
                        routeId TEXT,
                        routeSummary TEXT NOT NULL,
                        transportModes TEXT NOT NULL,
                        departureTime INTEGER NOT NULL,
                        arrivalTime INTEGER NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        profileUsedId INTEGER,
                        profileUsedName TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'UPCOMING',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // Create scheduled_alarms table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS scheduled_alarms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        journeyId INTEGER NOT NULL,
                        templateAlarmId INTEGER,
                        label TEXT NOT NULL,
                        scheduledTime INTEGER NOT NULL,
                        originalOffsetMinutes INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        soundSource TEXT NOT NULL DEFAULT 'LOCAL',
                        soundUri TEXT,
                        soundName TEXT,
                        requiresUserDismiss INTEGER NOT NULL DEFAULT 1,
                        autoStopAfterMinutes INTEGER,
                        vibrationEnabled INTEGER NOT NULL DEFAULT 1,
                        isEnabled INTEGER NOT NULL DEFAULT 1,
                        isFired INTEGER NOT NULL DEFAULT 0,
                        firedAt INTEGER,
                        dismissedAt INTEGER,
                        snoozedUntil INTEGER,
                        FOREIGN KEY (journeyId) REFERENCES scheduled_journeys(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_alarms_journeyId ON scheduled_alarms(journeyId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_alarms_scheduledTime ON scheduled_alarms(scheduledTime)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope? = null): OverlordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OverlordDatabase::class.java,
                    "overlord_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Database callback to seed default profiles on first create
     */
    private class DatabaseCallback(
        private val scope: CoroutineScope?
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope?.launch(Dispatchers.IO) {
                INSTANCE?.let { database ->
                    seedDefaultProfiles(database.alarmProfileTemplateDao())
                }
            }
        }
        
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Check if we need to seed profiles (for migration case)
            scope?.launch(Dispatchers.IO) {
                INSTANCE?.let { database ->
                    val count = database.alarmProfileTemplateDao().getDefaultProfileCount()
                    if (count == 0) {
                        seedDefaultProfiles(database.alarmProfileTemplateDao())
                    }
                }
            }
        }
        
        private suspend fun seedDefaultProfiles(dao: AlarmProfileTemplateDao) {
            // Standard Morning Profile
            val standardMorningId = dao.insertProfile(
                AlarmProfileTemplateEntity(
                    name = "Standard Morning",
                    description = "Basic wake up and leave routine",
                    isSystemDefault = true
                )
            )
            dao.insertAlarms(listOf(
                AlarmTemplateEntity(profileId = standardMorningId, label = "Wake Up", offsetMinutes = -90, sortOrder = 1),
                AlarmTemplateEntity(profileId = standardMorningId, label = "Get Out of Bed", offsetMinutes = -75, sortOrder = 2),
                AlarmTemplateEntity(profileId = standardMorningId, label = "Leave Home", offsetMinutes = -10, sortOrder = 3)
            ))
            
            // Full Morning Routine Profile
            val fullMorningId = dao.insertProfile(
                AlarmProfileTemplateEntity(
                    name = "Full Morning Routine",
                    description = "For mornings with shower and prep time",
                    isSystemDefault = true
                )
            )
            dao.insertAlarms(listOf(
                AlarmTemplateEntity(profileId = fullMorningId, label = "Wake Up", offsetMinutes = -120, sortOrder = 1),
                AlarmTemplateEntity(profileId = fullMorningId, label = "Get Out of Bed", offsetMinutes = -105, sortOrder = 2),
                AlarmTemplateEntity(profileId = fullMorningId, label = "Start Shower", offsetMinutes = -90, sortOrder = 3),
                AlarmTemplateEntity(profileId = fullMorningId, label = "Getting Ready", offsetMinutes = -60, sortOrder = 4),
                AlarmTemplateEntity(profileId = fullMorningId, label = "Pack Reminder", offsetMinutes = -30, sortOrder = 5),
                AlarmTemplateEntity(profileId = fullMorningId, label = "10-Min Warning", offsetMinutes = -10, sortOrder = 6),
                AlarmTemplateEntity(profileId = fullMorningId, label = "Leave Now!", offsetMinutes = 0, sortOrder = 7)
            ))
            
            // Quick Trip Profile
            val quickTripId = dao.insertProfile(
                AlarmProfileTemplateEntity(
                    name = "Quick Trip",
                    description = "When you're already ready, just need a reminder",
                    isSystemDefault = true
                )
            )
            dao.insertAlarms(listOf(
                AlarmTemplateEntity(profileId = quickTripId, label = "30-Min Warning", offsetMinutes = -30, sortOrder = 1),
                AlarmTemplateEntity(profileId = quickTripId, label = "Leave Now", offsetMinutes = 0, sortOrder = 2)
            ))
        }
    }
}
