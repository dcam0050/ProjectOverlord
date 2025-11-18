package com.productions666.overlord.domain.model

import com.productions666.overlord.data.model.AlarmType
import java.time.Instant

data class AlarmInstance(
    val id: Long,
    val journeyId: Long?,
    val scheduledTime: Instant,
    val type: AlarmType,
    val profileId: Long,
    val requiresUserDismiss: Boolean,
    val autoStopAfterMillis: Long?,
    val label: String
)

