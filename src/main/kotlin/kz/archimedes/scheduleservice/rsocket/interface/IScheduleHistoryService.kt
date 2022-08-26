package kz.archimedes.scheduleservice.rsocket.`interface`

import kotlinx.coroutines.flow.Flow
import kz.archimedes.scheduleservice.model.query.ScheduleHistoryEntity
import java.util.UUID

interface IScheduleHistoryService {
    suspend fun findAllHistoryForMedic(medicId: UUID): Flow<ScheduleHistoryEntity>
}