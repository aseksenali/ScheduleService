package kz.archimedes.scheduleservice.repository.extension

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kz.archimedes.scheduleservice.exception.DatabaseRecordNotFoundException
import kz.archimedes.scheduleservice.model.query.ScheduleHistoryEntity
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.*
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
class CustomScheduleHistoryRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) :
    CustomScheduleHistoryRepository {
    override suspend fun findScheduleHistoryBetween(
        medicId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ScheduleHistoryEntity> {
        val query = Query()
            .addCriteria(ScheduleHistoryEntity::medicId isEqualTo medicId)
            .addCriteria(
                Criteria()
                    .andOperator(
                        ScheduleHistoryEntity::createdDate gte startDate,
                        ScheduleHistoryEntity::createdDate lte endDate
                    )
            )

        return mongoTemplate.find<ScheduleHistoryEntity>(query).collectList().awaitSingle()
    }

    override suspend fun findScheduleOnDate(medicId: UUID, date: LocalDate): List<ScheduleHistoryEntity> {
        return findScheduleHistoryBetween(
            medicId,
            LocalDateTime.from(date),
            LocalDateTime.from(date.plusDays(1))
        )
    }

    override suspend fun findLatestScheduleHistory(medicId: UUID): ScheduleHistoryEntity {
        val query = Query()
            .addCriteria(ScheduleHistoryEntity::medicId isEqualTo medicId)
            .with(Sort.by(Sort.Direction.DESC, "createdDate"))
            .limit(1)
        return mongoTemplate.find<ScheduleHistoryEntity>(query).awaitFirstOrNull()
            ?: throw DatabaseRecordNotFoundException
    }
}