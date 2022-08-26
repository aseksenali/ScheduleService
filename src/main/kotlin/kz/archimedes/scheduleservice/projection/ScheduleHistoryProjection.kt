package kz.archimedes.scheduleservice.projection

import kotlinx.coroutines.runBlocking
import kz.archimedes.api.event.*
import kz.archimedes.api.query.FindScheduleHistoryOfMedicQuery
import kz.archimedes.scheduleservice.exception.DatabaseRecordNotFoundException
import kz.archimedes.scheduleservice.model.query.HolidayEntity
import kz.archimedes.scheduleservice.model.query.ScheduleHistoryEntity
import kz.archimedes.scheduleservice.model.query.SpecialCaseDayEntity
import kz.archimedes.scheduleservice.model.query.WorkingDaysEntity
import kz.archimedes.scheduleservice.model.util.OperationType
import kz.archimedes.scheduleservice.model.util.WorkingHours
import kz.archimedes.scheduleservice.repository.ScheduleHistoryRepository
import kz.archimedes.scheduleservice.util.CollectionUtils.minus
import kz.archimedes.scheduleservice.util.CollectionUtils.plus
import org.axonframework.eventhandling.EventHandler
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture

@Component
class ScheduleHistoryProjection(
    private val scheduleRepository: ScheduleHistoryRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
    @EventHandler
    fun on(event: ScheduleCreatedEvent) {
        val entity = ScheduleHistoryEntity(
            UUID.randomUUID(), event.medicId, event.startDate, event.endDate, listOf(), listOf(), WorkingDaysEntity(
                event.branchId, event.workingSchedule
            ), OperationType.CREATE
        ).also { it.markNew() }
        runBlocking {
            scheduleRepository.save(entity)
            queryUpdateEmitter.emit<FindScheduleHistoryOfMedicQuery, ScheduleHistoryEntity>(entity) {
                it.medicId == event.medicId
            }
        }
    }

    @EventHandler
    fun on(event: HolidayCreatedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findFirstByMedicIdOrderByCreatedDateDesc(event.medicId)
                ?: throw DatabaseRecordNotFoundException
            val updatedSchedule = schedule.copy(
                id = UUID.randomUUID(),
                holidays = schedule.holidays + HolidayEntity(event.branchId, event.startDate, event.endDate),
                operationType = OperationType.UPDATE
            ).also { it.markNew() }
            scheduleRepository.save(updatedSchedule)
            queryUpdateEmitter.emit<FindScheduleHistoryOfMedicQuery, ScheduleHistoryEntity>(updatedSchedule) {
                it.medicId == event.medicId
            }
        }
    }

    @EventHandler
    fun on(event: HolidayDeletedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findFirstByMedicIdOrderByCreatedDateDesc(event.medicId)
                ?: throw DatabaseRecordNotFoundException
            val updatedSchedule = schedule.copy(
                id = UUID.randomUUID(),
                holidays = schedule.holidays - HolidayEntity(event.branchId, event.startDate, event.endDate),
                operationType = OperationType.UPDATE
            ).also { it.markNew() }
            scheduleRepository.save(updatedSchedule)
            queryUpdateEmitter.emit<FindScheduleHistoryOfMedicQuery, ScheduleHistoryEntity>(updatedSchedule) {
                it.medicId == event.medicId
            }
        }
    }

    @EventHandler
    fun on(event: ScheduleDeletedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findLatestScheduleHistory(event.medicId)
            val deletedSchedule = schedule.copy(
                id = UUID.randomUUID(),
                operationType = OperationType.DELETE
            ).also { it.markNew() }
            scheduleRepository.save(deletedSchedule)
            queryUpdateEmitter.emit<FindScheduleHistoryOfMedicQuery, ScheduleHistoryEntity>(deletedSchedule) {
                it.medicId == event.medicId
            }
        }
    }

    @EventHandler
    fun on(event: SpecialCaseDayCreatedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findLatestScheduleHistory(event.medicId)
            val updatedSchedule = schedule.copy(
                id = UUID.randomUUID(),
                specialCaseDays = schedule.specialCaseDays + SpecialCaseDayEntity(
                    event.branchId,
                    event.date,
                    event.workingHours
                ),
                operationType = OperationType.UPDATE
            ).also { it.markNew() }
            scheduleRepository.save(updatedSchedule)
            queryUpdateEmitter.emit<FindScheduleHistoryOfMedicQuery, ScheduleHistoryEntity>(updatedSchedule) {
                it.medicId == event.medicId
            }
        }
    }

    @EventHandler
    fun on(event: SpecialCaseDayDeletedEvent) {
        runBlocking {
            val schedule = scheduleRepository.findLatestScheduleHistory(event.medicId)
            val updatedSchedule = schedule.copy(
                id = UUID.randomUUID(),
                specialCaseDays = schedule.specialCaseDays - SpecialCaseDayEntity(
                    event.branchId,
                    event.date,
                    WorkingHours(event.startTime, event.endTime)
                ),
                operationType = OperationType.UPDATE
            ).also { it.markNew() }
            scheduleRepository.save(updatedSchedule)
            queryUpdateEmitter.emit<FindScheduleHistoryOfMedicQuery, ScheduleHistoryEntity>(updatedSchedule) {
                it.medicId == event.medicId
            }
        }
    }

    @QueryHandler
    fun handle(query: FindScheduleHistoryOfMedicQuery): CompletableFuture<List<ScheduleHistoryEntity>> {
        return runBlocking {
            val schedules = scheduleRepository.findScheduleHistoryBetween(query.medicId, query.startDate, query.endDate)
            CompletableFuture.completedFuture(schedules)
        }
    }
}