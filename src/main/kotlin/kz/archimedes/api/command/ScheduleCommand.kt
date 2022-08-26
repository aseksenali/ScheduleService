package kz.archimedes.api.command

import kz.archimedes.scheduleservice.model.util.DaySchedule
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.LocalDate
import java.util.*

sealed interface ScheduleCommand

data class AssignDeletionDateCommand(@TargetAggregateIdentifier val medicId: UUID, val date: LocalDate) :
    ScheduleCommand

data class CreateScheduleCommand(
    @TargetAggregateIdentifier
    val medicId: UUID,
    val branchId: UUID,
    val specialtyId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val workingSchedule: List<DaySchedule>
) : ScheduleCommand

data class RemoveDeletionDateCommand(@TargetAggregateIdentifier val medicId: UUID) : ScheduleCommand
