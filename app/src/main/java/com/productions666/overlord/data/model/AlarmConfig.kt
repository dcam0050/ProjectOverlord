package com.productions666.overlord.data.model

data class AlarmConfig(
    val wakeUpOffsetMinutes: Int,
    val getOutOfBedOffsetMinutes: Int,
    val leaveHomeOffsetMinutes: Int,
    val defaultWakeUpProfileId: Long? = null,
    val defaultGetOutOfBedProfileId: Long? = null,
    val defaultLeaveHomeProfileId: Long? = null
)

