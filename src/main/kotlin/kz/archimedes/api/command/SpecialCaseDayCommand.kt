package kz.archimedes.api.command

import kz.archimedes.scheduleservice.model.util.WorkingHours
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

sealed interface SpecialCaseDayCommand

data class DeleteSpecialCaseDayCommand(
    @TargetAggregateIdentifier
    val medicId: UUID,
    val branchId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
) : SpecialCaseDayCommand

data class CreateSpecialCaseDayCommand(
    @TargetAggregateIdentifier
    val medicId: UUID,
    val branchId: UUID,
    val date: LocalDate,
    val workingHours: WorkingHours
) : SpecialCaseDayCommand