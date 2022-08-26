package kz.archimedes.scheduleservice.dto.relocation

import kz.archimedes.scheduleservice.model.util.DaySchedule
import java.time.LocalDate
import java.util.*

data class CreateRelocationDTO(val toBranch: UUID, val startDate: LocalDate, val endDate: LocalDate, val workingHours: DaySchedule): RelocationDTO