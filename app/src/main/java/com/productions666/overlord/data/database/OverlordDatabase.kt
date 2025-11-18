package com.productions666.overlord.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.productions666.overlord.data.database.dao.AlarmInstanceDao
import com.productions666.overlord.data.database.dao.AlarmProfileDao
import com.productions666.overlord.data.database.dao.JourneyDao
import com.productions666.overlord.data.database.entity.AlarmInstanceEntity
import com.productions666.overlord.data.database.entity.AlarmProfileEntity
import com.productions666.overlord.data.database.entity.JourneyEntity

@Database(
    entities = [
        JourneyEntity::class,
        AlarmInstanceEntity::class,
        AlarmProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OverlordDatabase : RoomDatabase() {
    abstract fun journeyDao(): JourneyDao
    abstract fun alarmInstanceDao(): AlarmInstanceDao
    abstract fun alarmProfileDao(): AlarmProfileDao

    companion object {
        @Volatile
        private var INSTANCE: OverlordDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE alarm_instances ADD COLUMN acknowledgedAt INTEGER")
            }
        }

        fun getDatabase(context: Context): OverlordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OverlordDatabase::class.java,
                    "overlord_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

