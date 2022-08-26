package kz.archimedes.scheduleservice.rsocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kz.archimedes.api.query.FindScheduleHistoryOfMedicQuery
import kz.archimedes.scheduleservice.model.query.ScheduleHistoryEntity
import kz.archimedes.scheduleservice.rsocket.`interface`.IScheduleHistoryService
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.util.*

@Controller
class ScheduleHistoryService(
    private val queryGateway: ReactorQueryGateway
) : IScheduleHistoryService {
    @MessageMapping("stream")
    override suspend fun findAllHistoryForMedic(medicId: UUID): Flow<ScheduleHistoryEntity> {
        val startDate = LocalDateTime.of(0, 1, 1, 0, 0)
        val endDate = LocalDateTime.of(100000, 12, 31, 23, 59, 59)
        val result = queryGateway.subscriptionQuery(
            FindScheduleHistoryOfMedicQuery(medicId, startDate, endDate),
            ResponseTypes.multipleInstancesOf(ScheduleHistoryEntity::class.java),
            ResponseTypes.instanceOf(ScheduleHistoryEntity::class.java)
        ).awaitSingle()
        return flow {
            for (i in result.initialResult().awaitSingle()) {
                emit(i)
            }
            emitAll(result.updates().asFlow())
        }
    }
}