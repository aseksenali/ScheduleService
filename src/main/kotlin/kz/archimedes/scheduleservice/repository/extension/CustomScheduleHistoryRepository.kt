package kz.archimedes.scheduleservice.repository.extension

import kz.archimedes.scheduleservice.model.query.ScheduleHistoryEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface CustomScheduleHistoryRepository {
    suspend fun findScheduleHistoryBetween(medicId: UUID, startDate: LocalDateTime, endDate: LocalDateTime): List<ScheduleHistoryEntity>
    suspend fun findScheduleOnDate(medicId: UUID, date: LocalDate): List<ScheduleHistoryEntity>
    suspend fun findLatestScheduleHistory(medicId: UUID): ScheduleHistoryEntity
}