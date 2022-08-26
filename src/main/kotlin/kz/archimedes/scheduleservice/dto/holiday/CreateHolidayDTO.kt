package kz.archimedes.scheduleservice.dto.holiday

import kz.archimedes.api.command.CreateHolidayCommand
import java.time.LocalDate
import java.util.*

data class CreateHolidayDTO(
    val branchId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate
) : HolidayDTO<CreateHolidayCommand>