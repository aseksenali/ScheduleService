package kz.archimedes.api.query

import java.time.LocalDateTime
import java.util.*

sealed interface ScheduleQuery

data class FindScheduleHistoryOfMedicQuery(
    val medicId: UUID,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
) : ScheduleQuery

data class FindAllScheduleQuery(
    val branchId: UUID? = null,
): ScheduleQuery

data class FindScheduleQuery(val medicId: UUID) : ScheduleQuery

data class FindScheduleAfterRelocationQuery(val medicId: UUID) : ScheduleQuery