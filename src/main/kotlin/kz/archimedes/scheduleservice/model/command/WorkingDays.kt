package kz.archimedes.scheduleservice.model.command

import kz.archimedes.api.command.UpdateWorkingDayCommand
import kz.archimedes.api.event.WorkingDayUpdatedEvent
import kz.archimedes.scheduleservice.model.util.DaySchedule
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.extensions.kotlin.applyEvent
import org.axonframework.modelling.command.EntityId
import java.util.*

class WorkingDays(
    @EntityId
    var branchId: UUID,
    var workingSchedule: List<DaySchedule>
) {

    @CommandHandler
    fun on(command: UpdateWorkingDayCommand) {
        applyEvent(
            WorkingDayUpdatedEvent(
                command.medicId,
                command.branchId,
                command.specialtyId,
                command.day,
                command.workingHours
            )
        )
    }

    @EventSourcingHandler
    fun handle(event: WorkingDayUpdatedEvent) {
        workingSchedule.map {
            if (it.specialtyId == event.specialtyId) {
                DaySchedule(event.specialtyId, it.days.mapValues { (day, workingHours) ->
                    if (day == event.day) event.workingHours
                    else workingHours
                })
            } else it
        }
    }
}