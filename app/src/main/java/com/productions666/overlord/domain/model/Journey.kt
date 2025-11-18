package com.productions666.overlord.domain.model

import com.productions666.overlord.data.model.Place
import com.productions666.overlord.data.model.Route
import java.time.Instant

data class Journey(
    val id: Long,
    val origin: Place,
    val destination: Place,
    val selectedRoute: Route?,
    val departureTime: Instant
)

