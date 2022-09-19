package kz.archimedes.scheduleservice.dto.relocation

import kz.archimedes.scheduleservice.model.util.WeekSchedule
import java.time.LocalDate
import java.util.*

data class CreateRelocationDTO(
    val branchId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val workingHours: WeekSchedule
) : RelocationDTO