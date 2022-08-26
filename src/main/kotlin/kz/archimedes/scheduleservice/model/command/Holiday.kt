package kz.archimedes.scheduleservice.model.command

import kz.archimedes.scheduleservice.model.util.TimeInterval
import org.axonframework.modelling.command.EntityId
import java.time.LocalDate
import java.util.*

data class Holiday(
    @EntityId
    var branchId: UUID,
    var startDate: LocalDate,
    var endDate: LocalDate
) : TimeInterval<LocalDate>(startDate, endDate)