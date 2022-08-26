package kz.archimedes.api.command

import kz.archimedes.scheduleservice.model.util.DaySchedule
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
    val workingSchedule: DaySchedule
) : RelocationCommand

data class DeleteRelocationCommand(
    @TargetAggregateIdentifier
    val medicId: UUID,
    val toBranch: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate
) : RelocationCommand