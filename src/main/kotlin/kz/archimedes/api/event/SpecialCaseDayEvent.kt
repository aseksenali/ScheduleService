package kz.archimedes.api.event

import kz.archimedes.scheduleservice.model.util.WorkingHours
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

sealed interface SpecialCaseDayEvent

data class SpecialCaseDayCreatedEvent(
    val medicId: UUID,
    val branchId: UUID,
    val date: LocalDate,
    val workingHours: WorkingHours
) : SpecialCaseDayEvent

data class SpecialCaseDayDeletedEvent(
    val medicId: UUID,
    val branchId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
) : SpecialCaseDayEvent
