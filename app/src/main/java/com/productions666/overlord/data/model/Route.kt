package com.productions666.overlord.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
data class Route(
    val id: String,
    val summary: String,
    val departureTime: Instant,
    val arrivalTime: Instant,
    val legs: List<Leg>,
    val durationMinutes: Int,
    val walkingDurationToFirstStopMinutes: Int
) : Parcelable

enum class LegType {
    WALK,
    BUS,
    TRAIN,
    TRAM,
    SUBWAY,
    FERRY,
    OTHER
}

@Parcelize
data class Leg(
    val type: LegType,
    val from: Stop?,
    val to: Stop?,
    val durationMinutes: Int,
    val lineName: String? = null,
    val departureTime: Instant? = null,
    val arrivalTime: Instant? = null
) : Parcelable

@Parcelize
data class Stop(
    val name: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable

