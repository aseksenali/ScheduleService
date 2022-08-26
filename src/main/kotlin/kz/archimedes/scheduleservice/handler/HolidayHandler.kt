package kz.archimedes.scheduleservice.handler

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kz.archimedes.api.command.CreateHolidayCommand
import kz.archimedes.api.command.DeleteHolidayCommand
import kz.archimedes.api.query.FindHolidaysForMedicQuery
import kz.archimedes.api.query.FindScheduleQuery
import kz.archimedes.scheduleservice.dto.holiday.CreateHolidayDTO
import kz.archimedes.scheduleservice.dto.holiday.DeleteHolidayDTO
import kz.archimedes.scheduleservice.exception.ValidationException
import kz.archimedes.scheduleservice.handler.`interface`.IHolidayHandler
import kz.archimedes.scheduleservice.model.query.HolidayEntity
import kz.archimedes.scheduleservice.model.query.ScheduleEntity
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.time.LocalDate
import java.util.*

@Component
class HolidayHandler(
    private val commandGateway: ReactorCommandGateway, private val queryGateway: ReactorQueryGateway
) : IHolidayHandler {
    private fun String.toUUID() = UUID.fromString(this)
    private fun String.toLocalDate() = LocalDate.parse(this)

    override suspend fun createHoliday(request: ServerRequest): ServerResponse {
        val medicId = request.pathVariable("medicId").toUUID()
        val requestData = request.bodyToMono<CreateHolidayDTO>().awaitSingleOrNull() ?: throw IllegalArgumentException()
        val query = FindScheduleQuery(medicId)
        val subscriptionQuery = queryGateway.subscriptionQuery(
            query,
            ResponseTypes.instanceOf(ScheduleEntity::class.java),
            ResponseTypes.instanceOf(ScheduleEntity::class.java)
        ).awaitSingle()
        val command = CreateHolidayCommand(
            medicId, requestData.branchId, requestData.startDate, requestData.endDate
        )
        commandGateway.send<Void>(command).awaitSingleOrNull()
        return ServerResponse.ok().bodyValueAndAwait(subscriptionQuery.updates().awaitFirst())
    }

    override suspend fun findHolidays(request: ServerRequest): ServerResponse {
        val startDate = request.queryParamOrNull("startDate")?.toLocalDate()
        val endDate = request.queryParamOrNull("endDate")?.toLocalDate()
        val medicId = request.pathVariable("medicId").toUUID()
        val query = FindHolidaysForMedicQuery(
            medicId, startDate ?: LocalDate.MIN, endDate ?: LocalDate.MAX
        )
        val result = queryGateway.query(
            query, ResponseTypes.multipleInstancesOf(HolidayEntity::class.java)
        ).awaitSingle()
        return ServerResponse.ok().json().bodyValueAndAwait(result)
    }

    override suspend fun deleteHoliday(request: ServerRequest): ServerResponse {
        val requestData = request.bodyToMono<DeleteHolidayDTO>().awaitSingleOrNull()
            ?: throw ValidationException("Request format is incorrect")
        val medicId = request.pathVariable("medicId").toUUID()
        val query = FindScheduleQuery(medicId)
        val subscriptionQuery = queryGateway.subscriptionQuery(
            query,
            ResponseTypes.instanceOf(ScheduleEntity::class.java),
            ResponseTypes.instanceOf(ScheduleEntity::class.java)
        ).awaitSingle()
        val command = DeleteHolidayCommand(
            medicId, requestData.branchId, requestData.startDate, requestData.endDate
        )
        commandGateway.send<Void>(command).awaitSingleOrNull()
        val result = subscriptionQuery.updates().awaitFirst()
        return ServerResponse.ok().bodyValueAndAwait(result)
    }
}