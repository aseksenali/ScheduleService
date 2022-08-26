package kz.archimedes.scheduleservice.model.command

import kz.archimedes.api.command.*
import kz.archimedes.api.event.*
import kz.archimedes.scheduleservice.exception.ValidationException
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
import java.util.*

@Aggregate
class Schedule() {
    @AggregateIdentifier
    private lateinit var medicId: UUID
    private lateinit var startDate: LocalDate
    private var endDate: LocalDate? = null
    private var deletionDeadlineId: ScheduleToken? = null

    @AggregateMember
    private lateinit var workingDays: WorkingDays

    @AggregateMember
    private final val specialDays: MutableList<SpecialCaseDay> = mutableListOf()

    @AggregateMember
    private final val holidays: MutableList<Holiday> = mutableListOf()

    @CommandHandler
    constructor(command: CreateScheduleCommand) : this() {
        if (command.endDate?.isBefore(command.startDate) == true) throw ValidationException("Start date must be before end date")
        apply(
            ScheduleCreatedEvent(
                command.medicId, command.branchId, command.specialtyId, command.startDate, command.endDate, command.workingSchedule
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
        val currentBranch = this.workingDays.branchId
        apply(HolidayCreatedEvent(command.medicId, currentBranch, command.startDate, command.endDate))
        for (date in command.startDate.datesUntil(command.endDate.plusDays(1))) {
            for (workingSchedule in command.workingSchedule.days[date.dayOfWeek] ?: listOf()) {
                apply(SpecialCaseDayCreatedEvent(command.medicId, command.toBranch, date, workingSchedule))
            }
        }
    }

    @EventSourcingHandler
    fun handle(event: ScheduleCreatedEvent) {
        medicId = event.medicId
        startDate = event.startDate
        endDate = event.endDate
        workingDays = WorkingDays(event.branchId, event.workingSchedule)
    }

    @EventSourcingHandler
    fun handle(event: SpecialCaseDayCreatedEvent) {
        specialDays += SpecialCaseDay(event.date, event.branchId, event.workingHours)
    }

    @EventSourcingHandler
    fun handle(event: HolidayCreatedEvent) {
        holidays += Holiday(event.branchId, event.startDate, event.endDate)
    }

    @EventSourcingHandler
    fun handle(event: HolidayDeletedEvent) {
        holidays -= Holiday(event.branchId, event.startDate, event.endDate)
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