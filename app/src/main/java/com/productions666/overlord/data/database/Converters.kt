package com.productions666.overlord.data.database

import androidx.room.TypeConverter
import com.productions666.overlord.data.database.entity.JourneyStatus
import com.productions666.overlord.data.model.AlarmSource
import com.productions666.overlord.data.model.AlarmType

class Converters {
    @TypeConverter
    fun fromAlarmType(value: AlarmType): String {
        return value.name
    }

    @TypeConverter
    fun toAlarmType(value: String): AlarmType {
        return AlarmType.valueOf(value)
    }

    @TypeConverter
    fun fromAlarmSource(value: AlarmSource): String {
        return value.name
    }

    @TypeConverter
    fun toAlarmSource(value: String): AlarmSource {
        return AlarmSource.valueOf(value)
    }
    
    @TypeConverter
    fun fromJourneyStatus(value: JourneyStatus): String {
        return value.name
    }

    @TypeConverter
    fun toJourneyStatus(value: String): JourneyStatus {
        return JourneyStatus.valueOf(value)
    }
}

