package com.productions666.overlord.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.productions666.overlord.data.model.Place
import com.productions666.overlord.data.model.Route
import java.time.Instant

@Entity(tableName = "journeys")
data class JourneyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originName: String,
    val originAddress: String?,
    val originLatitude: Double?,
    val originLongitude: Double?,
    val destinationName: String,
    val destinationAddress: String?,
    val destinationLatitude: Double?,
    val destinationLongitude: Double?,
    val routeId: String,
    val routeSummary: String,
    val departureTime: Long, // Instant as epoch millis
    val arrivalTime: Long,
    val createdAt: Long = System.currentTimeMillis()
)

fun JourneyEntity.toDomain(): com.productions666.overlord.domain.model.Journey {
    return com.productions666.overlord.domain.model.Journey(
        id = id,
        origin = Place(
            name = originName,
            address = originAddress,
            latitude = originLatitude,
            longitude = originLongitude
        ),
        destination = Place(
            name = destinationName,
            address = destinationAddress,
            latitude = destinationLatitude,
            longitude = destinationLongitude
        ),
        selectedRoute = null, // Will be loaded separately if needed
        departureTime = Instant.ofEpochMilli(departureTime)
    )
}

