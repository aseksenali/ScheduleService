package kz.archimedes.scheduleservice.dto.specialcase

import kz.archimedes.scheduleservice.model.util.WeekSchedule
import java.time.LocalDate
import java.util.*

data class CreateSpecialCaseDayDTO(
    val branchId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val workingSchedule: WeekSchedule
) : SpecialCaseDayDTO