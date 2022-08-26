package kz.archimedes.scheduleservice.model.command

import kz.archimedes.api.command.DeleteSpecialCaseDayCommand
import kz.archimedes.api.event.SpecialCaseDayDeletedEvent
import kz.archimedes.scheduleservice.exception.ValidationException
import kz.archimedes.scheduleservice.model.util.TimeInterval
import kz.archimedes.scheduleservice.model.util.WorkingHours
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.EntityId
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class SpecialCaseDay(
    @EntityId
    var date: LocalDate,
    var branchId: UUID,
    var workingHours: WorkingHours
) : TimeInterval<LocalTime>(workingHours.startTime, workingHours.endTime) {
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
        this.workingHours = WorkingHours(startTime, endTime)
    }
}