package kz.archimedes.scheduleservice.model.query

import kz.archimedes.scheduleservice.model.util.TimeInterval
import lombok.With
import org.springframework.data.annotation.TypeAlias
import java.time.LocalDate
import java.util.*

@TypeAlias("holiday")
@With
data class HolidayEntity(
    val branchId: UUID, val startDate: LocalDate, val endDate: LocalDate
) : TimeInterval<LocalDate>(startDate, endDate)