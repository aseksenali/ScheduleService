package kz.archimedes.scheduleservice.model.query

import kz.archimedes.scheduleservice.model.util.TimeInterval
import kz.archimedes.scheduleservice.model.util.WorkingHours
import java.time.LocalDate
import java.time.LocalTime

data class SpecialCaseDayEntity(
    val date: LocalDate,
    val workingHours: WorkingHours
): TimeInterval<LocalTime>(workingHours.startTime, workingHours.endTime)