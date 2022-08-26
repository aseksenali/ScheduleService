package kz.archimedes.scheduleservice.model.query

import kz.archimedes.scheduleservice.model.util.OperationType
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Document("schedule_history")
data class ScheduleHistoryEntity(
    val id: UUID = UUID.randomUUID(),
    val medicId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val holidays: List<HolidayEntity>,
    val specialCaseDays: List<SpecialCaseDayEntity>,
    val workingDays: WorkingDaysEntity,
    val operationType: OperationType,
    @CreatedDate
    var createdDate: LocalDateTime? = null,
    @CreatedBy
    var createdBy: String? = null
) : BasePersistable<UUID>(id)