package kz.archimedes.api.event

import java.time.LocalDate
import java.util.*

sealed interface HolidayEvent

data class HolidayCreatedEvent(
    val medicId: UUID,
    val branchId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate
) : HolidayEvent

data class HolidayDeletedEvent(
    val medicId: UUID,
    val branchId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate
) : HolidayEvent

