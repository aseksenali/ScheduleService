package kz.archimedes.api.event

import kz.archimedes.scheduleservice.model.util.WeekSchedule
import java.time.LocalDate
import java.util.*

sealed interface ScheduleEvent

data class DeletionDateAssignedEvent(val medicId: UUID, val date: LocalDate) :
    ScheduleEvent

data class DeletionDateRemovedEvent(val medicId: UUID) : ScheduleEvent

data class ScheduleCreatedEvent(
    val medicId: UUID,
    val branchId: UUID,
    val minimalAppointmentPeriod: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val workingScheduleVisit: WeekSchedule?,
    val workingScheduleOutgoing: WeekSchedule?,
    val workingScheduleOnline: WeekSchedule?,
) : ScheduleEvent

data class ScheduleDeletedEvent(val medicId: UUID) : ScheduleEvent