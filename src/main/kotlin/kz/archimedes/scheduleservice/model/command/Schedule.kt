package kz.archimedes.scheduleservice.model.command

import kz.archimedes.api.command.*
import kz.archimedes.api.event.*
import kz.archimedes.scheduleservice.exception.ValidationException
import kz.archimedes.scheduleservice.model.util.WeekSchedule
import kz.archimedes.scheduleservice.model.util.WorkingHours
import kz.archimedes.scheduleservice.util.CollectionUtils.minusAssign
import kz.archimedes.scheduleservice.util.CollectionUtils.plusAssign
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.deadline.annotation.DeadlineHandler
import org.axonframework.eventhandling.scheduling.EventScheduler
import org.axonframework.eventhandling.scheduling.ScheduleToken
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.annotation.MetaDataValue
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateLifecycle.markDeleted
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.spring.stereotype.Aggregate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@Aggregate
class Schedule() {
    @AggregateIdentifier
    private lateinit var medicId: UUID
    private lateinit var branchId: UUID
    private lateinit var startDate: LocalDate
    private var endDate: LocalDate? = null
    private var minimalAppointmentPeriod: Int = 0
    private var deletionDeadlineId: ScheduleToken? = null

    private final var specialDays: MutableList<SpecialCaseDay> = mutableListOf()
    private final var holidays: MutableList<Holiday> = mutableListOf()
    private final var relocations: MutableList<Relocation> = mutableListOf()

    @AggregateMember
    private var workingScheduleVisit: WeekSchedule? = null

    @AggregateMember
    private var workingScheduleOutgoing: WeekSchedule? = null

    @AggregateMember
    private var workingScheduleOnline: WeekSchedule? = null

    @CommandHandler
    constructor(command: CreateScheduleCommand) : this() {
        if (command.endDate?.isBefore(command.startDate) == true) throw ValidationException("Start date must be before end date")
        apply(
            ScheduleCreatedEvent(
                command.medicId,
                command.branchId,
                command.minimalAppointmentPeriod,
                command.startDate,
                command.endDate,
                command.workingScheduleVisit,
                command.workingScheduleOutgoing,
                command.workingScheduleOnline
            )
        )
    }

    @CommandHandler
    fun on(command: CreateHolidayCommand) {
        if (command.endDate.isBefore(command.startDate)) throw ValidationException("Start date must be before end date")
        apply(
            HolidayCreatedEvent(
                command.medicId, command.branchId, command.startDate, command.endDate
            )
        )
    }

    @CommandHandler
    fun on(command: CreateSpecialCaseDayCommand) {
        if (command.date.isBefore(LocalDate.now())) throw ValidationException("Special case date must not be created before current date")
        apply(
            SpecialCaseDayCreatedEvent(
                command.medicId, command.branchId, command.date, command.workingHours
            )
        )
    }

    @CommandHandler
    fun on(command: DeleteHolidayCommand) {
        apply(
            HolidayDeletedEvent(
                command.medicId, command.branchId, command.startDate, command.endDate
            )
        )
    }

    @CommandHandler
    fun on(command: AssignDeletionDateCommand, eventScheduler: EventScheduler) {
        val deadlineId = eventScheduler.schedule(
            Instant.from(command.date), ScheduleDeletedEvent(command.medicId)
        )
        apply(DeletionDateAssignedEvent(command.medicId, command.date), MetaData.with("deletionDeadlineId", deadlineId))
    }

    @CommandHandler
    fun on(command: RemoveDeletionDateCommand, eventScheduler: EventScheduler) {
        eventScheduler.cancelSchedule(deletionDeadlineId)
        apply(DeletionDateRemovedEvent(medicId))
    }

    @CommandHandler
    fun on(command: CreateRelocationCommand) {
        val currentBranch = this.branchId
        apply(HolidayCreatedEvent(command.medicId, currentBranch, command.startDate, command.endDate))
        for (date in command.startDate.datesUntil(command.endDate.plusDays(1))) {
            for (workingSchedule in command.workingSchedule.days[date.dayOfWeek] ?: listOf()) {
                apply(SpecialCaseDayCreatedEvent(command.medicId, command.toBranch, date, workingSchedule))
            }
        }
    }

    @CommandHandler
    fun on(command: DeleteSpecialCaseDayCommand) {
        if (command.startTime.isAfter(command.endTime))
            throw ValidationException("Start time is after end time")
        if (command.endTime.isBefore(command.startTime))
            throw ValidationException("End time is before start time")
        apply(
            SpecialCaseDayDeletedEvent(
                command.medicId,
                command.branchId,
                command.date,
                command.startTime,
                command.endTime
            )
        )
    }

    @EventSourcingHandler
    fun handle(event: SpecialCaseDayDeletedEvent) {
        val startTime: LocalTime = event.startTime
        val endTime: LocalTime = event.endTime
        specialDays.forEach {
            val workingHours = WorkingHours(startTime, endTime)
            if (it.workingHours == workingHours)
                it.workingHours = workingHours
        }
    }

    @EventSourcingHandler
    fun handle(event: ScheduleCreatedEvent) {
        medicId = event.medicId
        branchId = event.branchId
        minimalAppointmentPeriod = event.minimalAppointmentPeriod
        startDate = event.startDate
        endDate = event.endDate
        workingScheduleVisit = event.workingScheduleVisit
        workingScheduleOutgoing = event.workingScheduleOutgoing
        workingScheduleOnline = event.workingScheduleOnline
    }

    @EventSourcingHandler
    fun handle(event: SpecialCaseDayCreatedEvent) {
        specialDays += SpecialCaseDay(event.date, event.workingHours)
    }

    @EventSourcingHandler
    fun handle(event: HolidayCreatedEvent) {
        holidays += Holiday(event.startDate, event.endDate)
    }

    @EventSourcingHandler
    fun handle(event: HolidayDeletedEvent) {
        holidays -= Holiday(event.startDate, event.endDate)
    }

    @EventSourcingHandler
    fun handle(event: DeletionDateAssignedEvent, @MetaDataValue("deletionDeadlineId") deadlineId: ScheduleToken) {
        endDate = event.date
        deletionDeadlineId = deadlineId
    }

    @EventSourcingHandler
    fun handle(event: DeletionDateRemovedEvent) {
        endDate = null
        deletionDeadlineId = null
    }

    @EventSourcingHandler
    fun handle(event: ScheduleDeletedEvent) {
        markDeleted()
    }

    @DeadlineHandler
    fun handle(medicId: UUID) {
        apply(ScheduleDeletedEvent(medicId))
    }
}