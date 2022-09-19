package kz.archimedes.scheduleservice.handler

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kz.archimedes.api.command.CreateRelocationCommand
import kz.archimedes.api.command.CreateScheduleCommand
import kz.archimedes.api.command.CreateSpecialCaseDayCommand
import kz.archimedes.api.command.DeleteRelocationCommand
import kz.archimedes.api.query.FindAllScheduleQuery
import kz.archimedes.api.query.FindScheduleAfterRelocationQuery
import kz.archimedes.api.query.FindScheduleHistoryOfMedicQuery
import kz.archimedes.api.query.FindScheduleQuery
import kz.archimedes.scheduleservice.dto.relocation.CreateRelocationDTO
import kz.archimedes.scheduleservice.dto.relocation.DeleteRelocationDTO
import kz.archimedes.scheduleservice.dto.schedule.CreateScheduleDTO
import kz.archimedes.scheduleservice.dto.specialcase.CreateSpecialCaseDayDTO
import kz.archimedes.scheduleservice.exception.DatabaseRecordNotFoundException
import kz.archimedes.scheduleservice.exception.ValidationException
import kz.archimedes.scheduleservice.handler.`interface`.IScheduleHandler
import kz.archimedes.scheduleservice.model.query.ScheduleEntity
import kz.archimedes.scheduleservice.model.query.ScheduleHistoryEntity
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class ScheduleHandler(
    private val commandGateway: ReactorCommandGateway,
    private val queryGateway: ReactorQueryGateway
) : IScheduleHandler {
    private fun String.toUUID(): UUID = UUID.fromString(this)
    private fun String.toLocalDateTime(): LocalDateTime =
        LocalDateTime.parse(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    override suspend fun createSchedule(request: ServerRequest): ServerResponse {
        val requestData =
            request.awaitBodyOrNull<CreateScheduleDTO>() ?: throw ValidationException("Request body is not correct")
        val command = CreateScheduleCommand(
            requestData.medicId,
            requestData.branchId,
            requestData.minimalAppointmentPeriod,
            requestData.startDate,
            requestData.endDate,
            requestData.workingScheduleVisit,
            requestData.workingScheduleOutgoing,
            requestData.workingScheduleOnline
        )
        val subscriptionQuery = queryGateway.subscriptionQuery(
            FindScheduleQuery(requestData.medicId),
            ResponseTypes.optionalInstanceOf(ScheduleEntity::class.java),
            ResponseTypes.instanceOf(ScheduleEntity::class.java)
        ).awaitSingle()
        commandGateway.send<Void>(command).awaitSingle()
        return ServerResponse
            .ok()
            .json()
            .bodyValueAndAwait(subscriptionQuery.updates().awaitFirst())
    }

    override suspend fun createSpecialCaseDay(request: ServerRequest): ServerResponse {
        val medicId = request.pathVariable("medicId").toUUID()
        val requestData =
            request.awaitBodyOrNull<CreateSpecialCaseDayDTO>() ?: throw ValidationException("Request body is invalid")
        val query = FindScheduleQuery(medicId)
        val subscriptionQuery = queryGateway.subscriptionQuery(
            query, ResponseTypes.instanceOf(ScheduleEntity::class.java),
            ResponseTypes.instanceOf(ScheduleEntity::class.java)
        ).awaitSingle()
        for (date in requestData.startDate.datesUntil(requestData.endDate.plusDays(1))) {
            for (workingHours in requestData.workingSchedule.days[date.dayOfWeek] ?: listOf()) {
                val command = CreateSpecialCaseDayCommand(medicId, requestData.branchId, date, workingHours)
                commandGateway.send<Void>(command).awaitSingle()
            }
        }
        val updated = subscriptionQuery.updates().awaitFirst()
        return ServerResponse.ok()
            .json()
            .bodyValueAndAwait(updated.medicId)
    }

    override suspend fun findAllSchedules(request: ServerRequest): ServerResponse {
        val query = FindAllScheduleQuery()
        val result = queryGateway.query(query, ResponseTypes.multipleInstancesOf(ScheduleEntity::class.java)).awaitSingle()
        return ServerResponse.ok()
            .json()
            .bodyValueAndAwait(result)
    }

    override suspend fun findSchedule(request: ServerRequest): ServerResponse {
        val medicId = request.pathVariable("medicId").toUUID()
        val query = FindScheduleQuery(medicId)
        val result = queryGateway.query(query, ResponseTypes.instanceOf(ScheduleEntity::class.java))
            .awaitSingleOrNull() ?: throw DatabaseRecordNotFoundException
        return ServerResponse.ok()
            .json()
            .bodyValueAndAwait(result)
    }

    override suspend fun findScheduleHistory(request: ServerRequest): ServerResponse {
        val medicId = request.pathVariable("medicId").toUUID()
        val startDate = request.queryParamOrNull("startDate")?.toLocalDateTime() ?: LocalDateTime.of(0, 1, 1, 0, 0)
        val endDate =
            request.queryParamOrNull("endDate")?.toLocalDateTime() ?: LocalDateTime.of(100000, 12, 31, 23, 59, 59)
        val query = FindScheduleHistoryOfMedicQuery(medicId, startDate, endDate)
        val result = queryGateway.query(query, ResponseTypes.multipleInstancesOf(ScheduleHistoryEntity::class.java))
            .awaitSingle()
        return ServerResponse.ok()
            .json()
            .bodyValueAndAwait(result)
    }

    override suspend fun createRelocation(request: ServerRequest): ServerResponse {
        val medicId = request.pathVariable("medicId").toUUID()
        val requestData = request.awaitBodyOrNull<CreateRelocationDTO>()
            ?: throw ValidationException("Request is incorrect")
        val command = CreateRelocationCommand(
            medicId,
            requestData.branchId,
            requestData.startDate,
            requestData.endDate,
            requestData.workingHours
        )
        val query = FindScheduleAfterRelocationQuery(medicId)
        val subscriptionQuery = queryGateway.subscriptionQuery(
            query,
            ResponseTypes.instanceOf(ScheduleEntity::class.java),
            ResponseTypes.instanceOf(ScheduleEntity::class.java)
        ).awaitSingle()
        commandGateway.send<Void>(command).awaitFirstOrNull()
        val updated = subscriptionQuery.updates().awaitFirst()
        return ServerResponse.ok()
            .json()
            .bodyValueAndAwait(updated.medicId)
    }

    override suspend fun deleteRelocation(request: ServerRequest): ServerResponse {
        val medicId = request.pathVariable("medicId").toUUID()
        val requestData = request.bodyToMono<DeleteRelocationDTO>().awaitSingleOrNull()
            ?: throw ValidationException("Request body is invalid")
        val command = DeleteRelocationCommand(
            medicId, requestData.toBranch, requestData.startDate, requestData.endDate
        )
        val query = FindScheduleAfterRelocationQuery(medicId)
        val subscriptionQuery = queryGateway.subscriptionQuery(
            query,
            ResponseTypes.instanceOf(ScheduleEntity::class.java),
            ResponseTypes.instanceOf(ScheduleEntity::class.java)
        ).awaitSingle()
        commandGateway.send<Void>(command).awaitFirstOrNull()
        val updates = subscriptionQuery.updates().awaitFirst()
        return ServerResponse.ok()
            .json()
            .bodyValueAndAwait(updates.medicId)
    }
}