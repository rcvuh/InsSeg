package com.pascm.inspseg.data.local

import androidx.room.TypeConverter
import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.domain.model.InspectionModule
import com.pascm.inspseg.domain.model.InspectionStatus
import com.pascm.inspseg.domain.model.ZoneRequirement

/** Conversores de enums a texto para persistirlos de forma legible en Room. */
class Converters {
    @TypeConverter fun toModule(v: String) = InspectionModule.valueOf(v)
    @TypeConverter fun fromModule(v: InspectionModule) = v.name

    @TypeConverter fun toCriticality(v: String) = CriticalityLevel.valueOf(v)
    @TypeConverter fun fromCriticality(v: CriticalityLevel) = v.name

    @TypeConverter fun toAnswer(v: String) = AnswerResult.valueOf(v)
    @TypeConverter fun fromAnswer(v: AnswerResult) = v.name

    @TypeConverter fun toIncidenceStatus(v: String) = IncidenceStatus.valueOf(v)
    @TypeConverter fun fromIncidenceStatus(v: IncidenceStatus) = v.name

    @TypeConverter fun toInspectionStatus(v: String) = InspectionStatus.valueOf(v)
    @TypeConverter fun fromInspectionStatus(v: InspectionStatus) = v.name

    @TypeConverter fun toZoneRequirement(v: String) = ZoneRequirement.valueOf(v)
    @TypeConverter fun fromZoneRequirement(v: ZoneRequirement) = v.name
}
