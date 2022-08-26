package kz.archimedes.scheduleservice.dto.relocation

import java.time.LocalDate
import java.util.*

data class DeleteRelocationDTO(val toBranch: UUID, val startDate: LocalDate, val endDate: LocalDate) : RelocationDTO