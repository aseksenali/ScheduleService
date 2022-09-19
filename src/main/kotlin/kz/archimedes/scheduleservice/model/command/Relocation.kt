package kz.archimedes.scheduleservice.model.command

import kz.archimedes.scheduleservice.model.util.WeekSchedule
import org.axonframework.eventhandling.scheduling.ScheduleToken
import java.time.LocalDate
import java.util.*

data class Relocation(
    var startDate: LocalDate,
    var endDate: LocalDate? = null,
    var branchId: UUID,
    var minimalAppointmentPeriod: Int = 0,
    var deletionDeadlineId: ScheduleToken? = null,

    var specialDays: MutableList<SpecialCaseDay> = mutableListOf(),
    var holidays: MutableList<Holiday> = mutableListOf(),

    var workingScheduleVisit: WeekSchedule? = null,
    var workingScheduleOutgoing: WeekSchedule? = null,
    var workingScheduleOnline: WeekSchedule? = null,
)