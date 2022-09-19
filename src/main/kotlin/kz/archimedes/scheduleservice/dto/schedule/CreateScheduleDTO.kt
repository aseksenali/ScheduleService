package kz.archimedes.scheduleservice.dto.schedule

import kz.archimedes.api.command.CreateScheduleCommand
import kz.archimedes.scheduleservice.model.util.WeekSchedule
import java.time.LocalDate
import java.util.*

data class CreateScheduleDTO(
    val medicId: UUID,
    val branchId: UUID,
    val minimalAppointmentPeriod: Int,
    val workingScheduleVisit: WeekSchedule?,
    val workingScheduleOutgoing: WeekSchedule?,
    val workingScheduleOnline: WeekSchedule?,
    val startDate: LocalDate,
    val endDate: LocalDate? = null
) : ScheduleDTO<CreateScheduleCommand>