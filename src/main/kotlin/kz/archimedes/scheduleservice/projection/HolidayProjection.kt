package kz.archimedes.scheduleservice.projection

import kotlinx.coroutines.runBlocking
import kz.archimedes.api.event.HolidayCreatedEvent
import kz.archimedes.api.event.HolidayDeletedEvent
import kz.archimedes.api.query.FindHolidaysForMedicQuery
import kz.archimedes.api.query.FindScheduleAfterRelocationQuery
import kz.archimedes.api.query.FindScheduleQuery
import kz.archimedes.scheduleservice.exception.DatabaseRecordNotFoundException
import kz.archimedes.scheduleservice.model.query.HolidayEntity
import kz.archimedes.scheduleservice.model.query.ScheduleEntity
import kz.archimedes.scheduleservice.repository.ScheduleEntityRepository
import kz.archimedes.scheduleservice.util.CollectionUtils.minus
import kz.archimedes.scheduleservice.util.CollectionUtils.plus
import org.axonframework.eventhandling.EventHandler
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class HolidayProjection(
    private val scheduleRepository: ScheduleEntityRepository, private val queryUpdateEmitter: QueryUpdateEmitter
) {
    @EventHandler
    fun on(event: HolidayDeletedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findById(event.medicId) ?: throw DatabaseRecordNotFoundException
            val updatedSchedule = schedule.copy(
                holidays = schedule.holidays - HolidayEntity(event.startDate, event.endDate)
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
    fun on(event: HolidayCreatedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findById(event.medicId) ?: throw DatabaseRecordNotFoundException
            val updatedSchedule = schedule.copy(
                holidays = schedule.holidays + HolidayEntity(event.startDate, event.endDate)
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

    @QueryHandler
    fun handle(query: FindHolidaysForMedicQuery): CompletableFuture<List<HolidayEntity>> {
        return runBlocking {
            val schedule = scheduleRepository.findById(query.medicId) ?: throw DatabaseRecordNotFoundException
            val holidays = schedule.holidays.filter {
                it.startDate >= query.startDate && it.endDate <= query.endDate
            }
            CompletableFuture.completedFuture(holidays)
        }
    }
}