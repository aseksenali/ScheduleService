package kz.archimedes.api.event

import kz.archimedes.scheduleservice.model.util.WorkingHours
import java.time.DayOfWeek
import java.util.*

sealed interface WorkingDayEvent

data class WorkingDayUpdatedEvent(
    val medicId: UUID,
    val branchId: UUID,
    val specialtyId: UUID,
    val day: DayOfWeek,
    val workingHours: List<WorkingHours>
): WorkingDayEvent