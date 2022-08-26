package kz.archimedes.api.command

import kz.archimedes.scheduleservice.model.util.WorkingHours
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.DayOfWeek
import java.util.*

sealed interface WorkingDayCommand

data class UpdateWorkingDayCommand(
    @TargetAggregateIdentifier
    val medicId: UUID,
    val specialtyId: UUID,
    val branchId: UUID,
    val day: DayOfWeek,
    val workingHours: List<WorkingHours>
) : WorkingDayCommand