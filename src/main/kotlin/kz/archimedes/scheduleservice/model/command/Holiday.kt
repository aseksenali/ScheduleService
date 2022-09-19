package kz.archimedes.scheduleservice.model.command

import kz.archimedes.scheduleservice.model.util.TimeInterval
import java.time.LocalDate

data class Holiday(
    var startDate: LocalDate,
    var endDate: LocalDate
) : TimeInterval<LocalDate>(startDate, endDate)