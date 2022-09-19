package kz.archimedes.api.command

import kz.archimedes.scheduleservice.model.util.WeekSchedule
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.LocalDate
import java.util.*

sealed interface RelocationCommand

data class CreateRelocationCommand(
    @TargetAggregateIdentifier
    val medicId: UUID,
    val toBranch: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val workingSchedule: WeekSchedule
) : RelocationCommand

data class DeleteRelocationCommand(
    @TargetAggregateIdentifier
    val medicId: UUID,
    val toBranch: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate
) : RelocationCommand