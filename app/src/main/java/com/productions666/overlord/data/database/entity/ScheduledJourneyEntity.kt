package com.productions666.overlord.data.database.entity

import androidx.room.*
import com.productions666.overlord.data.model.AlarmSource

/**
 * Journey Status
 */
enum class JourneyStatus {
    UPCOMING,      // Future journey, alarms scheduled
    IN_PROGRESS,   // Currently happening (some alarms fired)
    COMPLETED,     // All alarms done, journey complete
    CANCELLED      // User cancelled
}

/**
 * Scheduled Journey Entity
 * 
 * A journey with all its scheduled alarms.
 * Created when user selects a route and profile.
 */
@Entity(tableName = "scheduled_journeys")
data class ScheduledJourneyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Origin
    val originName: String,
    val originAddress: String? = null,
    val originPlaceId: String? = null,
    val originLatitude: Double? = null,
    val originLongitude: Double? = null,
    
    // Destination
    val destinationName: String,
    val destinationAddress: String? = null,
    val destinationPlaceId: String? = null,
    val destinationLatitude: Double? = null,
    val destinationLongitude: Double? = null,
    
    // Route info
    val routeId: String? = null,
    val routeSummary: String,                   // "Train via Kings Cross"
    val transportModes: String,                 // JSON array: ["train", "walk"]
    val departureTime: Long,                    // Epoch millis
    val arrivalTime: Long,                      // Epoch millis
    val durationMinutes: Int,
    
    // Profile used (snapshot of name at time of scheduling)
    val profileUsedId: Long? = null,
    val profileUsedName: String,
    
    // Status
    val status: JourneyStatus = JourneyStatus.UPCOMING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Scheduled Alarm Entity
 * 
 * Individual alarm scheduled for a journey.
 * Can be customized from the template (label, time, sound).
 */
@Entity(
    tableName = "scheduled_alarms",
    foreignKeys = [
        ForeignKey(
            entity = ScheduledJourneyEntity::class,
            parentColumns = ["id"],
            childColumns = ["journeyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["journeyId"]), Index(value = ["scheduledTime"])]
)
data class ScheduledAlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journeyId: Long,
    val templateAlarmId: Long? = null,          // Null if custom alarm added for this journey
    
    // Alarm details (can be customized from template)
    val label: String,                          // Can be edited by user
    val scheduledTime: Long,                    // Epoch millis - actual alarm time
    val originalOffsetMinutes: Int,             // Original offset from template
    val sortOrder: Int,
    
    // Sound (can be customized per alarm)
    val soundSource: AlarmSource = AlarmSource.LOCAL,
    val soundUri: String? = null,
    val soundName: String? = null,
    
    // Behavior
    val requiresUserDismiss: Boolean = true,
    val autoStopAfterMinutes: Int? = null,
    val vibrationEnabled: Boolean = true,
    
    // State
    val isEnabled: Boolean = true,              // User can toggle off
    val isFired: Boolean = false,               // Has the alarm fired?
    val firedAt: Long? = null,                  // When did it fire?
    val dismissedAt: Long? = null,              // When was it dismissed?
    val snoozedUntil: Long? = null              // If snoozed, until when?
)

/**
 * Journey with all its scheduled alarms
 */
data class JourneyWithAlarms(
    @Embedded val journey: ScheduledJourneyEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "journeyId"
    )
    val alarms: List<ScheduledAlarmEntity>
)

