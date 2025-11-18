package com.productions666.overlord.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Place(
    val id: String? = null,
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable

