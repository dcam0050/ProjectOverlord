package com.productions666.overlord.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.productions666.overlord.data.model.AlarmSource

@Entity(tableName = "alarm_profiles")
data class AlarmProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val source: AlarmSource,
    val localResName: String? = null,
    val spotifyUri: String? = null
)

fun AlarmProfileEntity.toDomain(): com.productions666.overlord.domain.model.AlarmProfile {
    return com.productions666.overlord.domain.model.AlarmProfile(
        id = id,
        name = name,
        source = source,
        localResName = localResName,
        spotifyUri = spotifyUri
    )
}

