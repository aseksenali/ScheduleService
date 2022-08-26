package kz.archimedes.scheduleservice.dto.schedule

import kz.archimedes.api.command.CreateScheduleCommand
import kz.archimedes.scheduleservice.model.util.DaySchedule
import java.time.LocalDate
import java.util.*

data class CreateScheduleDTO(
    val medicId: UUID,
    val branchId: UUID,
    val specialtyId: UUID,
    val workingSchedule: List<DaySchedule>,
    val startDate: LocalDate,
    val endDate: LocalDate? = null
) : ScheduleDTO<CreateScheduleCommand>