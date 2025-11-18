package com.productions666.overlord.domain.usecase

import com.productions666.overlord.data.model.AlarmConfig
import com.productions666.overlord.data.model.AlarmType
import com.productions666.overlord.domain.model.AlarmInstance
import com.productions666.overlord.domain.model.Journey
import java.time.Instant
import java.time.temporal.ChronoUnit

class CreateAlarmScheduleForJourney {
    operator fun invoke(
        journey: Journey,
        route: com.productions666.overlord.data.model.Route,
        config: AlarmConfig
    ): List<AlarmInstance> {
        val firstTransitDeparture = route.departureTime
        val alarms = mutableListOf<AlarmInstance>()

        // Wake up alarm
        val wakeUpTime = firstTransitDeparture.minus(config.wakeUpOffsetMinutes.toLong(), ChronoUnit.MINUTES)
        alarms.add(
            AlarmInstance(
                id = 0, // Will be set by database
                journeyId = journey.id,
                scheduledTime = wakeUpTime,
                type = AlarmType.WAKE_UP,
                profileId = config.defaultWakeUpProfileId ?: 1L,
                requiresUserDismiss = true,
                autoStopAfterMillis = null,
                label = "${route.summary} - ${firstTransitDeparture}"
            )
        )

        // Get out of bed alarm
        val getOutOfBedTime = firstTransitDeparture.minus(config.getOutOfBedOffsetMinutes.toLong(), ChronoUnit.MINUTES)
        alarms.add(
            AlarmInstance(
                id = 0,
                journeyId = journey.id,
                scheduledTime = getOutOfBedTime,
                type = AlarmType.GET_OUT_OF_BED,
                profileId = config.defaultGetOutOfBedProfileId ?: 1L,
                requiresUserDismiss = true,
                autoStopAfterMillis = null,
                label = "${route.summary} - ${firstTransitDeparture}"
            )
        )

        // Leave home alarm
        val leaveHomeTime = firstTransitDeparture.minus(config.leaveHomeOffsetMinutes.toLong(), ChronoUnit.MINUTES)
        alarms.add(
            AlarmInstance(
                id = 0,
                journeyId = journey.id,
                scheduledTime = leaveHomeTime,
                type = AlarmType.LEAVE_HOME,
                profileId = config.defaultLeaveHomeProfileId ?: 1L,
                requiresUserDismiss = false, // Can auto-stop
                autoStopAfterMillis = 5 * 60 * 1000L, // 5 minutes
                label = "${route.summary} - ${firstTransitDeparture}"
            )
        )

        return alarms.sortedBy { it.scheduledTime }
    }
}

