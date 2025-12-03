package com.productions666.overlord.data.database.entity

import androidx.room.*
import com.productions666.overlord.data.model.AlarmSource

/**
 * Alarm Profile Template Entity
 * 
 * A profile is a TEMPLATE containing multiple alarm templates.
 * Each profile can be used for different journey types (e.g., "Full Morning Routine", "Quick Trip")
 */
@Entity(tableName = "alarm_profile_templates")
data class AlarmProfileTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val isSystemDefault: Boolean = false,  // Pre-loaded default profiles
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Alarm Template Entity
 * 
 * Individual alarm within a profile template.
 * Defines the alarm's offset from departure, label, and sound.
 */
@Entity(
    tableName = "alarm_templates",
    foreignKeys = [
        ForeignKey(
            entity = AlarmProfileTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class AlarmTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val label: String,                          // "Wake Up", "Pack Reminder"
    val offsetMinutes: Int,                     // Negative = before departure (e.g., -90)
    val sortOrder: Int,                         // Display order within profile
    val soundSource: AlarmSource = AlarmSource.LOCAL,
    val soundUri: String? = null,               // File path or Spotify URI
    val soundName: String? = null,              // Display name for the sound
    val requiresUserDismiss: Boolean = true,
    val autoStopAfterMinutes: Int? = null,      // Null = never auto-stop
    val vibrationEnabled: Boolean = true
)

/**
 * Profile with all its alarm templates
 */
data class ProfileWithAlarms(
    @Embedded val profile: AlarmProfileTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "profileId"
    )
    val alarms: List<AlarmTemplateEntity>
)

