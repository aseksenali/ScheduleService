package kz.archimedes.scheduleservice.model.query

import kz.archimedes.scheduleservice.model.util.TimeInterval
import lombok.With
import org.springframework.data.annotation.TypeAlias
import java.time.LocalDate

@TypeAlias("holiday")
@With
data class HolidayEntity(
    val startDate: LocalDate, val endDate: LocalDate
) : TimeInterval<LocalDate>(startDate, endDate)