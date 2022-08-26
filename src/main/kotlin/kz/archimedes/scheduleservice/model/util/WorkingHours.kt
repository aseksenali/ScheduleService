package kz.archimedes.scheduleservice.model.util

import java.time.LocalTime

data class WorkingHours(
    val startTime: LocalTime,
    val endTime: LocalTime
): TimeInterval<LocalTime>(startTime, endTime)