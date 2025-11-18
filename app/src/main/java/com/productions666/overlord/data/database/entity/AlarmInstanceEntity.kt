package com.productions666.overlord.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.productions666.overlord.data.model.AlarmType
import java.time.Instant

@Entity(
    tableName = "alarm_instances",
    foreignKeys = [
        ForeignKey(
            entity = JourneyEntity::class,
            parentColumns = ["id"],
            childColumns = ["journeyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AlarmProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["journeyId"]), Index(value = ["scheduledTime"])]
)
data class AlarmInstanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journeyId: Long? = null,
    val scheduledTime: Long, // Instant as epoch millis
    val type: AlarmType,
    val profileId: Long,
    val requiresUserDismiss: Boolean,
    val autoStopAfterMillis: Long? = null,
    val label: String,
    val isActive: Boolean = true,
    val acknowledgedAt: Long? = null, // Instant as epoch millis when user acknowledged/dismissed
    val cancelledAt: Long? = null // Instant as epoch millis when alarm was cancelled
)

fun AlarmInstanceEntity.toDomain(): com.productions666.overlord.domain.model.AlarmInstance {
    return com.productions666.overlord.domain.model.AlarmInstance(
        id = id,
        journeyId = journeyId,
        scheduledTime = Instant.ofEpochMilli(scheduledTime),
        type = type,
        profileId = profileId,
        requiresUserDismiss = requiresUserDismiss,
        autoStopAfterMillis = autoStopAfterMillis,
        label = label
    )
}

