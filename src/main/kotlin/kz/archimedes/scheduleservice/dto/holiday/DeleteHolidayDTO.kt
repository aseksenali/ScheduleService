package kz.archimedes.scheduleservice.dto.holiday

import kz.archimedes.api.command.DeleteHolidayCommand
import java.time.LocalDate
import java.util.*

data class DeleteHolidayDTO(
    val branchId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate
): HolidayDTO<DeleteHolidayCommand>