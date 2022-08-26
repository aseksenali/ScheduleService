package kz.archimedes.scheduleservice.model.query

import kz.archimedes.scheduleservice.model.util.DaySchedule
import java.util.*

data class WorkingDaysEntity(
    val branchId: UUID,
    val workingSchedule: List<DaySchedule>
)