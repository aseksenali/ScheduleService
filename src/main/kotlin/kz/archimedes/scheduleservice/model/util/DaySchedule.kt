package kz.archimedes.scheduleservice.model.util

import java.time.DayOfWeek
import java.time.DayOfWeek.*
import java.util.*

data class DaySchedule(
    val specialtyId: UUID,
    val days: Map<DayOfWeek, List<WorkingHours>> = mapOf(
        MONDAY to listOf(),
        TUESDAY to listOf(),
        WEDNESDAY to listOf(),
        THURSDAY to listOf(),
        FRIDAY to listOf(),
        SATURDAY to listOf(),
        SUNDAY to listOf(),
    )
)