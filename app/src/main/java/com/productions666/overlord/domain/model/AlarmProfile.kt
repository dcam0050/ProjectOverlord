package com.productions666.overlord.domain.model

import com.productions666.overlord.data.model.AlarmSource

data class AlarmProfile(
    val id: Long,
    val name: String,
    val source: AlarmSource,
    val localResName: String?,
    val spotifyUri: String?
)

