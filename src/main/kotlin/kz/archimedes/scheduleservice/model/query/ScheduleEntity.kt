package kz.archimedes.scheduleservice.model.query

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Document("schedule")
data class ScheduleEntity(
    val medicId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val holidays: List<HolidayEntity>,
    val specialCaseDays: List<SpecialCaseDayEntity>,
    val workingSchedule: WorkingDaysEntity,

    @CreatedDate
    var createdDate: LocalDateTime? = null,
    @CreatedBy
    var createdBy: String? = null,
    @LastModifiedDate
    var modifiedDate: LocalDateTime? = null,
    @LastModifiedBy
    var modifiedBy: String? = null,
) : BasePersistable<UUID>(medicId)