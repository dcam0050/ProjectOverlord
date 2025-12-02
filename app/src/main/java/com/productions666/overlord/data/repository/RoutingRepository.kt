package com.productions666.overlord.data.repository

import com.productions666.overlord.data.model.Leg
import com.productions666.overlord.data.model.LegType
import com.productions666.overlord.data.model.Place
import com.productions666.overlord.data.model.Route
import com.productions666.overlord.data.model.Stop
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

/**
 * Repository for Google Maps routing API.
 * Currently mocked for development - will be replaced with real API calls.
 */
class RoutingRepository {
    
    /**
     * Find transit routes between origin and destination.
     * 
     * @param origin Starting location
     * @param destination End location
     * @param arriveBy Target arrival time (null for "leave now" or "depart at")
     * @param departAt Target departure time (null for "leave now" or "arrive by")
     * @return List of available transit routes
     */
    suspend fun findTransitRoutes(
        origin: Place,
        destination: Place,
        arriveBy: Instant? = null,
        departAt: Instant? = null
    ): Result<List<Route>> {
        // Simulate API call delay
        delay(1500)
        
        return try {
            // Generate 3-5 mock routes
            val routeCount = Random.nextInt(3, 6)
            val routes = (1..routeCount).map { index ->
                generateMockRoute(
                    origin = origin,
                    destination = destination,
                    routeIndex = index,
                    arriveBy = arriveBy,
                    departAt = departAt
                )
            }.sortedBy { it.departureTime }
            
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate a mock transit route for testing.
     */
    private fun generateMockRoute(
        origin: Place,
        destination: Place,
        routeIndex: Int,
        arriveBy: Instant?,
        departAt: Instant?
    ): Route {
        val now = Instant.now()
        val baseTime = departAt ?: now
        
        // Vary departure times by route index (15-45 minute intervals)
        val departureOffsetMinutes = routeIndex * 15 + Random.nextInt(0, 10)
        val departureTime = baseTime.plusSeconds(departureOffsetMinutes * 60L)
        
        // Generate route duration (30-90 minutes)
        val totalDurationMinutes = 30 + routeIndex * 10 + Random.nextInt(0, 20)
        val arrivalTime = departureTime.plusSeconds(totalDurationMinutes * 60L)
        
        // Generate legs
        val legs = mutableListOf<Leg>()
        
        // First leg: walking to first stop (5-15 minutes)
        val walkToStopMinutes = 5 + Random.nextInt(0, 10)
        legs.add(
            Leg(
                type = LegType.WALK,
                from = Stop(
                    name = origin.name,
                    latitude = origin.latitude ?: 0.0,
                    longitude = origin.longitude ?: 0.0
                ),
                to = Stop(
                    name = "Transit Stop ${routeIndex}",
                    latitude = (origin.latitude ?: 0.0) + Random.nextDouble(-0.01, 0.01),
                    longitude = (origin.longitude ?: 0.0) + Random.nextDouble(-0.01, 0.01)
                ),
                durationMinutes = walkToStopMinutes,
                departureTime = departureTime,
                arrivalTime = departureTime.plusSeconds(walkToStopMinutes * 60L)
            )
        )
        
        // Transit legs (1-3 legs)
        val transitLegCount = Random.nextInt(1, 4)
        var currentTime = departureTime.plusSeconds(walkToStopMinutes * 60L)
        var remainingDuration = totalDurationMinutes - walkToStopMinutes
        
        repeat(transitLegCount) { legIndex ->
            val legDuration = remainingDuration / (transitLegCount - legIndex)
            val legType = when (Random.nextInt(0, 4)) {
                0 -> LegType.BUS
                1 -> LegType.TRAIN
                2 -> LegType.SUBWAY
                else -> LegType.TRAM
            }
            
            val lineName = when (legType) {
                LegType.BUS -> "Bus ${Random.nextInt(1, 500)}"
                LegType.TRAIN -> "Train ${Random.nextInt(1, 20)}"
                LegType.SUBWAY -> "Line ${Random.nextInt(1, 10)}"
                LegType.TRAM -> "Tram ${Random.nextInt(1, 10)}"
                else -> null
            }
            
            val fromStop = Stop(
                name = if (legIndex == 0) "Transit Stop ${routeIndex}" else "Stop ${legIndex}",
                latitude = (origin.latitude ?: 0.0) + Random.nextDouble(-0.05, 0.05),
                longitude = (origin.longitude ?: 0.0) + Random.nextDouble(-0.05, 0.05)
            )
            
            val toStop = if (legIndex == transitLegCount - 1) {
                // Last stop near destination
                Stop(
                    name = destination.name,
                    latitude = destination.latitude ?: 0.0,
                    longitude = destination.longitude ?: 0.0
                )
            } else {
                Stop(
                    name = "Stop ${legIndex + 1}",
                    latitude = (destination.latitude ?: 0.0) + Random.nextDouble(-0.05, 0.05),
                    longitude = (destination.longitude ?: 0.0) + Random.nextDouble(-0.05, 0.05)
                )
            }
            
            val legArrivalTime = currentTime.plusSeconds(legDuration * 60L)
            
            legs.add(
                Leg(
                    type = legType,
                    from = fromStop,
                    to = toStop,
                    durationMinutes = legDuration,
                    lineName = lineName,
                    departureTime = currentTime,
                    arrivalTime = legArrivalTime
                )
            )
            
            currentTime = legArrivalTime
            remainingDuration -= legDuration
        }
        
        // Final leg: walking from last stop to destination (3-10 minutes)
        val walkFromStopMinutes = 3 + Random.nextInt(0, 7)
        legs.add(
            Leg(
                type = LegType.WALK,
                from = legs.last().to,
                to = Stop(
                    name = destination.name,
                    latitude = destination.latitude ?: 0.0,
                    longitude = destination.longitude ?: 0.0
                ),
                durationMinutes = walkFromStopMinutes,
                departureTime = currentTime,
                arrivalTime = arrivalTime
            )
        )
        
        // Generate summary
        val transitLegs = legs.filter { it.type != LegType.WALK }
        val summary = when {
            transitLegs.isEmpty() -> "Walking route"
            transitLegs.size == 1 -> transitLegs.first().lineName ?: "Direct transit"
            else -> "${transitLegs.size} transfers"
        }
        
        return Route(
            id = "route_${routeIndex}_${departureTime.epochSecond}",
            summary = summary,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            legs = legs,
            durationMinutes = totalDurationMinutes + walkFromStopMinutes,
            walkingDurationToFirstStopMinutes = walkToStopMinutes
        )
    }
}



