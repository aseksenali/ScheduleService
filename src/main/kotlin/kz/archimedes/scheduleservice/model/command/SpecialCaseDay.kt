package kz.archimedes.scheduleservice.model.command

import kz.archimedes.scheduleservice.model.util.TimeInterval
import kz.archimedes.scheduleservice.model.util.WorkingHours
import java.time.LocalDate
import java.time.LocalTime

data class SpecialCaseDay(
    var date: LocalDate,
    var workingHours: WorkingHours
) : TimeInterval<LocalTime>(workingHours.startTime, workingHours.endTime)