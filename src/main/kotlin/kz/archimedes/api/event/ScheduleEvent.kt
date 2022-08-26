package kz.archimedes.api.event

import kz.archimedes.scheduleservice.model.util.DaySchedule
import java.time.LocalDate
import java.util.*

sealed interface ScheduleEvent

data class DeletionDateAssignedEvent(val medicId: UUID, val date: LocalDate) :
    ScheduleEvent

data class DeletionDateRemovedEvent(val medicId: UUID) : ScheduleEvent

data class ScheduleCreatedEvent(
    val medicId: UUID,
    val branchId: UUID,
    val specialtyId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val workingSchedule: List<DaySchedule>
) : ScheduleEvent

data class ScheduleDeletedEvent(val medicId: UUID) : ScheduleEvent