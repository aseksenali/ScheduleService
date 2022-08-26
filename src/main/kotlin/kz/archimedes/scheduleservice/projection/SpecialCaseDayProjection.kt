package kz.archimedes.scheduleservice.projection

import kotlinx.coroutines.runBlocking
import kz.archimedes.api.event.SpecialCaseDayCreatedEvent
import kz.archimedes.api.event.SpecialCaseDayDeletedEvent
import kz.archimedes.api.query.FindScheduleAfterRelocationQuery
import kz.archimedes.api.query.FindScheduleQuery
import kz.archimedes.scheduleservice.exception.DatabaseRecordNotFoundException
import kz.archimedes.scheduleservice.model.query.ScheduleEntity
import kz.archimedes.scheduleservice.model.query.SpecialCaseDayEntity
import kz.archimedes.scheduleservice.model.util.WorkingHours
import kz.archimedes.scheduleservice.repository.ScheduleEntityRepository
import kz.archimedes.scheduleservice.util.CollectionUtils.minus
import kz.archimedes.scheduleservice.util.CollectionUtils.plus
import org.axonframework.eventhandling.EventHandler
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
class SpecialCaseDayProjection(
    private val scheduleRepository: ScheduleEntityRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
    @EventHandler
    fun on(event: SpecialCaseDayCreatedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findById(event.medicId) ?: throw DatabaseRecordNotFoundException
            val updatedSchedule = schedule.copy(
                specialCaseDays = schedule.specialCaseDays + SpecialCaseDayEntity(event.branchId, event.date, event.workingHours)
            )
            scheduleRepository.save(updatedSchedule)
            queryUpdateEmitter.emit<FindScheduleQuery, ScheduleEntity>(updatedSchedule) {
                it.medicId == event.medicId
            }
            queryUpdateEmitter.emit<FindScheduleAfterRelocationQuery, ScheduleEntity>(updatedSchedule) {
                it.medicId == event.medicId
            }
        }
    }

    @EventHandler
    fun on(event: SpecialCaseDayDeletedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findById(event.medicId) ?: throw DatabaseRecordNotFoundException
            val updatedSchedule = schedule.copy(
                specialCaseDays = schedule.specialCaseDays - SpecialCaseDayEntity(event.branchId, event.date, WorkingHours(event.startTime, event.endTime))
            )
            scheduleRepository.save(updatedSchedule)
            queryUpdateEmitter.emit<FindScheduleQuery, ScheduleEntity>(updatedSchedule) {
                it.medicId == event.medicId
            }
            queryUpdateEmitter.emit<FindScheduleAfterRelocationQuery, ScheduleEntity>(updatedSchedule) {
                it.medicId == event.medicId
            }
        }
    }
}