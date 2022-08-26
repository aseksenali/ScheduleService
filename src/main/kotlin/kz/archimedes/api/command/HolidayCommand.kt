package kz.archimedes.api.command

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.LocalDate
import java.util.*

sealed interface HolidayCommand

data class CreateHolidayCommand(
    @TargetAggregateIdentifier val medicId: UUID, val branchId: UUID, val startDate: LocalDate, val endDate: LocalDate
) : HolidayCommand

data class DeleteHolidayCommand(
    @TargetAggregateIdentifier val medicId: UUID, val branchId: UUID, val startDate: LocalDate, val endDate: LocalDate
) : HolidayCommand