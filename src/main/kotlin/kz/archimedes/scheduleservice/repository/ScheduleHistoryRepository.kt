package kz.archimedes.scheduleservice.repository

import kz.archimedes.scheduleservice.model.query.ScheduleHistoryEntity
import kz.archimedes.scheduleservice.repository.extension.CustomScheduleHistoryRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.util.*

interface ScheduleHistoryRepository: CoroutineSortingRepository<ScheduleHistoryEntity, UUID>, CustomScheduleHistoryRepository {
    suspend fun findFirstByMedicIdOrderByCreatedDateDesc(medicId: UUID): ScheduleHistoryEntity?
    suspend fun findAllByMedicId(medicId: UUID): List<ScheduleHistoryEntity>
}