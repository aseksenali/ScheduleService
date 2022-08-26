package kz.archimedes.scheduleservice.projection

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kz.archimedes.api.event.ScheduleCreatedEvent
import kz.archimedes.api.event.ScheduleDeletedEvent
import kz.archimedes.api.query.FindAllScheduleQuery
import kz.archimedes.api.query.FindScheduleAfterRelocationQuery
import kz.archimedes.api.query.FindScheduleQuery
import kz.archimedes.scheduleservice.exception.DatabaseRecordNotFoundException
import kz.archimedes.scheduleservice.model.query.ScheduleEntity
import kz.archimedes.scheduleservice.model.query.WorkingDaysEntity
import kz.archimedes.scheduleservice.repository.ScheduleEntityRepository
import org.axonframework.eventhandling.EventHandler
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.CompletableFuture

@Component
class ScheduleProjection(
    private val scheduleRepository: ScheduleEntityRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
    @EventHandler
    fun on(event: ScheduleCreatedEvent) {
        val entity = ScheduleEntity(
            event.medicId, event.startDate, event.endDate, listOf(), listOf(), WorkingDaysEntity(
                event.branchId, event.workingSchedule
            )
        ).also { it.markNew() }
        runBlocking {
            scheduleRepository.save(entity)
            queryUpdateEmitter.emit<FindScheduleQuery, ScheduleEntity>(entity) {
                it.medicId == event.medicId
            }
        }
    }

    @EventHandler
    fun on(event: ScheduleDeletedEvent) {
        runBlocking {
            scheduleRepository.deleteById(event.medicId)
            queryUpdateEmitter.complete(FindScheduleQuery::class.java) {
                it.medicId == event.medicId
            }
        }
    }

    @QueryHandler
    fun handle(query: FindScheduleQuery): CompletableFuture<ScheduleEntity> {
        return runBlocking {
            val schedule = scheduleRepository.findById(query.medicId) ?: throw DatabaseRecordNotFoundException
            schedule.toMono().toFuture()
        }
    }

    @QueryHandler
    fun handle(query: FindAllScheduleQuery): CompletableFuture<List<ScheduleEntity>> {
        return runBlocking {
            val schedules = scheduleRepository.findAll().toList()
            CompletableFuture.completedFuture(schedules)
        }
    }

    @QueryHandler
    fun handle(query: FindScheduleAfterRelocationQuery): CompletableFuture<ScheduleEntity> {
        return runBlocking {
            val schedule = scheduleRepository.findById(query.medicId) ?: throw DatabaseRecordNotFoundException
            schedule.toMono().toFuture()
        }
    }
}